package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.app.ActivityManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ActivityManagerHook extends Hook {

	public static final String TAG = "DAEAM_ActivityManager:";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);

		Method killBackgroundProcessesmethod = Reflector.findMethod(ActivityManager.class, "killBackgroundProcesses", String.class);
		methodHookImpl.hookMethod(killBackgroundProcessesmethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "关闭程序", "killedprogram", (String)param.args[0]);
			}
		});

		Method forceStopPackagemethod = Reflector.findMethod(ActivityManager.class, "forceStopPackage", String.class);
		methodHookImpl.hookMethod(forceStopPackagemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "关闭程序","killedprogram", (String)param.args[0]);
			}
		});

	}
}
