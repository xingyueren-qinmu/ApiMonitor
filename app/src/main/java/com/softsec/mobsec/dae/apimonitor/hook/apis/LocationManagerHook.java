package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.location.LocationManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LocationManagerHook extends Hook {
    public static final String TAG = "DAEAM_LocationManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        logger.setTag(TAG);
        Method getLastKnownLocationMethod = Reflector.findMethod(LocationManager.class, "getLastKnownLocation", String.class);
        methodHookImpl.hookMethod(getLastKnownLocationMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                logger.recordAPICalling(param, "地理位置获取", "provider", (String)param.args[0]);
            }
        });
    }
}
