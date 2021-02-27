package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.util.Base64;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class SmsManagerHook extends Hook {

	public static final String TAG = "DAEAM_SMSManager";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);

		Method sendTextMessagemethod = Reflector.findMethod(SmsManager.class,
				"sendTextMessage", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
		methodHookImpl.hookMethod(sendTextMessagemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String dstNumber = (String)param.args[0];
				String content = (String)param.args[2];
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "发送信息", "destNumber", dstNumber, "content", content);
			}
		});
		Method getAllMessagesFromIccmethod = Reflector.findMethod(SmsManager.class, "getAllMessagesFromIcc");
		methodHookImpl.hookMethod(getAllMessagesFromIccmethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "读取短信");
			}
		});

		Method sendDataMessagemethod = Reflector.findMethod(SmsManager.class,
				"sendDataMessage", String.class, String.class,short.class,byte[].class, PendingIntent.class, PendingIntent.class);
		methodHookImpl.hookMethod(sendDataMessagemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String dstNumber = (String)param.args[0];
				Short port = (Short)param.args[2];
				String content = Base64.encodeToString((byte[]) param.args[3],0);
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "发送信息", "destNumber", dstNumber, "destPort", port.toString(), "b64Content", content);

			}
		});

		Method sendMultipartTextMessagemethod = Reflector.findMethod(SmsManager.class,
				"sendMultipartTextMessage", String.class, String.class, ArrayList.class, ArrayList.class, ArrayList.class);
		methodHookImpl.hookMethod(sendMultipartTextMessagemethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				String dstNumber = (String)param.args[0];
				ArrayList<String> sms = (ArrayList<String>) param.args[2];
				StringBuilder sb = new StringBuilder();
				for(int i=0; i<sms.size(); i++)
					sb.append(sms.get(i));
				String[] callingInfo = getCallingInfo();
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "发送信息", "destNumber", dstNumber, "SMSContent", sb.toString());
			}
		});
	}

}
