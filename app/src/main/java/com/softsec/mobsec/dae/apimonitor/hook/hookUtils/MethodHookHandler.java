package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import java.lang.reflect.Member;

import de.robv.android.xposed.XposedBridge;

public class MethodHookHandler {

    public static void hookMethod(Member method, MethodHookCallBack callback) {
        // TODO Auto-generated method stub
        XposedBridge.hookMethod(method, callback);
    }

    public static void hookAllConstructors(Class clazz, MethodHookCallBack callBack){
        XposedBridge.hookAllConstructors(clazz, callBack);
    }

    public static void hookAllMethods(Class clazz, String methodName, MethodHookCallBack callBack){
        XposedBridge.hookAllMethods(clazz, methodName, callBack);
    }


}