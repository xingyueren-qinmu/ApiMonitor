package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.accounts.AccountManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AccountManagerHook extends Hook {

    public static final String TAG = "DAEAM_AccountManager:";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        logger.setTag(TAG);

        Method getAccountsMethod = Reflector.findMethod(
                AccountManager.class, "getAccounts");
        methodHookImpl.hookMethod(getAccountsMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                logger.recordAPICalling(param, "获取账户信息");
            }
        });

        Method getAccountsByTypeMethod = Reflector.findMethod(
                AccountManager.class, "getAccountsByType", String.class);
        methodHookImpl.hookMethod(getAccountsByTypeMethod, new MethodHookCallBack() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Exception {
                logger.recordAPICalling(param, "获取账户信息", "type", (String)param.args[0]);
            }
        });
    }
}
