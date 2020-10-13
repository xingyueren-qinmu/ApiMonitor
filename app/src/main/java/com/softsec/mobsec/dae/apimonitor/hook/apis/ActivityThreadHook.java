package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.annotation.SuppressLint;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ActivityThreadHook extends Hook {

	public static final String TAG = "DAEAM_ActivityThread";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);
		try {
			@SuppressLint("PrivateApi") Class receiverDataClass = Class.forName("android.app.ActivityThread$ReceiverData");
			Method handleReceiverMethod = Reflector.findCustomerMethod("android.app.ActivityThread", ClassLoader.getSystemClassLoader(),
					"handleReceiver", receiverDataClass);
			methodHookImpl.hookMethod(handleReceiverMethod, new MethodHookCallBack() {

				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					logger.recordAPICalling(param, "接收器");
				}
			});
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			logger.logError(e);
		}
	}
}
