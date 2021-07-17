package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;
import com.softsec.mobsec.dae.apimonitor.util.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class CryptoHook extends Hook {

    public static final String TAG = "Crypto";
    volatile Logger logger = new Logger();

    @Override
    public void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        Constructor secretKeySpecConstructor = Reflector.findConstructor(SecretKeySpec.class, byte[].class, String.class);
        MethodHookHandler.hookMethod(secretKeySpecConstructor, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    logger.addRelatedAttrs("SecretKeySpec", Util.byteArrayToString((byte[]) param.args[0]) + "," + (String) param.args[1]);
                }
            }
        });

        Method doFinalMethod = Reflector.findMethod(Cipher.class, "doFinal", byte[].class);
        MethodHookHandler.hookMethod(doFinalMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    logger.setTag(TAG);
                    logger.setCallingInfo(callingInfo[0]);
                    Field opmodeField = Cipher.class.getDeclaredField("opmode");
                    opmodeField.setAccessible(true);
                    int opmode = (int) opmodeField.get((Cipher)(param.thisObject));
                    logger.addRelatedAttrs("opmode", opmode == Cipher.ENCRYPT_MODE ? "Encrypt" : "Decrypt");
                    logger.addRelatedAttrs("output", Util.byteArrayToString((byte[]) param.getResult()));
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    logger.recordAPICalling(param, "加/解密行为", "input", Util.byteArrayToString((byte[]) param.args[0]));
                    XposedBridge.log("result:" + Util.byteArrayToString((byte[]) param.getResult()));
                }
            }
        });

        Method getIVMethod = Reflector.findMethod(Cipher.class, "getIV");
        MethodHookHandler.hookMethod(getIVMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    logger.addRelatedAttrs("IV", (String) param.getResult());
                }
            }
        });

        Constructor ivParameterSpecConstructor = Reflector.findConstructor(IvParameterSpec.class, byte[].class);
        MethodHookHandler.hookMethod(ivParameterSpecConstructor, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    logger.addRelatedAttrs("IV", Util.byteArrayToString((byte[]) param.args[0]));
                }
            }
        });

        Method setSeedMethod = Reflector.findMethod(SecureRandom.class, "setSeed", byte[].class);
        MethodHookHandler.hookMethod(setSeedMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    logger.addRelatedAttrs("seed", Util.byteArrayToString((byte[]) param.args[0]));
                }
            }
        });

        Method cipherGetInstanceMethod = Reflector.findMethod(Cipher.class, "getInstance", String.class);
        MethodHookHandler.hookMethod(cipherGetInstanceMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    logger.addRelatedAttrs("cipher mod", (String) param.args[0]);
                }
            }
        });

        Constructor pbeKeySpecConstructor = Reflector.findConstructor(PBEKeySpec.class, char[].class, byte[].class, int.class, int.class);
        MethodHookHandler.hookMethod(pbeKeySpecConstructor, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                synchronized (logger) {
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    logger.setTag(TAG);
                    logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("xrefFrom",callingInfo[1]);
                    logger.recordAPICalling(param, "PBE秘钥生成",
                            "password", String.valueOf((char[])param.args[0]),
                            "salt", Util.byteArrayToString((byte[])param.args[1]));
                }
            }
        });
    }
}
