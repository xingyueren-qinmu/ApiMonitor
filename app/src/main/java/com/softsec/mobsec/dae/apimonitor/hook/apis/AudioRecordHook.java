package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.media.AudioRecord;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AudioRecordHook extends Hook {

	public static final String TAG = "AudioRecord";


	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

		Method startRecordingMethod = Reflector.findMethod(AudioRecord.class, "startRecording");
		MethodHookHandler.hookMethod(startRecordingMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				logger.recordAPICalling(param, "启动录音");
			}
		});
	}
}
