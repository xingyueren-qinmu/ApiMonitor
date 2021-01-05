package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.Iterator;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ContextImplHook extends Hook {

	public static final String TAG = "DAEAM_ContextImpl";
	
	private String descIntentFilter(IntentFilter intentFilter) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> actions =intentFilter.actionsIterator();
		String action;
		while(actions.hasNext()){
			action = actions.next();
			sb.append(action).append(",");
		}
		String res = sb.toString();
		return res.endsWith(",") ? res.substring(0, res.length() - 1) : res;
	}

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		logger.setTag(TAG);
		try {
			Method registerReceiverMethod = Reflector.findMethod(
					"android.app.ContextImpl", ClassLoader.getSystemClassLoader(),
					"registerReceiver", BroadcastReceiver.class, IntentFilter.class);
			methodHookImpl.hookMethod(registerReceiverMethod, new MethodHookCallBack() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					if(param.args[1] != null){
						String intentstr = descIntentFilter((IntentFilter) param.args[1]);
						logger.recordAPICalling(param, "注册广播接收器","IntentFilter", intentstr);
					}
				}
			});
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			logger.logError(e);
		}
	}
}
