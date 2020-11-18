package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public abstract class MethodHookCallBack extends XC_MethodHook {
    private Logger logger;

    protected MethodHookCallBack(Logger logger){
        this.logger = logger;
    }

    protected MethodHookCallBack() {

    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        printStackInfo();
    }

    protected void printStackInfo() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if(stackElements != null){
            StackTraceElement st;
            for (StackTraceElement se : stackElements) {
//                if (se.getClassName().startsWith("com.android.monitor") || se.getClassName().startsWith("de.robv.android.xposed.XposedBridge")) {
//                    continue;
//                }
                XposedBridge.log(se.getClassName() + ":" + se.getMethodName() + ":" + se.getFileName() + ":" + se.getLineNumber());
            }
        }
    }
}
