package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import com.softsec.mobsec.dae.apimonitor.util.Config;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public abstract class MethodHookCallBack extends XC_MethodHook {

    protected MethodHookCallBack() {

    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    protected String getCallingInfo() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();

        Class clazz = null;
        for(StackTraceElement st : stackElements) {
            XposedBridge.log(st.getClassName());
            if(!st.getClassName().contains(Config.DAEAM_PKGNAME) || st.getClassName().contains("Xposed")) {
                if(null == clazz) {
                    clazz = st.getClass();
                } else {
                    if(st.getClass().getPackage().toString().startsWith(clazz.getPackage().toString())) {
                        return "";
                    }
                    break;
                }
            }
        }
        return clazz.getPackage().toString() + "---" + clazz.getName();
    }
}
