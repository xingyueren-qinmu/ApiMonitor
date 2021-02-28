package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.observer;

import android.util.Log;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketMonitor;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketPackEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileLogEventObserver implements EventObserver {

    private File socketMonitorDirectory;

    private Map<Socket, EventAppender> socketEventAppenderMap = new ConcurrentHashMap<>();

    public FileLogEventObserver(File socketMonitorDirectory) {
        this.socketMonitorDirectory = socketMonitorDirectory;
        //不能在这里创建文件夹，因为可能存在文件重定向，理论上需要在文件重定向之后再创建日志文件夹，或者在文件路径白名单中处理
//        if (!socketMonitorDirectory.exists()) {
//            if (!socketMonitorDirectory.mkdirs()) {
//                throw new IllegalStateException("can not create directory: " + socketMonitorDirectory.getAbsolutePath());
//            }
//        }
    }

    @Override
    public void onSocketPackageArrival(SocketPackEvent socketPackEvent) {
        EventAppender eventAppender = makeSureAppender(socketPackEvent.socket);
        if (eventAppender == null) {
            //not happen
            return;
        }

        eventAppender.appendEvent(socketPackEvent);
    }

    private EventAppender makeSureAppender(Socket socket) {
        EventAppender eventAppender = socketEventAppenderMap.get(socket);
        if (eventAppender != null) {
            return eventAppender;
        }
        synchronized (this) {
            eventAppender = socketEventAppenderMap.get(socket);
            if (eventAppender != null) {
                return eventAppender;
            }
            try {
                if (!socketMonitorDirectory.exists()) {
                    if (!socketMonitorDirectory.mkdirs()) {
                        throw new IllegalStateException("can not create directory: " + socketMonitorDirectory.getAbsolutePath());
                    }
                }
                eventAppender = new EventAppender(new File(socketMonitorDirectory, System.currentTimeMillis() + "_socket.txt"));
                socketEventAppenderMap.put(socket, eventAppender);
                return eventAppender;
            } catch (IOException e) {
                Log.e("RATEL", "failed to write data", e);
                return null;
            }
        }
    }


    private class EventAppender {
        private FileOutputStream fileOutputStream;

        public EventAppender(File theLogFile) throws IOException {
            if (!theLogFile.exists()) {
                if (!theLogFile.createNewFile()) {
                    throw new IOException("can not create file: " + theLogFile.getAbsolutePath());
                }
            }
            fileOutputStream = new FileOutputStream(theLogFile, true);
        }

        public synchronized void appendEvent(SocketPackEvent socketPackEvent) {
            int localPort = socketPackEvent.socket.getLocalPort();
            int remotePort = socketPackEvent.socket.getPort();
            InetAddress inetAddress = socketPackEvent.socket.getInetAddress();

            String remoteAddress;
            if (inetAddress != null) {
                remoteAddress = inetAddress.getHostAddress();
            } else {
                remoteAddress = socketPackEvent.socket.toString();
            }

            StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("Socket ");

            if (socketPackEvent.readAndWrite == SocketMonitor.statusRead) {
                headerBuilder.append("response");
            } else {
                headerBuilder.append("request");
            }
            headerBuilder.append(" local port:").append(localPort)
                    .append(" remote address:").append(remoteAddress).append(":").append(remotePort)
                    .append(" isHttp:").append(socketPackEvent.isHttp)
                    .append("\n").append("StackTrace:");

            try {
                //输出头部数据
                fileOutputStream.write(headerBuilder.toString().getBytes(StandardCharsets.UTF_8));

                //输出堆栈
                PrintStream printStream = new PrintStream(fileOutputStream);
                socketPackEvent.stackTrace.printStackTrace(printStream);
                printStream.flush();
                printStream.write(newLineBytes);

                //输出报文内容
                if (!socketPackEvent.isHttp) {
                    printStream.write(socketPackEvent.body);
                } else {
                    //先写头部数据
                    printStream.write(socketPackEvent.httpHeaderContent);

                    //body数据，可能没有，而且可能存在分段压缩之类的
                    if (socketPackEvent.needDecodeHttpBody()) {
                        byte[] httpBodyContent = socketPackEvent.httpBodyContent;
                        if (socketPackEvent.charset != null && socketPackEvent.charset != StandardCharsets.UTF_8) {
                            httpBodyContent = new String(httpBodyContent, socketPackEvent.charset).getBytes(StandardCharsets.UTF_8);
                        }
                        printStream.write(httpBodyContent);

                    }
                }

                printStream.write(newLineBytes);

                printStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final byte[] newLineBytes = "\n\n".getBytes(StandardCharsets.UTF_8);
}
