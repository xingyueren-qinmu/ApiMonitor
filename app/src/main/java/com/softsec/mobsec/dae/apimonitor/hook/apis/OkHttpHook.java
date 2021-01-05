package com.softsec.mobsec.dae.apimonitor.hook.apis;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import kotlin.Pair;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class OkHttpHook extends Hook {

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        logger.setTag("OKHttp");

        try {
            Method openMethod = Reflector.findMethod("com.android.okhttp.OkHttpClient", packageParam.classLoader, "open", URI.class);
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
            Method getOutputStringMethod = Reflector.findMethod(
                    "com.android.okhttp.internal.http.HttpURLConnectionImpl",
                    packageParam.classLoader,
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

            Method getInputStreamMethod = Reflector.findMethod(
                    "com.android.okhttp.internal.http.HttpURLConnectionImpl",
                    packageParam.classLoader,
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

        try {
            Class requestClass = Reflector.findClass(
                    "okhttp3.Request",
                    packageParam.classLoader);
            Method newCallMethod = Reflector.findMethod(
                    "okhttp3.OkHttpClient", packageParam.classLoader,
                    "newCall", requestClass
            );

            methodHookImpl.hookMethod(newCallMethod, new MethodHookCallBack() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Request request = (Request) param.args[0];
                    StringBuilder sb = new StringBuilder();
                    for (Iterator<Pair<String, String>> it = request.headers().iterator(); it.hasNext(); ) {
                        Pair<String, String> pair = it.next();
                        sb.append(pair.component1()).append(":").append(pair.component2());
                    }
                    logger.addRelatedAttrs("url", request.url().toString());
                    logger.addRelatedAttrs("headers", sb.toString());
                    if(request.method().equals("POST")) {
                        RequestBody body = request.body();
                        if(null != body) {
                            if(body.contentLength() <= 512) {
                                Buffer buffer = new Buffer();
                                body.writeTo(buffer);
                                logger.addRelatedAttrs("method", "POST");
                                logger.addRelatedAttrs("body", buffer.readUtf8());
                            }
                        }
                    } else {
                        logger.addRelatedAttrs("method", "GET");
                    }
                    logger.recordAPICalling(param, "建立网络连接");
                }
            });
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.logError(e);
        }
    }
}
