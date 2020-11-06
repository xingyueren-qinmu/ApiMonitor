package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * @author qinmu997
 */
public class HttpHook extends Hook {

    public static final String TAG = "DAEAM_Http";

    @Override
    public void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        logger.setTag(TAG);

        try {
            Class<?> httpUrlConnectionClass = Reflector.findClass("java.net.HttpURLConnection", loadPackageParam.classLoader);
            methodHookImpl.hookAllConstructors(httpUrlConnectionClass, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length == 1 && param.args[0].getClass() == URL.class) {
                        logger.recordAPICalling(param, "通过URL建立网络连接",  "URL", param.args[0].toString());
                    }
                }
            });
        } catch (ClassNotFoundException e) {
            logger.logError(e);
        }

        try {
            Method openMethod = Reflector.findCustomerMethod("com.android.okhttp.OkHttpClient", loadPackageParam.classLoader, "open", URI.class);
            methodHookImpl.hookMethod(openMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] != null) {
                        URI uri = (URI) param.args[0];
                        logger.recordAPICalling(param, "打开网络链接", "URL", uri.toString());
                    } else {
                        logger.recordAPICalling(param, "打开网络链接");
                    }
                }
            });

            //com.squareup.okhttp.internal.http.HttpURLConnectionImpl
            Method getOutputStringMethod = Reflector.findCustomerMethod(
                    "com.android.okhttp.internal.http.HttpURLConnectionImpl",
                    loadPackageParam.classLoader,
                    "getOutputStream");
            methodHookImpl.hookMethod(getOutputStringMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    HttpURLConnection urlConn = (HttpURLConnection) param.thisObject;
                    if (urlConn != null) {
                        StringBuilder sb = new StringBuilder();
                        boolean connected = (boolean)getObjectField(param.thisObject, "connected");
                        if(!connected){
                            Map<String, List<String>> properties = urlConn.getRequestProperties();
                            if (properties != null && properties.size() > 0) {
                                for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
                                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
                                }
                            }
                            logger.recordAPICalling(param, "网络通信：请求",
                                    "method", urlConn.getRequestMethod(),
                                    "URL", urlConn.getURL().toString(),
                                    "params", sb.toString());
                        }
                    }

                }
            });

            Method getInputStreamMethod = Reflector.findCustomerMethod(
                    "com.android.okhttp.internal.http.HttpURLConnectionImpl",
                    loadPackageParam.classLoader,
                    "getInputStream");
            methodHookImpl.hookMethod(getInputStreamMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    HttpURLConnection urlConn = (HttpURLConnection) param.thisObject;
                    if (urlConn != null) {
                        StringBuilder sb = new StringBuilder();
                        int code = urlConn.getResponseCode();
                        if(code == 200){
                            Map<String, List<String>> properties = urlConn.getHeaderFields();
                            if (properties != null && properties.size() > 0) {
                                for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
                                    sb.append(entry.getKey() + ": " + entry.getValue() + ", ");
                                }
                            }
                        }
                        logger.recordAPICalling(param, "网络通信：响应",
                                "method", urlConn.getRequestMethod(),
                                "URL", urlConn.getURL().toString(),
                                "param", sb.toString()
                        );
                    }
                }
            });
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            logger.logError(e);
        }
    }
}
