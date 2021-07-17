package com.softsec.mobsec.dae.apimonitor.hook;

import android.os.RemoteException;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHide extends Hook {

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

        Method getStackTraceMethod = Reflector.findMethod(Thread.class, "getStackTrace");
        MethodHookHandler.hookMethod(getStackTraceMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("getStackTrace");
                StackTraceElement[] res = (StackTraceElement[]) param.getResult();
                int newLen = res.length;
                for(StackTraceElement se : res) {
                    if(se.getClassName().toLowerCase().contains("xposed")) {
                        newLen--;
                    }
                }
                StackTraceElement[] newRes = new StackTraceElement[newLen];
                newLen = 0;
                for(StackTraceElement se : res) {
                    if(se.getClassName().toLowerCase().contains("xposed")) {
                        continue;
                    }
                    newRes[newLen] = se;
                    newLen ++;
                }
                param.setResult(newRes);
            }
        });


        Method findClassMethod = Reflector.findMethod(ClassLoader.class, "findClass",
                String.class);
        MethodHookHandler.hookMethod(findClassMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("findClass");
                String className = (String)param.args[0];
                if(className.contains("Xposed")) {
                    param.setThrowable(new ClassNotFoundException(className));
                } else {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                }
            }
        });

        Method loadClassMethod = Reflector.findMethod(ClassLoader.class, "loadClass",
                String.class);
        MethodHookHandler.hookMethod(loadClassMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("loadClass");
                String className = (String)param.args[0];
                if(className.contains("Xposed")) {
                    XposedBridge.log("got:" + className);
                    param.setThrowable(new ClassNotFoundException(className));
                }
            }
        });

        try {

            String xposedServiceName = "user.xposed.system";
            Method getServiceMethod = Reflector.findMethod(
                    "android.os.ServiceManager",
                    packageParam.classLoader,
                    "getService",
                    String.class);
            MethodHookHandler.hookMethod(getServiceMethod, new MethodHookCallBack() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String serviceName = (String) param.args[0];
                    if (xposedServiceName.equals(serviceName)) {
                        XposedBridge.log("got:" + serviceName);
                        param.setThrowable(new RemoteException(serviceName));
                        param.setResult(null);
                    }
                }
            });


        } catch (NoSuchMethodException | ClassNotFoundException e) {
            Logger.logError(e);
        }


    }
}
