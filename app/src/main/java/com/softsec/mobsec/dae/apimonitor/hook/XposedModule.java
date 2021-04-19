package com.softsec.mobsec.dae.apimonitor.hook;

import android.os.Handler;
import android.os.Looper;

import com.softsec.mobsec.dae.apimonitor.hook.apis.*;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.HttpHook;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.NetStreamHook;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.OkHttpHook;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketMonitor;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketPackEvent;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.observer.EventObserver;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.util.Config;
import com.softsec.mobsec.dae.apimonitor.util.FileUtil;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * @author qinmu997
 */
public class XposedModule implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static XSharedPreferences xSP;
    public static final String TAG = "DAEAM_XposedModule";

    @Override
    public void initZygote(StartupParam startupParam) {
        xSP = new XSharedPreferences(Config.DAEAM_PKGNAME, Config.SP_NAME);
        xSP.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        xSP.reload();
        if(lpparam.packageName.equals(Config.DAEAM_PKGNAME)) {
            findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl",
                    lpparam.classLoader,
                    "notifyListeners",
                    "android.app.SharedPreferencesImpl.MemoryCommitResult",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(FileUtil::fixSharedPreference, 1000);
                        }
                    });
        }


        // no app to hook, return
        if(lpparam.packageName.equals(Config.DAEAM_PKGNAME)) {
            return;
        }

        String appToHookList = xSP.getString(Config.SP_APPS_TO_HOOK, null);
        if (appToHookList != null) {
            if(!appToHookList.contains(lpparam.packageName)) {
                return;
            }
        } else {
            return;
        }

        // logPath
        String absolutePath = xSP.getString(lpparam.packageName + Config.SP_TARGET_APP_LOG_DIR, null);
        XposedBridge.log(absolutePath);

        // Need access to the files
        File folder = new File(xSP.getString(lpparam.packageName + Config.SP_TARGET_APP_DIR, null));
        folder.setExecutable(true, false);

        findAndHookMethod("android.util.Log", lpparam.classLoader, "i",
                String.class, String.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if ("Xposed".equals(param.args[0])) {
                            String log = (String) param.args[1];
                            if(log.contains("DAEAM_") && !log.contains("DAEAM_ERROR")) {
                                FileUtil.writeToFile(log.replace("DAEAM_", ""), absolutePath);
                            }
                        }
                    }
                });
        startAllHooks(lpparam);
    }
    
    private void setupSocketMonitor(XC_LoadPackage.LoadPackageParam lpparam) {
        SocketMonitor.setPacketEventObserver(socketPackEvent -> {
            int localPort = socketPackEvent.socket.getLocalPort();
            int desport = socketPackEvent.socket.getPort();
            InetAddress inetAddress = socketPackEvent.socket.getInetAddress();

            String desip;
            if (inetAddress != null) {
                desip = inetAddress.getHostAddress();
            } else {
                desip = socketPackEvent.socket.toString();
            }

            Logger logger = new Logger();
            logger.setTag("Socket Hook");
            StringBuilder headerBuilder = new StringBuilder();
            String type = socketPackEvent.readAndWrite == SocketMonitor.statusRead ? "response" : "request";
            logger.addRelatedAttrs("type", type);
            logger.addRelatedAttrs("desip", desip);
            logger.addRelatedAttrs("desport", String.valueOf(desport));
            logger.addRelatedAttrs("protocal", socketPackEvent.isHttp ? "HTTP" : "OTHER");

            try {
                XposedBridge.log(headerBuilder.toString());
                if (!socketPackEvent.isHttp) {
//                        printStream.write(socketPackEvent.body);
//                    logger.addRelatedAttrs(type + "_raw", new String(socketPackEvent.body));
                } else {
                    //先写头部数据
                    logger.addRelatedAttrs("header", new String(socketPackEvent.httpHeaderContent));

                    //body数据，可能没有，而且可能存在分段压缩之类的
                    if (socketPackEvent.needDecodeHttpBody()) {
                        byte[] httpBodyContent = socketPackEvent.httpBodyContent;
                        if (socketPackEvent.charset != null && socketPackEvent.charset != StandardCharsets.UTF_8) {
                            httpBodyContent = new String(httpBodyContent, socketPackEvent.charset).getBytes(StandardCharsets.UTF_8);
                        }
                        XposedBridge.log(new String(httpBodyContent));


                    }
                }
                XposedBridge.log("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.recordAPICalling("Socket通信");
        });
    }


    private void startAllHooks(XC_LoadPackage.LoadPackageParam lpparam) {
        new XposedHide().initAllHooks(lpparam);

        new CryptoHook().initAllHooks(lpparam);
//        new FileSystemHook().initAllHooks(lpparam);
        new IPCHook().initAllHooks(lpparam);
        new HttpHook().initAllHooks(lpparam);
        new OkHttpHook().initAllHooks(lpparam);
        new AccountManagerHook().initAllHooks(lpparam);
        new CameraHook().initAllHooks(lpparam);
        new TelephonyManagerHook().initAllHooks(lpparam);
        new ActivityManagerHook().initAllHooks(lpparam);
        new ActivityThreadHook().initAllHooks(lpparam);
        new AudioRecordHook().initAllHooks(lpparam);
        new ContentResolverHook().initAllHooks(lpparam);
        new ContextImplHook().initAllHooks(lpparam);
        new LocationManagerHook().initAllHooks(lpparam);
        new MediaRecorderHook().initAllHooks(lpparam);
        new NotificationManagerHook().initAllHooks(lpparam);
        new PackageManagerHook().initAllHooks(lpparam);
        new ProcessHook().initAllHooks(lpparam);
        new RuntimeHook().initAllHooks(lpparam);
        new SmsManagerHook().initAllHooks(lpparam);
        new WebViewHook().initAllHooks(lpparam);
        new CookieManagerHook().initAllHooks(lpparam);
        new NetInfoHook().initAllHooks(lpparam);
        new SensorManagerHook().initAllHooks(lpparam);
        new SettingsHook().initAllHooks(lpparam);
        new NetStreamHook().initAllHooks(lpparam);
        setupSocketMonitor(lpparam);
    }
}

