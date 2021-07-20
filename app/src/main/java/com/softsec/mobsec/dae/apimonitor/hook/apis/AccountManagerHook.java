package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AccountManagerHook extends Hook {

    public static final String TAG = "AccountManager:";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {


        Method getAccountsMethod = Reflector.findMethod(
                AccountManager.class, "getAccounts");
        MethodHookHandler.hookMethod(getAccountsMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Account[] accs = (Account[]) param.getResult();
                StringBuilder sb = new StringBuilder();
                for(Account acc : accs) {
                    sb.append("name=").append(acc.name).append("&type=").append(acc.type).append(";");
                }
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.addRelatedAttrs("result", sb.toString());
                logger.recordAPICalling(param, "获取账户信息");
            }
        });

        Method getAccountsByTypeMethod = Reflector.findMethod(
                AccountManager.class, "getAccountsByType", String.class);
        MethodHookHandler.hookMethod(getAccountsByTypeMethod, new MethodHookCallBack() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Exception {
                Account[] accs = (Account[]) param.getResult();
                StringBuilder sb = new StringBuilder();
                for(Account acc : accs) {
                    sb.append("name=").append(acc.name).append("&type=").append(acc.type).append(";");
                }
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.addRelatedAttrs("result", sb.toString());
                logger.recordAPICalling(param, "获取账户信息", "type", (String)param.args[0]);
            }
        });
    }
}
