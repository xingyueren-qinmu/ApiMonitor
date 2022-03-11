package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FileOutputStreamHook extends Hook {

    public static final String TAG = "写文件";

    public native String readlink(int fd);//jni方法

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        Method writeMethod = Reflector.findMethod(FileOutputStream.class, "write", byte[].class, int.class, int.class);
        MethodHookHandler.hookMethod(writeMethod, new MethodHookCallBack() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                FileOutputStream fos = (FileOutputStream) param.thisObject;
                fos.getFD();
                String path = null;
                if(Objects.isNull(path)) {
                    try {
                        FileDescriptor descriptor = fos.getFD();
                        int fd = Reflector.getFieldInt(FileDescriptor.class, descriptor, "descriptor");
                        path = readlink(fd);
                    } catch (Exception e) {

                    }
                }
                if(Objects.nonNull(path) && path.contains("DAEAM")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    Logger logger = new Logger();
                    logger.setTag(TAG);
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("xrefFrom",callingInfo[1]);
                    logger.recordAPICalling(param, "写文件", "content",
                            new String((byte[])param.args[0]));
                }
            }
        });
    }
}
