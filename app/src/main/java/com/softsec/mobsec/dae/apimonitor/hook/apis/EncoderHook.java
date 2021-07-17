package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.util.Base64;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;
import com.softsec.mobsec.dae.apimonitor.util.Util;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class EncoderHook extends Hook {

    public static final String TAG = "Base64";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

        Method encodeMethod1 = Reflector.findMethod(Base64.class, "encode", byte[].class, int.class);
        MethodHookHandler.hookMethod(encodeMethod1, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", Util.byteArrayToString((byte[])param.getResult()));
                logger.recordAPICalling(param, "Base64编码",
                        "input", Util.byteArrayToString((byte[])param.args[0]),
                        "mode", String.valueOf((int)param.args[1]));
            }
        });

        Method encodeMethod2 = Reflector.findMethod(Base64.class,"encode", byte[].class, int.class, int.class, int.class);
        MethodHookHandler.hookMethod(encodeMethod2, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", Util.byteArrayToString((byte[])param.getResult()));
                int from = (int) param.args[1], to = (int) param.args[2];
                byte[] input = new byte[to - from];
                System.arraycopy((byte[])param.args[0], from, input, 0, to - from);
                logger.recordAPICalling(param, "Base64编码",
                        "input", Util.byteArrayToString(input),
                        "mode", String.valueOf((int)param.args[3]));
            }
        });

        Method encodeToStringMethod1 = Reflector.findMethod(Base64.class, "encodeToString", byte[].class, int.class);
        MethodHookHandler.hookMethod(encodeToStringMethod1, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", (String) param.getResult());
                logger.recordAPICalling(param, "Base64编码",
                        "input", Util.byteArrayToString((byte[])param.args[0]),
                        "mode", String.valueOf((int)param.args[1]));
            }
        });

        Method encodeToStringMethod2 = Reflector.findMethod(Base64.class, "encodeToString", byte[].class, int.class, int.class, int.class);
        MethodHookHandler.hookMethod(encodeToStringMethod2, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", Util.byteArrayToString((byte[])param.getResult()));
                int from = (int) param.args[1], to = (int) param.args[2];
                byte[] input = new byte[to - from];
                System.arraycopy((byte[])param.args[0], from, input, 0, to - from);
                logger.recordAPICalling(param, "Base64编码",
                        "input", Util.byteArrayToString(input),
                        "mode", String.valueOf(param.args[3]));
            }
        });

        Method decodeMethod1 = Reflector.findMethod(Base64.class, "decode", String.class, int.class);
        MethodHookHandler.hookMethod(decodeMethod1, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", Util.byteArrayToString((byte[])param.getResult()));
                logger.recordAPICalling(param, "Base64解码",
                        "input", (String)param.args[0],
                        "mode", String.valueOf(param.args[1]));
            }
        });

        Method decodeMethod2 = Reflector.findMethod(Base64.class,"decode", byte[].class, int.class, int.class, int.class);
        MethodHookHandler.hookMethod(decodeMethod2, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", Util.byteArrayToString((byte[])param.getResult()));
                int from = (int) param.args[1], to = (int) param.args[2];
                byte[] input = new byte[to - from];
                System.arraycopy((byte[])param.args[0], from, input, 0, to - from);
                logger.recordAPICalling(param, "Base64解码",
                        "input", Util.byteArrayToString(input),
                        "mode", String.valueOf(param.args[3]));
            }
        });


        Method decodeMethod3 = Reflector.findMethod(Base64.class, "decode", byte[].class, int.class);
        MethodHookHandler.hookMethod(decodeMethod3, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xref", callingInfo[1]);
                logger.addRelatedAttrs("output", Util.byteArrayToString((byte[])param.getResult()));
                logger.recordAPICalling(param, "Base64解码",
                        "input", Util.byteArrayToString((byte[])param.args[0]),
                        "mode", String.valueOf((int)param.args[1]));
            }
        });

    }
}
