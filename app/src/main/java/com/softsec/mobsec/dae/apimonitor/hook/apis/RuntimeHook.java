package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.File;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RuntimeHook extends Hook {

	public static final String TAG = "DAEAM_Runtime";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);
		Method execmethod = Reflector.findMethod(
				Runtime.class, "exec", String[].class, String[].class, File.class);
		methodHookImpl.hookMethod(execmethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] progs = (String[]) param.args[0];
				String[] logs = new String[progs.length * 2];
				for(int i = 0 ;i < progs.length; i++) {
					logs[i * 2] = "Command" + i;
					logs[i * 2 + 1] = progs[i];
				}
				logger.recordAPICalling(param, "执行命令", logs);

			}
		});
	}
}
