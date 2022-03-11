package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TelephonyManagerHook extends Hook {

	public static final String TAG = "TelephonyManager";
	public static Map<String, String> map = new HashMap<>((int)Math.ceil(10 / 0.75));

	static {
		map.put("46001", "中国联通");
		map.put("46006", "中国联通");
		map.put("46009", "中国联通");
		map.put("46000", "中国移动");
		map.put("46002", "中国移动");
		map.put("46004", "中国移动");
		map.put("46007", "中国移动");
		map.put("46003", "中国电信");
		map.put("46005", "中国电信");
		map.put("46011", "中国电信");
	}

	@Override
	public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {


		Method getLine1Numbermethod = Reflector.findMethod(TelephonyManager.class, "getLine1Number");
		MethodHookHandler.hookMethod(getLine1Numbermethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.addRelatedAttrs("result", (String)param.getResult());
				logger.recordAPICalling(param, "获取本机号码");
			}
		});

		Method listenMethod = Reflector.findMethod(TelephonyManager.class,
				"listen", PhoneStateListener.class,int.class);
		MethodHookHandler.hookMethod(listenMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				int eventNo =  (Integer) param.args[1];
				String eventStr = "null";
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
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "监听手机信息",
						"PhoneStateListener", param.args[0].getClass().getName(),
						"ListeningEvent", eventStr);
			}
		});

		//　获取设备id
		Method telphonyManager_getDeviceIdMethod = Reflector.findMethod(TelephonyManager.class, "getDeviceId");
		MethodHookHandler.hookMethod(telphonyManager_getDeviceIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.addRelatedAttrs("result", (String)param.getResult());
				logger.recordAPICalling(param, "获取设备ID");
			}
		});


		// 获取IMSI
		Method telphonyManager_getSubscriberIdMethod = Reflector.findMethod(TelephonyManager.class, "getSubscriberId");
		MethodHookHandler.hookMethod(telphonyManager_getSubscriberIdMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.addRelatedAttrs("result", (String)param.getResult());
				logger.recordAPICalling(param, "获取Sim卡MSI");
			}
		});

		// 获取手机位置
		Method telphonyManager_getCellLocationMethod = Reflector.findMethod(TelephonyManager.class, "getCellLocation");
		MethodHookHandler.hookMethod(telphonyManager_getCellLocationMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				CellLocation location = (CellLocation)param.getResult();
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "获取基站位置");
			}
		});

		// 获取系统ID
		Method cdmaLocation_getSystemId = Reflector.findMethod(CdmaCellLocation.class, "getSystemId");
		MethodHookHandler.hookMethod(cdmaLocation_getSystemId, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("result", String.valueOf((int)param.getResult()));
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "获取系统ID");
			}
		});


		// 获取网络ID
		Method cdmaLocation_getNetworkId = Reflector.findMethod(CdmaCellLocation.class, "getNetworkId");
		MethodHookHandler.hookMethod(cdmaLocation_getNetworkId, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("result", String.valueOf((int)param.getResult()));
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "获取网络服务种类");
			}
		});

		// 获取基站ID
		Method cdmaLocation_getBaseStationId = Reflector.findMethod(CdmaCellLocation.class, "getBaseStationId");
		MethodHookHandler.hookMethod(cdmaLocation_getBaseStationId, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("result", String.valueOf((int)param.getResult()));
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "获取基站ID");
			}
		});

		// 获得cell id
		Method gsmLocation_getCidMethod = Reflector.findMethod(GsmCellLocation.class, "getCid");
		MethodHookHandler.hookMethod(gsmLocation_getCidMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("result", String.valueOf((int)param.getResult()));
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "获取GSM原件ID");
			}
		});


		//获得gsm地区代号
		Method gsmLocation_getLacMethod = Reflector.findMethod(GsmCellLocation.class, "getLac");
		MethodHookHandler.hookMethod(gsmLocation_getLacMethod, new MethodHookCallBack() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				String[] callingInfo = getCallingInfo(param.method.getName());
				Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
				logger.addRelatedAttrs("result", String.valueOf((int)param.getResult()));
				logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
				logger.recordAPICalling(param, "获取地理位置");
			}
		});

		// 获得运营商信息
		Method getSimCarrierIdMethod = Reflector.findMethod(TelephonyManager.class, "getSimCarrierId");
		if(getSimCarrierIdMethod != null) {
			MethodHookHandler.hookMethod(getSimCarrierIdMethod, new MethodHookCallBack() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					String[] callingInfo = getCallingInfo(param.method.getName());
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("result", String.valueOf((int)param.getResult()));
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					logger.recordAPICalling(param, "获取运营商");
				}
			});
		}

		// 获得运营商信息
		if(Build.VERSION.SDK_INT >= 28) {
			Method getSimCarrierIdNameMethod = Reflector.findMethod(TelephonyManager.class, "getSimCarrierIdName");
			if(getSimCarrierIdNameMethod != null) {
				MethodHookHandler.hookMethod(getSimCarrierIdNameMethod, new MethodHookCallBack() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						super.afterHookedMethod(param);
						String[] callingInfo = getCallingInfo(param.method.getName());
						Logger logger = new Logger();
						logger.setTag(TAG);
						logger.setCallingInfo(callingInfo[0]);
						logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
						logger.recordAPICalling(param, "获取运营商");
					}
				});
			}
		}

		try {
			Method getSimOperatorMethod = Reflector.findMethod(TelephonyManager.class, "getSimOperator");
			MethodHookHandler.hookMethod(getSimOperatorMethod, new MethodHookCallBack() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					String[] callingInfo = getCallingInfo(param.method.getName());
					Logger logger = new Logger();
					logger.setTag(TAG);
					logger.setCallingInfo(callingInfo[0]);
					logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
					String operator = map.get((String)param.getResult());
					logger.addRelatedAttrs("result", null == operator ? "unknown" : operator);
					logger.recordAPICalling(param, "获取运营商");
				}
			});
		} catch (Exception e) {
			Logger.logError(e);
		}
	}
}
