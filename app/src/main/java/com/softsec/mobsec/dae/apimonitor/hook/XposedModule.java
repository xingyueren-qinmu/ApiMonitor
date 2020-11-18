package com.softsec.mobsec.dae.apimonitor.hook;

import android.os.Handler;
import android.os.Looper;

import com.softsec.mobsec.dae.apimonitor.hook.apis.*;
import com.softsec.mobsec.dae.apimonitor.util.Config;
import com.softsec.mobsec.dae.apimonitor.util.FileUtil;

import java.io.File;

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

        String pkgToHookString = xSP.getString(Config.SP_APPS_TO_HOOK, null);
        if (pkgToHookString != null) {
            if(!pkgToHookString.contains(lpparam.packageName)) {
                return;
            }
        } else {
            return;
        }

        // logPath
        String absolutePath = xSP.getString(lpparam.packageName + Config.SP_TARGET_APP_LOG_DIR, null);
        XposedBridge.log(absolutePath);

        //Need access to the files
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

    private void startAllHooks(XC_LoadPackage.LoadPackageParam lpparam) {
//        new CryptoHook().initAllHooks(lpparam);
//        new FileSystemHook().initAllHooks(lpparam);
        new IPCHook().initAllHooks(lpparam);
//        new HttpHook().initAllHooks(lpparam);
        new AccountManagerHook().initAllHooks(lpparam);
//        new AccountManagerHook().initAllHooks(lpparam);
//        new CameraHook().initAllHooks(lpparam);
        new TelephonyManagerHook().initAllHooks(lpparam);
//        new ActivityManagerHook().initAllHooks(lpparam);
//        new ActivityThreadHook().initAllHooks(lpparam);
//        new AudioRecordHook().initAllHooks(lpparam);
//        new ContentResolverHook().initAllHooks(lpparam);
//        new ContextImplHook().initAllHooks(lpparam);
        new LocationManagerHook().initAllHooks(lpparam);
//        new MediaRecorderHook().initAllHooks(lpparam);
//        new NotificationManagerHook().initAllHooks(lpparam);
//        new PackageManagerHook().initAllHooks(lpparam);
//        new ProcessHook().initAllHooks(lpparam);
//        new RuntimeHook().initAllHooks(lpparam);
//        new SmsManagerHook().initAllHooks(lpparam);
//        new WebViewHook().initAllHooks(lpparam);
    }
}

