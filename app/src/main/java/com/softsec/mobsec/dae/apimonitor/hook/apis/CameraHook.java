package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CameraHook extends Hook {

	public static final String TAG = "DAEAM_Camera:";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		logger.setTag(TAG);
		Method takePictureMethod = Reflector.findMethod(Camera.class, "takePicture",
				ShutterCallback.class, PictureCallback.class, PictureCallback.class, PictureCallback.class);
		methodHookImpl.hookMethod(takePictureMethod, new MethodHookCallBack() {

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "拍摄照片");
			}

		});

		Method setPreviewCallbackMethod = Reflector.findMethod(Camera.class,
				"setPreviewCallback", PreviewCallback.class);
		methodHookImpl.hookMethod(setPreviewCallbackMethod, new MethodHookCallBack() {

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "拍摄视频");
			}
		});

		Method setPreviewCallbackWithBufferMethod = Reflector.findMethod(Camera.class,
				"setPreviewCallbackWithBuffer", PreviewCallback.class);
		methodHookImpl.hookMethod(setPreviewCallbackWithBufferMethod, new MethodHookCallBack() {

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "拍摄视频");
			}
		});

		Method setOneShotPreviewCallbackMethod = Reflector.findMethod(Camera.class,
				"setOneShotPreviewCallback", PreviewCallback.class);
		methodHookImpl.hookMethod(setOneShotPreviewCallbackMethod, new MethodHookCallBack() {

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "拍摄视频");
			}
		});
	}



}
