package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.webkit.CookieManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CookieManagerHook extends Hook {

    public static final String TAG = "DAEAM_CookieManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        logger.setTag(TAG);
//        Method getCookieMethod = null;
//        try {
//            getCookieMethod = Reflector.findMethod("android.webkit.CookieManager",
//                    packageParam.classLoader,
//                    "getCookie"
//                    );
//            methodHookImpl.hookMethod(getCookieMethod, new MethodHookCallBack() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    CookieManager cm = (CookieManager)param.getResult();
//                    logger.recordAPICalling(param, "获取Cookie",
//                            "URL", (String)param.args[0]);
//                }
//            });
//        } catch (NoSuchMethodException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }

    }
}
