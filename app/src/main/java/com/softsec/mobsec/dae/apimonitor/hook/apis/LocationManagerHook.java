package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.location.Location;
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
                Location location = (Location)param.getResult();
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                String result = "lat=" + location.getLatitude() +
                        "&lng=" + location.getLongitude();
                logger.addRelatedAttrs("result", result);
                logger.recordAPICalling(param, "获取地理位置", "provider", (String)param.args[0]);
            }
        });
    }
}
