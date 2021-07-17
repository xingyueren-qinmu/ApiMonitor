package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.content.ContextWrapper;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FileSystemHook extends Hook {

    public static final String TAG = "FileSystem";

    @Override
    public void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {


        Method openFileOutputMethod = Reflector.findMethod(ContextWrapper.class, "openFileOutput", String.class, "int");
        MethodHookHandler.hookMethod(openFileOutputMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String name = (String) param.args[0];
                int mode = (int) param.args[1];

                if (name.contains("DAEAM")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    String m;
                    switch (mode) {
                        case android.content.Context.MODE_PRIVATE:
                            m = "MODE_PRIVATE";
                            break;
                        case android.content.Context.MODE_APPEND:
                            m = "MODE_APPEND";
                            break;
                        default:
                            m = "?";
                    }
                    Logger logger = new Logger();
                    logger.setTag(TAG);
                    logger.recordAPICalling(param, "输出到文件", "filename", name, "mod", m);
                }
            }
        });

        Constructor fileConstructor1 = Reflector.findConstructor(File.class, String.class);
        MethodHookHandler.hookMethod(fileConstructor1, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String filePath = (String) param.args[0];
                if (filePath.contains("DAEAM")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else
                    if(((File)param.thisObject).isFile()) {
                        String[] callingInfo = getCallingInfo(param.method.getName());
                        Logger logger = new Logger();
                        logger.setTag(TAG);
                        logger.setCallingInfo(callingInfo[0]);
                        logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                        logger.recordAPICalling(param, "打开文件", "filepath", filePath);
                    }
            }
        });


        Constructor fileConstructor2 = Reflector.findConstructor(File.class, String.class, String.class);
        MethodHookHandler.hookMethod(fileConstructor2, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String filedir = (String) param.args[0];
                String fileName = (String) param.args[1];
                if (filedir.contains("DAEAM") || fileName.contains("DAEAM")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else
                     if(((File)param.thisObject).isFile()) {
                         String[] callingInfo = getCallingInfo(param.method.getName());
                         Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                         logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                         logger.recordAPICalling(param, "打开文件", "filedir", filedir, "filename", fileName);
                     }
            }
        });

        Constructor fileConstructor3 = Reflector.findConstructor(File.class, File.class, String.class);
        MethodHookHandler.hookMethod(fileConstructor3, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                File fileDir = (File) param.args[0];
                String fileName = (String) param.args[1];
                if(fileDir != null) {
                    if (fileDir.getAbsolutePath().contains("DAEAM") || fileName.contains("DAEAM")) {
                        XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } else if(((File)param.thisObject).isFile()) {
                        String[] callingInfo = getCallingInfo(param.method.getName());
                        Logger logger = new Logger();
                        logger.setTag(TAG);
                        logger.setCallingInfo(callingInfo[0]);
                        logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                        logger.recordAPICalling(param, "打开文件",
                            "filedir", fileDir.getAbsolutePath(),
                                    "filename", fileName);
                    }
                }
            }
        });


        Constructor fileConstructor4 = Reflector.findConstructor(File.class, URI.class);
        MethodHookHandler.hookMethod(fileConstructor4, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                URI uri = (URI) param.args[0];
                if(uri!=null) {
                    if (uri.toString().contains("DAEAM")) {
                        XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } else if(((File)param.thisObject).isFile()) {
                        String[] callingInfo = getCallingInfo(param.method.getName());
                        Logger logger = new Logger();
                        logger.setTag(TAG);
                        logger.setCallingInfo(callingInfo[0]);
                        logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                        logger.recordAPICalling(param, "打开文件", "uri", uri.toString());
                    }
                }
            }
        });
    }
}
