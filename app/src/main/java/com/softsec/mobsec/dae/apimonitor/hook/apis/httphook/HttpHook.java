package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.net.URL;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author qinmu997
 */
public class HttpHook extends Hook {

    public static final String TAG = "Http";

    @Override
    public void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        try {
            Class<?> httpUrlConnectionClass = Reflector.findClass("java.net.HttpURLConnection", loadPackageParam.classLoader);
            MethodHookHandler.hookAllConstructors(httpUrlConnectionClass, new MethodHookCallBack() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
                    logger.setTag(TAG);
                    logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    if (param.args.length == 1 && param.args[0].getClass() == URL.class) {
                        logger.recordAPICalling(param, "网络通信",  "url", param.args[0].toString());
                    }
                }
            });
        } catch (ClassNotFoundException e) {
            Logger.logError(e);
        }

        Method openConnectionMethod = Reflector.findMethod(URL.class, "openConnection");
        MethodHookHandler.hookMethod(openConnectionMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                URL url = (URL)param.thisObject;
                String[] callingInfo = getCallingInfo(param.method.getName());
                Logger logger = new Logger();
                logger.setTag(TAG);
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.recordAPICalling(param, "网络通信", "url", url.toString());
            }
        });


    }
}
