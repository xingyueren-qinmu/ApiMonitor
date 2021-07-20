package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.os.Build;
import android.util.Log;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OthersHook extends Hook {
    public static final String TAG = "TelephonyManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {


        try {
            Method getSerialMethod = Reflector.findMethod(Build.class, "getSerial");
            MethodHookHandler.hookMethod(getSerialMethod, new MethodHookCallBack() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
                    logger.setTag(TAG);
                    logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    String serial = (String)param.getResult();
                    logger.addRelatedAttrs("serial", null == serial ? "" : serial);
                    logger.recordAPICalling(param, "获取运营商");
                }
            });
        } catch (Exception e) {
            Logger.logError(e);
        }

        Method getStringMethod = Reflector.findMethod(Build.class, "getString", String.class);
        MethodHookHandler.hookMethod(getStringMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if("ro.product.brand".equals(param.args[0])) {
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
                    logger.setTag(TAG);
                    logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    String brand = (String)param.getResult();
                    logger.addRelatedAttrs("brand", null == brand ? "" : brand);
                    logger.recordAPICalling(param, "获取设备名称");
                }
            }
        });
    }

}
