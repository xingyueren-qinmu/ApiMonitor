package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.app.Notification;
import android.app.NotificationManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NotificationManagerHook extends Hook {

	public static final String TAG = "DAEAM_NotificationManager";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);
		Method notifyMethod = Reflector.findMethod(NotificationManager.class, "notify", int.class, Notification.class);
		methodHookImpl.hookMethod(notifyMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "发送通知",  "notification", ((Notification) param.args[1]).toString());
			}
		});
	}
}
