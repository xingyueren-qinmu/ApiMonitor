package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.content.ContentResolver;
import android.provider.Settings;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SettingsHook extends Hook {

    public static final String TAG = "SettingsManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

        try {
            Method getStringMethod = Reflector.findMethod("android.provider.Settings$Secure",
                    packageParam.classLoader,
                    "getString",
                    ContentResolver.class, String.class);
            MethodHookHandler.hookMethod(getStringMethod, new MethodHookCallBack() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if(Settings.Secure.ANDROID_ID.equals((String)param.args[1])) {
                        String[] callingInfo = getCallingInfo(param.method.getName());
                        Logger logger = new Logger();
                        logger.setTag(TAG);
                        logger.setCallingInfo(callingInfo[0]);
                        logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                        logger.addRelatedAttrs("result", (String)param.getResult());
                        logger.recordAPICalling(param, "获取Android ID");
                    }
                }
            });
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            Logger.logError(e);
        }


    }
}
