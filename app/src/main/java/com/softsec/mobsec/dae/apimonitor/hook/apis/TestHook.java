package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.util.Log;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;
import com.softsec.mobsec.dae.apimonitor.util.FileUtil;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TestHook extends Hook {

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        Method iMethod = Reflector.findMethod(Log.class, "w", String.class, String.class);
        MethodHookHandler.hookMethod(iMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log((String)param.args[0] + ":" + (String)param.args[1]);
                FileUtil.writeToFile((String)param.args[1], "/sdcard/Android/data/com.cleanmaster.mguard_cn/xposedLog");
            }
        });
    }
}
