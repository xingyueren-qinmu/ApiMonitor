package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook;

import android.text.TextUtils;
import android.util.Log;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter.EventFormatter;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter.GZipDecodeFormatter;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter.HttpBaseFormatter;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter.HttpChunckAggregateFormatter;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.observer.EventObserver;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SocketMonitor {
    private static Set<Class> hookedClass = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static Map<Socket, SocketMonitor> allMonitor = new ConcurrentHashMap<>();

    private static Map<String, EventFormatter> formatterMap = new HashMap<>();

    private static LinkedList<String> formatterChain = new LinkedList<>();

    private static Set<EventObserver> socketEventObserver = new HashSet<>();

    private Socket socket;

    private InputStreamWrapper inputStreamWrapper;
    private OutputStreamWrapper outputStreamWrapper;

    private static final int statusInit = 0;
    public static final int statusRead = 1;
    public static final int statusWrite = 2;
    private static final int statusDestroy = 3;

    private int nowStatus = statusInit;


    public int getNowStatus() {
        return nowStatus;
    }

    public void checkStatus(int status) {

        if (status <= statusInit || status > statusDestroy) {
            throw new IllegalStateException("error status:" + status);
        }

        if (nowStatus == statusInit) {
            nowStatus = status;
            return;
        }

        if (nowStatus == status) {
            return;
        }


        //切换读写模式，准备输出
        SocketPackEvent socketPackEvent;
        if (nowStatus == statusRead) {
            //当前状态是读，将写的缓存包装成时间
            socketPackEvent = inputStreamWrapper.genEvent();
        } else {
            socketPackEvent = outputStreamWrapper.genEvent();
        }

        nowStatus = status;

        socketPackEvent.socket = socket;

        //call formatter
        for (String formatterKey : formatterChain) {
            try {
                EventFormatter eventFormatter = formatterMap.get(formatterKey);
                if (eventFormatter == null) {
                    Log.w("RATEL", "can not find formatter for :" + formatterKey);
                    continue;
                }
                eventFormatter.formatEvent(socketPackEvent);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        //call event observer
        for (EventObserver eventObserver : socketEventObserver) {
            try {
                eventObserver.onSocketPackageArrival(socketPackEvent);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

    }

    public SocketMonitor(Socket socket) {
        this.socket = socket;
    }

    static {
        startMonitorInternal();
        addDefaultFormatter();
    }


    private static void startMonitorInternal() {
        //to find all class that extend java.net.Socket
        XposedBridge.hookAllConstructors(Socket.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Class<?> theSocketClass = param.thisObject.getClass();

                Socket socket = (Socket) param.thisObject;
                allMonitor.put(socket, new SocketMonitor(socket));

                if (hookedClass.contains(theSocketClass)) {
                    return;
                }
                synchronized (SocketMonitor.class) {
                    if (hookedClass.contains(theSocketClass)) {
                        return;
                    }
                    monitorSocketClass(theSocketClass);
                    hookedClass.add(theSocketClass);
                }
            }
        });
    }

    private static SocketMonitor makeSureSocketMonitor(Object socketObj) {
        Socket socket = (Socket) socketObj;

        SocketMonitor socketMonitor = allMonitor.get(socket);
        if (socketMonitor == null) {
            socketMonitor = new SocketMonitor(socket);
            allMonitor.put(socket, socketMonitor);
        }

        return socketMonitor;
    }

    private static void monitorSocketClass(Class socketClass) {
        //monitor socket input,


        XposedBridge.hookAllMethods(socketClass, "getInputStream", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                InputStream inputStream = (InputStream) param.getResult();
                if (inputStream instanceof InputStreamWrapper) {
                    return;
                }

                SocketMonitor socketMonitor = makeSureSocketMonitor(param.thisObject);

                if (socketMonitor.inputStreamWrapper == null) {
                    socketMonitor.inputStreamWrapper = new InputStreamWrapper(inputStream, socketMonitor);
                }

                param.setResult(socketMonitor.inputStreamWrapper);
            }
        });

        //monitor socket output,
        XposedBridge.hookAllMethods(socketClass, "getOutputStream", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                OutputStream outputStream = (OutputStream) param.getResult();
                if (outputStream instanceof OutputStreamWrapper) {
                    return;
                }

                SocketMonitor socketMonitor = makeSureSocketMonitor(param.thisObject);
                if (socketMonitor.outputStreamWrapper == null) {
                    socketMonitor.outputStreamWrapper = new OutputStreamWrapper(outputStream, socketMonitor);
                }
                param.setResult(socketMonitor.outputStreamWrapper);
            }
        });

        if(Reflector.findMethod(socketClass, "close") != null) {
            XposedHelpers.findAndHookMethod(socketClass, "close", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    SocketMonitor socketMonitor = makeSureSocketMonitor(param.thisObject);
                    socketMonitor.destroy();
                }
            });
        }
    }

    public void destroy() {
        SocketMonitor remove = allMonitor.remove(socket);
        if (remove == null) {
            return;
        }

        checkStatus(SocketMonitor.statusDestroy);
    }


    private static synchronized void addDefaultFormatter() {
        //分割http协议头部数据，解析header，method，http2params等
        addFormatter(EventFormatter.HTTP_BASE, new HttpBaseFormatter());

        //处理http分段传输问题
        addFormatter(EventFormatter.HTTP_CHUNKED_AGGRE, new HttpChunckAggregateFormatter());

        //http压缩问题
        addFormatter(EventFormatter.HTTP_UNZIP_GZIP, new GZipDecodeFormatter());


    }

    public synchronized static void addFormatter(String name, EventFormatter eventFormatter) {
        addFormatter(name, null, eventFormatter);
    }

    public synchronized static void addFormatter(String name, String before, EventFormatter eventFormatter) {
        if (TextUtils.isEmpty(name)) {
            return;
        }


        if (formatterMap.containsKey(name)) {
            formatterMap.put(name, eventFormatter);
            return;
        }
        formatterMap.put(name, eventFormatter);


        int index = -1;

        if (!TextUtils.isEmpty(before)) {
            index = formatterChain.indexOf(before);
        }

        if (index < 0) {
            formatterChain.addLast(name);
        } else {
            formatterChain.add(index, name);
        }
    }

    public static synchronized void addPacketEventObserver(EventObserver eventObserver) {
        socketEventObserver.add(eventObserver);
    }

    public static synchronized void setPacketEventObserver(EventObserver eventObserver) {
        socketEventObserver.clear();
        socketEventObserver.add(eventObserver);
    }
}
