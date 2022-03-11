package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.util.Log;
import android.webkit.CookieManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CookieManagerHook extends Hook {

    public static final String TAG = "CookieManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {


        try {
            Method getInstanceMethod = Reflector.findMethod(CookieManager.class, "getInstance");
            MethodHookHandler.hookMethod(getInstanceMethod, new MethodHookCallBack() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Class cmCLass = param.getResult().getClass();
                    Method getCookieMethod = cmCLass.getDeclaredMethod("getCookie", String.class);
                    MethodHookHandler.hookMethod(getCookieMethod, new MethodHookCallBack() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String[] callingInfo = getCallingInfo(param.method.getName());
                            Logger logger = new Logger();
                            logger.setTag(TAG);
                            logger.setCallingInfo(callingInfo[0]);
                            logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                            logger.recordAPICalling(param, "获取Cookie",
                                    "url", (String)param.args[0]);
                        }
                    });
                }
            });
        } catch (Exception e) {
            Logger.logError(e);
        }
    }
}
