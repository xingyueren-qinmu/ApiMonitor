package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class WebViewHook extends Hook {

	public static final String TAG = "WebView";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		try {
			Method loadUrlMethod= Reflector.findMethod("android.webkit.WebView",
					packageParam.classLoader, "loadUrl", String.class);
			MethodHookHandler.hookMethod(loadUrlMethod, new MethodHookCallBack(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.recordAPICalling(param, "通过WebView加载URL", "URL", (String)param.args[0]);
				}
			});
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			Logger.logError(e);
		}
	}
}
