package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.location.Location;
import android.location.LocationManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LocationManagerHook extends Hook {
    public static final String TAG = "LocationManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

        Method getLastKnownLocationMethod = Reflector.findMethod(LocationManager.class, "getLastKnownLocation", String.class);
        MethodHookHandler.hookMethod(getLastKnownLocationMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location location = (Location)param.getResult();
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                String result =
                        location != null ?
                                "lat=" + location.getLatitude() + "&lng=" + location.getLongitude()
                                :
                                "";
                logger.addRelatedAttrs("result", result);
                logger.recordAPICalling(param, "获取地理位置", "provider", (String)param.args[0]);
            }
        });
    }
}
