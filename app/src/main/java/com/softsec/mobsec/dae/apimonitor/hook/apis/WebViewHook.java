package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class WebViewHook extends Hook {

	public static final String TAG = "DAEAM_WebView";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);
		try {
			Method loadUrlMethod= Reflector.findMethod("android.webkit.WebView", ClassLoader.getSystemClassLoader(), "loadUrl", String.class);
			methodHookImpl.hookMethod(loadUrlMethod, new MethodHookCallBack(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					logger.recordAPICalling(param, "通过WebView加载URL", "URL", (String)param.args[0]);
				}
			});
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			logger.logError(e);
		}
	}
}
