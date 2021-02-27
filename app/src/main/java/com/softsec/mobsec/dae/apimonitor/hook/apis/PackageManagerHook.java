package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.content.pm.PackageInfo;
import android.net.Uri;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PackageManagerHook extends Hook {

	public static final String TAG = "DAEAM_PackageManager";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);

		try {
			Method installPackagemethod = Reflector.findMethod("android.app.ApplicationPackageManager", ClassLoader.getSystemClassLoader(),
					"installPackage", Uri.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
			methodHookImpl.hookMethod(installPackagemethod, new MethodHookCallBack() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Uri uri = (Uri) param.args[0];
					String[] callingInfo = getCallingInfo();
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.recordAPICalling(param, "安装应用", "installAPK", uri.toString());
				}
			});

			Method getInstalledPackagesMethod = Reflector.findMethod("android.app.ApplicationPackageManager",
					ClassLoader.getSystemClassLoader(), "getInstalledPackages", int.class, int.class);
			methodHookImpl.hookMethod(getInstalledPackagesMethod, new MethodHookCallBack() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					String[] callingInfo = getCallingInfo();
					StringBuilder sb = new StringBuilder();
					List<PackageInfo> pkgs = (List<PackageInfo>) param.getResult();
					for(PackageInfo pkg : pkgs) {
						sb.append(pkg.packageName).append(',');
					}
					sb.deleteCharAt(sb.length() - 1);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.addRelatedAttrs("return", sb.toString());
					logger.recordAPICalling(param, "获取本机安装应用");
				}
			});

		} catch (ClassNotFoundException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			logger.logError(e);
		}



	}
}
