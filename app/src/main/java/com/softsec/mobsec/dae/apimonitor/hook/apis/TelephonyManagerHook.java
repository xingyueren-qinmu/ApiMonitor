package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TelephonyManagerHook extends Hook {

	public static final String TAG = "DAEAM_TelephonyManager";

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
		logger.setTag(TAG);

		Method getLine1Numbermethod = Reflector.findMethod(TelephonyManager.class, "getLine1Number");
		methodHookImpl.hookMethod(getLine1Numbermethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取本机号码");
			}
		});

		Method listenMethod = Reflector.findMethod(TelephonyManager.class,
				"listen", PhoneStateListener.class,int.class);
		methodHookImpl.hookMethod(listenMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int eventNo =  (Integer) param.args[1];
				String eventStr = null;
				if((eventNo & PhoneStateListener.LISTEN_CELL_LOCATION) != 0){
					eventStr = "LISTEN_CELL_LOCATION";
				}
				if((eventNo & PhoneStateListener.LISTEN_SIGNAL_STRENGTHS) != 0){
					eventStr = "LISTEN_SIGNAL_STRENGTHS";
				}
				if((eventNo & PhoneStateListener.LISTEN_CALL_STATE) != 0){
					eventStr = "LISTEN_CALL_STATE";
				}
				if((eventNo & PhoneStateListener.LISTEN_DATA_CONNECTION_STATE) != 0){
					eventStr = "LISTEN_DATA_CONNECTION_STATE";
				}
				if((eventNo & PhoneStateListener.LISTEN_CELL_LOCATION) != 0){
					eventStr = "LISTEN_SERVICE_STATE";
				}
				logger.recordAPICalling(param, "监听手机信息",
						"PhoneStateListener", param.args[0].getClass().getName(),
						"ListeningEvent", eventStr);
			}
		});

		//　获取设备id
		Method telphonyManager_getDeviceIdMethod = Reflector.findMethod(TelephonyManager.class, "getDeviceId");
		methodHookImpl.hookMethod(telphonyManager_getDeviceIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取设备ID");
			}
		});


		// 获取IMSI
		Method telphonyManager_getSubscriberIdMethod = Reflector.findMethod(TelephonyManager.class, "getSubscriberId");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取配置文件的唯一标识符ID");
			}
		});

		// 获取手机位置
		Method telphonyManager_getCellLocationMethod = Reflector.findMethod(TelephonyManager.class, "getCellLocation");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取基站位置信息");
			}
		});

		// 获取系统ID
		Method cdmaLocation_getSystemId = Reflector.findMethod(CdmaCellLocation.class, "getSystemId");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取系统ID");
			}
		});


		// 获取网络ID
		Method cdmaLocation_getNetworkId = Reflector.findMethod(CdmaCellLocation.class, "getNetworkId");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取网络服务种类");
			}
		});

		// 获取基站ID
		Method cdmaLocation_getBaseStationId = Reflector.findMethod(CdmaCellLocation.class, "getBaseStationId");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取基站ID");
			}
		});

		// 获得cell id
		Method gsmLocation_getCidMethod = Reflector.findMethod(GsmCellLocation.class, "getCid");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取GSM原件ID");
			}
		});


		//获得gsm地区代号
		Method gsmLocation_getLacMethod = Reflector.findMethod(GsmCellLocation.class, "getLac");
		methodHookImpl.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				logger.recordAPICalling(param, "获取地理位置信息");
			}
		});
	}
}
