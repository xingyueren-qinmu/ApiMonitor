package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.File;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RuntimeHook extends Hook {

	public static final String TAG = "Runtime";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		Method execmethod = Reflector.findMethod(
				Runtime.class, "exec", String[].class, String[].class, File.class);
		MethodHookHandler.hookMethod(execmethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] progs = (String[]) param.args[0];
				String[] logs = new String[progs.length * 2];
				for(int i = 0 ;i < progs.length; i++) {
					logs[i * 2] = "Command" + i;
					logs[i * 2 + 1] = progs[i];
				}
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "执行命令", logs);
			}
		});
	}
}
