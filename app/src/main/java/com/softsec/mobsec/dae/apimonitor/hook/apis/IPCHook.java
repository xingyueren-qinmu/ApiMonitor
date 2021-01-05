package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class IPCHook extends Hook {
    public static final String TAG = "DAEAM_IPC";

    @Override
    public void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        logger.setTag(TAG);

        Method startActivitiesMethod = Reflector.findMethod(ContextWrapper.class, "startActivities", Intent[].class);
        methodHookImpl.hookMethod(startActivitiesMethod, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent[] it = (Intent[]) param.args[0];
                StringBuilder sb = new StringBuilder();
                for(Intent i : it) {
                    sb.append(i).append(",");
                }
                logger.recordAPICalling(param, "打开其他Activity",
                        "activities", sb.toString().substring(0, sb.length() - 1));
            }
        });

        Method startServiceMethod = Reflector.findMethod(ContextWrapper.class, "startService", Intent.class);
        methodHookImpl.hookMethod(startServiceMethod, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                logger.recordAPICalling(param, "打开其他服务", "Intent", intent.toString());
            }
        });

        Method startActivityMethod1 = Reflector.findMethod(ContextWrapper.class, "startActivity",
                Intent.class, Bundle.class);
        methodHookImpl.hookMethod(startActivityMethod1, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                logger.recordAPICalling(param, "打开其他服务", "Intent", intent.toString());
            }
        });

        //Method Method = Reflector.findMethod()(ContextWrapper.class, "startActivity",
        Method startActivityMethod2 = Reflector.findMethod(Activity.class, "startActivity",
                Intent.class);
        methodHookImpl.hookMethod(startActivityMethod2, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                logger.recordAPICalling(param, "打开其他应用", "Intent", intent.toString());
            }
        });

        Method sendBroadcastMethod1 = Reflector.findMethod(ContextWrapper.class, "sendBroadcast",
                Intent.class);
        methodHookImpl.hookMethod(sendBroadcastMethod1, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                logger.recordAPICalling(param, "发送广播", "Intent", intent.toString());
            }
        });

        Method sendBroadcastMethod2 = Reflector.findMethod(ContextWrapper.class, "sendBroadcast",
                Intent.class, String.class);
        methodHookImpl.hookMethod(sendBroadcastMethod2, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                logger.recordAPICalling(param, "发送广播", "Intent", intent.toString());
            }
        });

        Method registerReceiverMethod1 = Reflector.findMethod(ContextWrapper.class, "registerReceiver",
                BroadcastReceiver.class, IntentFilter.class);
        methodHookImpl.hookMethod(registerReceiverMethod1, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                IntentFilter intentFilter = (IntentFilter) param.args[1];
                StringBuilder sb = new StringBuilder();
                sb.append("Actions: ");
                for(int i=0; i<intentFilter.countActions(); i++) {
                    sb.append(intentFilter.getAction(i)).append(",");
                }
                logger.recordAPICalling(param, "注册广播接收器", "Intent", sb.toString().substring(0, sb.length() - 1));
            }
        });

        Method registerReceiverMethod2 = Reflector.findMethod(ContextWrapper.class, "registerReceiver",
                BroadcastReceiver.class, IntentFilter.class, String.class, Handler.class);
        methodHookImpl.hookMethod(registerReceiverMethod2, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                IntentFilter intentFilter = (IntentFilter) param.args[1];
                StringBuilder sb = new StringBuilder();
                sb.append("Actions: ");
                for(int i=0; i<intentFilter.countActions(); i++){
                    sb.append(intentFilter.getAction(i)).append(",");
                }

                if(param.args[2] != null){
                    sb.append(" Permissions: ").append(param.args[2]);
                }
                logger.recordAPICalling(param, "注册广播接收器", "Intent", sb.toString().substring(0, sb.length() - 1));
            }
        });

    }
}

