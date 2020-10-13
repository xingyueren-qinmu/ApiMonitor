package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.media.MediaRecorder;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.FileDescriptor;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MediaRecorderHook extends Hook {
	public static final String TAG = "DAEAM_MediaRecorder";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);
		Method startMethod = Reflector.findMethod(MediaRecorder.class,  "start");
		methodHookImpl.hookMethod(startMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String mPath = (String)Reflector.getFieldOjbect(MediaRecorder.class, param.thisObject, "mPath");
				if(mPath != null) logger.recordAPICalling(param, "path", mPath);
				else {
					FileDescriptor mFd = (FileDescriptor) Reflector.getFieldOjbect(MediaRecorder.class, param.thisObject, "mFd");
					assert mFd != null;
					logger.recordAPICalling(param, "开始录音", "path", mFd.toString());
				}
			}
		});

		Method stopMethod = Reflector.findMethod(MediaRecorder.class, "stop");
		methodHookImpl.hookMethod(stopMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "结束录音");
			}
		});
	}
}
