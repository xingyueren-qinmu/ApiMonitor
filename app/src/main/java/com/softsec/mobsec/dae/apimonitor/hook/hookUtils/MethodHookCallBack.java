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
    }

    protected void printStackInfo() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if(stackElements != null){
            StackTraceElement st;
            for(int i=0; i<stackElements.length; i++){
                st = stackElements[i];
                if(st.getClassName().startsWith("com.android.monitor")||st.getClassName().startsWith("de.robv.android.xposed.XposedBridge"))
                    continue;
//                Logger.log_behavior(st.getClassName()+":"+st.getMethodName()+":"+st.getFileName()+":"+st.getLineNumber());
                XposedBridge.log(st.getClassName()+":"+st.getMethodName()+":"+st.getFileName()+":"+st.getLineNumber());
            }
        }
    }
}
