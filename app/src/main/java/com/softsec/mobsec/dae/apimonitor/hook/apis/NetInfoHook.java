package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NetInfoHook extends Hook {

    public static final String TAG = "DAEAM_NetInfo";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        logger.setTag(TAG);
        Method getMacAddressMethod = Reflector.findMethod(WifiInfo.class, "getMacAddress");
        methodHookImpl.hookMethod(getMacAddressMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("MAC地址", (String)(param.getResult()));
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.recordAPICalling(param, "获取MAC地址");
            }
        });

        Method getIpAddressMethod = Reflector.findMethod(WifiInfo.class, "getIpAddress");
        methodHookImpl.hookMethod(getIpAddressMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("WIFI IP", String.valueOf((int)(param.getResult())));
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.recordAPICalling(param, "获取WIFI IP");
            }
        });

        Method getSSIDMethod = Reflector.findMethod(WifiInfo.class, "getSSID");
        methodHookImpl.hookMethod(getSSIDMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.addRelatedAttrs("WIFI SSID", (String)(param.getResult()));
                logger.recordAPICalling(param, "获取WIFI SSID");
            }
        });

        Method getBSSIDMethod = Reflector.findMethod(WifiInfo.class, "getBSSID");
        methodHookImpl.hookMethod(getBSSIDMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("Wifi BSSID", (String)(param.getResult()));
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.recordAPICalling(param, "获取Wifi BSSID");
            }
        });

        Method getInetAddressesMethod = Reflector.findMethod(NetworkInterface.class, "getInetAddresses");
        methodHookImpl.hookMethod(getInetAddressesMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                Enumeration<InetAddress> adds = (Enumeration<InetAddress>) param.getResult();
                StringBuilder sb = new StringBuilder();
                while(adds.hasMoreElements()) {
                    sb.append(adds.nextElement().getHostAddress());
                    sb.append(";");
                }
                logger.addRelatedAttrs("移动网络IP地址", sb.toString());
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.recordAPICalling(param, "获取移动网络IP地址");
            }
        });


    }
}
