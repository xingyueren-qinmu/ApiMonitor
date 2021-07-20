package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.media.MediaRecorder;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.FileDescriptor;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MediaRecorderHook extends Hook {
	public static final String TAG = "MediaRecorder";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		Method startMethod = Reflector.findMethod(MediaRecorder.class,  "start");
		MethodHookHandler.hookMethod(startMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);

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
		MethodHookHandler.hookMethod(stopMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "结束录音");
			}
		});
	}
}
