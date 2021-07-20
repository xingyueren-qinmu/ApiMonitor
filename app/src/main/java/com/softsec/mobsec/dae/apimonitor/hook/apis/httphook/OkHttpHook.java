package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OkHttpHook extends Hook {

    public static final String TAG = "OKHttp";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {


        try {
            Method openMethod = Reflector.findMethod("com.android.okhttp.OkHttpClient", packageParam.classLoader, "open", URI.class);
            if(openMethod != null) {
                MethodHookHandler.hookMethod(openMethod, new MethodHookCallBack() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String[] callingInfo = getCallingInfo(param.method.getName());
                        Logger logger = new Logger();
                        logger.setTag(TAG);
                        logger.setCallingInfo(callingInfo[0]);
                        logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                        if (param.args[0] != null) {
                            URI uri = (URI) param.args[0];
                            logger.recordAPICalling(param, "okhttp请求", "URL", uri.toString());
                        } else {
                            logger.recordAPICalling(param, "okhttp请求");
                        }
                    }
                });
            }

//
//
//            //com.squareup.okhttp.internal.http.HttpURLConnectionImpl
//            Method getOutputStringMethod = Reflector.findMethod(
//                    "com.android.okhttp.internal.http.HttpURLConnectionImpl",
//                    packageParam.classLoader,
//                    "getOutputStream");
//            MethodHookHandler.hookMethod(getOutputStringMethod, new MethodHookCallBack() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) {
//                    HttpURLConnection urlConn = (HttpURLConnection) param.thisObject;
//                    if (urlConn != null) {
//                        StringBuilder sb = new StringBuilder();
//                        boolean connected = (boolean)getObjectField(param.thisObject, "connected");
//                        if(!connected){
//                            Map<String, List<String>> properties = urlConn.getRequestProperties();
//                            if (properties != null && properties.size() > 0) {
//                                for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
//                                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
//                                }
//                            }
//                            logger.recordAPICalling(param, "网络通信：请求",
//                                    "method", urlConn.getRequestMethod(),
//                                    "URL", urlConn.getURL().toString(),
//                                    "params", sb.toString());
//                        }
//                    }
//
//                }
//            });
//
//            Method getInputStreamMethod = Reflector.findMethod(
//                    "com.android.okhttp.internal.http.HttpURLConnectionImpl",
//                    packageParam.classLoader,
//                    "getInputStream");
//            MethodHookHandler.hookMethod(getInputStreamMethod, new MethodHookCallBack() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    HttpURLConnection urlConn = (HttpURLConnection) param.thisObject;
//                    if (urlConn != null) {
//                        StringBuilder sb = new StringBuilder();
//                        int code = urlConn.getResponseCode();
//                        if(code == 200){
//                            Map<String, List<String>> properties = urlConn.getHeaderFields();
//                            if (properties != null && properties.size() > 0) {
//                                for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
//                                    sb.append(entry.getKey() + ": " + entry.getValue() + ", ");
//                                }
//                            }
//                        }
//                        logger.recordAPICalling(param, "网络通信：响应",
//                                "method", urlConn.getRequestMethod(),
//                                "URL", urlConn.getURL().toString(),
//                                "param", sb.toString()
//                        );
//                    }
//                }
//            });
//        } catch (NoSuchMethodException | ClassNotFoundException e) {
//            Logger.logError(e);
//        }

//        try {
//            Class requestClass = Reflector.findClass(
//                    "okhttp3.Request",
//                    packageParam.classLoader);
//            Method newCallMethod = Reflector.findMethod(
//                    "okhttp3.OkHttpClient", packageParam.classLoader,
//                    "newCall", requestClass
//            );
//
//            MethodHookHandler.hookMethod(newCallMethod, new MethodHookCallBack() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    Request request = (Request) param.args[0];
//                    StringBuilder sb = new StringBuilder();
//                    for (Iterator<Pair<String, String>> it = request.headers().iterator(); it.hasNext(); ) {
//                        Pair<String, String> pair = it.next();
//                        sb.append(pair.component1()).append(":").append(pair.component2());
//                    }
//                    logger.addRelatedAttrs("url", request.url().toString());
//                    logger.addRelatedAttrs("headers", sb.toString());
//                    if(request.method().equals("POST")) {
//                        RequestBody body = request.body();
//                        if(null != body) {
//                            if(body.contentLength() <= 512) {
//                                Buffer buffer = new Buffer();
//                                body.writeTo(buffer);
//                                logger.addRelatedAttrs("method", "POST");
//                                logger.addRelatedAttrs("body", buffer.readUtf8());
//                            }
//                        }
//                    } else {
//                        logger.addRelatedAttrs("method", "GET");
//                    }
//                    logger.recordAPICalling(param, "建立网络连接");
//                }
//            });
//        } catch (ClassNotFoundException | NoSuchMethodException e) {
//            Logger.logError(e);
//        }

            XposedBridge.log("okhttp test in");
            Class requestClass = Reflector.findClass("okhttp3.Request", packageParam.classLoader);
//            Class aClassRealInterceptorChain = Reflector.findClass("okhttp3.internal.http.RealInterceptorChain", packageParam.classLoader);
//            XposedHelpers.findAndHookMethod(aClassRealInterceptorChain, "proceed", requestClass, new MethodHookCallBack() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    StringBuilder sb = new StringBuilder();
//
//                    //入参
//                    Object arg = param.args[0];  //this.originalRequest
//                    StringBuilder sbRequest = new StringBuilder();
//                    sbRequest.append("----->Start Net").append("\n");
//                    sbRequest.append("arg.getClass: ").append(arg.getClass()).append("\n");
//                    sbRequest.append("arg: ").append(arg).append("\n");
//
//                    //获取body
//                    Class<?> aClassRequest = arg.getClass();
//                    Method requestBodyMethod = aClassRequest.getDeclaredMethod("body");
//                    Object invokeRequestBody = requestBodyMethod.invoke(arg);
//                    boolean hasRequestBody = invokeRequestBody != null;
//
//                    Method methodMethod = aClassRequest.getDeclaredMethod("method");
//                    Object invokeMethod = methodMethod.invoke(arg);
//
//                    if (hasRequestBody) {
//                        Class<?> invokeBodyClass = invokeRequestBody.getClass();
//
//                        Method contentTypeMethod = invokeBodyClass.getDeclaredMethod("contentType");
//                        Object invokeContentType = contentTypeMethod.invoke(invokeRequestBody);
//                        if (invokeContentType != null) {
//                            sbRequest.append("Content-Type: ").append(invokeContentType).append("\n");
//                        }
//
//                        Method contentLengthMethod = invokeBodyClass.getDeclaredMethod("contentLength");
//                        Object invokeContentLength = contentLengthMethod.invoke(invokeRequestBody);
//                        if (invokeContentLength != null) {
//                            sbRequest.append("Content-Length: ").append(invokeContentLength).append("\n");
//                        }
//
//                        Method headersMethod = aClassRequest.getDeclaredMethod("headers");
//                        Object invokeHeaders = headersMethod.invoke(arg);
//                        if (invokeHeaders != null) {
//                            Class<?> aClassHeaders = invokeHeaders.getClass();
//                            Method sizeMethod = aClassHeaders.getDeclaredMethod("size");
//                            int invokeSize = (int) sizeMethod.invoke(invokeHeaders);
//                            for (int i = 0; i < invokeSize; i++) {
//                                Method nameMethod = aClassHeaders.getDeclaredMethod("name", int.class);
//                                String name = (String) nameMethod.invoke(invokeHeaders, i);
//                                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
//                                    sbRequest.append(name).append(": ").append(aClassHeaders.getDeclaredMethod("value", int.class).invoke(invokeHeaders, i)).append("\n");
//                                }
//                            }
//
//                            if (bodyEncoded(invokeHeaders)) {
//                                sbRequest.append("----->End ").append("invokeHeaders: ").append(invokeHeaders).append("(encoded body omitted)").append("\n");
//                            } else {
//                                Class<?> aClassBuffer = null;
//                                try {
//                                    aClassBuffer = Reflector.findClass("okio.Buffer", packageParam.classLoader);
//                                } catch (Exception e) {
//                                    aClassBuffer = Reflector.findClass("okio.c", packageParam.classLoader);
//                                }
//                                Object objBuffer = aClassBuffer.newInstance();
//                                Class<?> aClassBufferedSink = null;
//                                try {
//                                    aClassBufferedSink = Reflector.findClass("okio.BufferedSink", packageParam.classLoader);
//                                } catch (Exception e) {
//                                    aClassBufferedSink = Reflector.findClass("okio.d", packageParam.classLoader);
//                                }
//                                Method writeToMethod = invokeBodyClass.getDeclaredMethod("writeTo", aClassBufferedSink);
//                                writeToMethod.invoke(invokeRequestBody, objBuffer);
//                                Method readStringMethod = null;
//                                try {
//                                    readStringMethod = aClassBuffer.getDeclaredMethod("readString", Charset.class);
//                                } catch (Exception e) {
//                                    readStringMethod = aClassBuffer.getDeclaredMethod("a", Charset.class);
//                                }
//                                String invokeReadString = (String) readStringMethod.invoke(objBuffer, Charset.forName("UTF-8"));
//                                sbRequest.append("request params: ").append(invokeReadString).append("\n");
//                                sbRequest.append("----->End ").append(invokeMethod).append("(").append(invokeContentLength).append("-byte body)\n");
//                            }
//                        }
//
//                    } else {
//                        sbRequest.append("----->End ").append("invokeMethod: ").append(invokeMethod).append("\n");
//                    }
//
//
//                    //出参
//                    Object result = param.getResult();
//                    StringBuilder sbResponse = new StringBuilder();
//                    if (result != null) {
//                        sbResponse.append("----->Start ").append("\n");
//                        sbResponse.append("result: ").append(result).append("\n");
//
//                        Class<?> aClassResponse = result.getClass();
//                        Method headersMethod = aClassResponse.getDeclaredMethod("headers");
//                        Object invokeHeaders = headersMethod.invoke(result);
//                        boolean isGzip = false;
//                        if (invokeHeaders != null) {
//                            Class<?> aClassHeaders = invokeHeaders.getClass();
//                            Method sizeMethod = aClassHeaders.getDeclaredMethod("size");
//                            int invokeSize = (int) sizeMethod.invoke(invokeHeaders);
//                            for (int i = 0; i < invokeSize; i++) {
//                                Method nameMethod = aClassHeaders.getDeclaredMethod("name", int.class);
//                                String name = (String) nameMethod.invoke(invokeHeaders, i);
//                                String value = (String) aClassHeaders.getDeclaredMethod("value", int.class).invoke(invokeHeaders, i);
//                                if ("Content-Encoding".equalsIgnoreCase(name) && "gzip".equals(value)) {
//                                    //标记下数据是否压缩
//                                    isGzip = true;
//                                }
//                                sbResponse.append(name).append(": ").append(value).append("\n");
//                            }
//                        }
//                        Method bodyResponseMethod = aClassResponse.getDeclaredMethod("body");
//                        Object invokeResponseBody = bodyResponseMethod.invoke(result);
//                        Class<?> aClassResponseBody = invokeResponseBody.getClass();
//                        Method sourceMethod = aClassResponseBody.getDeclaredMethod("source");
//                        Object invokeBufferedSource = sourceMethod.invoke(invokeResponseBody);
//                        //okio.e  BufferedSource
//                        Class<?> aClassBufferedSource = invokeBufferedSource.getClass();
//                        //b request
//                        Method requestMethod;//request
//                        try {
//                            requestMethod = aClassBufferedSource.getDeclaredMethod("request", long.class);//request
//                        } catch (Exception e) {
//                            requestMethod = aClassBufferedSource.getDeclaredMethod("b", long.class);//request
//                        }
//                        requestMethod.invoke(invokeBufferedSource, Long.MAX_VALUE);
//                        // c b() buffer
//                        Method bufferMethod;//buffer
//                        try {
//                            bufferMethod = aClassBufferedSource.getDeclaredMethod("buffer");//buffer
//                        } catch (Exception e) {
//                            bufferMethod = aClassBufferedSource.getDeclaredMethod("b");//buffer
//                        }
//                        Object invokeBuffer = bufferMethod.invoke(invokeBufferedSource);
//                        Class<?> aClassBuffer = invokeBuffer.getClass();
//                        Method cloneMethod = aClassBuffer.getDeclaredMethod("clone");
//                        Object invoke = cloneMethod.invoke(invokeBuffer);
//                        Class<?> aClass = invoke.getClass();
//                        Method readString = null;
//                        try {
//                            readString = aClass.getDeclaredMethod("readString", Charset.class);//readString
//                        } catch (Exception e) {
//                            readString = aClass.getDeclaredMethod("a", Charset.class);//readString
//                        }
//                        String repResult = (String) readString.invoke(invoke, Charset.forName("UTF-8"));
//
//                        sbResponse.append("response result: ").append(isGzip ? new String(uncompress(repResult.getBytes())) : repResult).append("\n");
//                    }
//
//                    sbResponse.append("----> End Net");
//
//                    sb.append(sbRequest);
//                    sb.append(sbResponse);
//
//                    XposedBridge.log(sb.toString());
//
//                }
//            });

            Method proceedMethod = Reflector.findMethod("okhttp3.internal.http.RealInterceptorChain",packageParam.classLoader, "proceed", requestClass);
            if(null != proceedMethod) {
                MethodHookHandler.hookMethod(proceedMethod, new MethodHookCallBack() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        StringBuilder sb = new StringBuilder();

                        //入参
                        Object arg = param.args[0];  //this.originalRequest


//                    StringBuilder sbRequest = new StringBuilder();
//                    sbRequest.append("----->Start Net").append("\n");
//                    sbRequest.append("arg.getClass: ").append(arg.getClass()).append("\n");
//                    sbRequest.append("arg: ").append(arg).append("\n");
//
//                    //获取body
//                    Class<?> aClassRequest = arg.getClass();
//                    Method requestBodyMethod = aClassRequest.getDeclaredMethod("body");
//                    Object invokeRequestBody = requestBodyMethod.invoke(arg);
//                    boolean hasRequestBody = invokeRequestBody != null;
//
//                    Method methodMethod = aClassRequest.getDeclaredMethod("method");
//                    Object invokeMethod = methodMethod.invoke(arg);
//
//                    if(hasRequestBody){
//                        Class<?> invokeBodyClass = invokeRequestBody.getClass();
//
//                        Method contentTypeMethod = invokeBodyClass.getDeclaredMethod("contentType");
//                        Object invokeContentType = contentTypeMethod.invoke(invokeRequestBody);
//                        if(invokeContentType != null){
//                            sbRequest.append("Content-Type: ").append(invokeContentType).append("\n");
//                        }
//
//                        Method contentLengthMethod = invokeBodyClass.getDeclaredMethod("contentLength");
//                        Object invokeContentLength = contentLengthMethod.invoke(invokeRequestBody);
//                        if(invokeContentLength != null){
//                            sbRequest.append("Content-Length: ").append(invokeContentLength).append("\n");
//                        }
//
//                        Method headersMethod = aClassRequest.getDeclaredMethod("headers");
//                        Object invokeHeaders = headersMethod.invoke(arg);
//                        if(invokeHeaders != null){
//                            Class<?> aClassHeaders = invokeHeaders.getClass();
//                            Method sizeMethod = aClassHeaders.getDeclaredMethod("size");
//                            int invokeSize = (int) sizeMethod.invoke(invokeHeaders);
//                            for(int i = 0; i < invokeSize; i++){
//                                Method nameMethod = aClassHeaders.getDeclaredMethod("name", int.class);
//                                String name = (String) nameMethod.invoke(invokeHeaders, i);
//                                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
//                                    sbRequest.append(name).append(": ").append(aClassHeaders.getDeclaredMethod("value", int.class).invoke(invokeHeaders, i)).append("\n");
//                                }
//                            }
//
//                            if(bodyEncoded(invokeHeaders)){
//                                sbRequest.append("----->End ").append("invokeHeaders: ").append(invokeHeaders).append("(encoded body omitted)").append("\n");
//                            } else{
//                                Class<?> aClassBuffer = null;
//                                try{
//                                    aClassBuffer = Reflector.findClass("okio.Buffer", packageParam.classLoader);
//                                } catch (Exception e){
//                                    aClassBuffer = Reflector.findClass("okio.c", packageParam.classLoader);
//                                }
//                                Object objBuffer = aClassBuffer.newInstance();
//                                Class<?> aClassBufferedSink = null;
//                                try{
//                                    aClassBufferedSink = Reflector.findClass("okio.BufferedSink", packageParam.classLoader);
//                                } catch (Exception e){
//                                    aClassBufferedSink = Reflector.findClass("okio.d", packageParam.classLoader);
//                                }
//                                Method writeToMethod = invokeBodyClass.getDeclaredMethod("writeTo", aClassBufferedSink);
//                                writeToMethod.invoke(invokeRequestBody, objBuffer);
//                                Method readStringMethod = null;
//                                try{
//                                    readStringMethod = aClassBuffer.getDeclaredMethod("readString", Charset.class);
//                                } catch(Exception e){
//                                    readStringMethod = aClassBuffer.getDeclaredMethod("a", Charset.class);
//                                }
//                                String invokeReadString = (String) readStringMethod.invoke(objBuffer, Charset.forName("UTF-8"));
//                                sbRequest.append("request params: ").append(invokeReadString).append("\n");
//                                sbRequest.append("----->End ").append(invokeMethod).append("(").append(invokeContentLength).append("-byte body)\n");
//                            }
//                        }
//
//                    } else{
//                        sbRequest.append("----->End ").append("invokeMethod: ").append(invokeMethod).append("\n");
//                    }
//
//
//                    //出参
//                    Object result = param.getResult();
//                    StringBuilder sbResponse = new StringBuilder();
//                    if (result != null) {
//                        sbResponse.append("----->Start ").append("\n");
//                        sbResponse.append("result: ").append(result).append("\n");
//
//                        Class<?> aClassResponse = result.getClass();
//                        Method headersMethod = aClassResponse.getDeclaredMethod("headers");
//                        Object invokeHeaders = headersMethod.invoke(result);
//                        boolean isGzip = false;
//                        if (invokeHeaders != null) {
//                            Class<?> aClassHeaders = invokeHeaders.getClass();
//                            Method sizeMethod = aClassHeaders.getDeclaredMethod("size");
//                            int invokeSize = (int) sizeMethod.invoke(invokeHeaders);
//                            for (int i = 0; i < invokeSize; i++) {
//                                Method nameMethod = aClassHeaders.getDeclaredMethod("name", int.class);
//                                String name = (String) nameMethod.invoke(invokeHeaders, i);
//                                String value = (String) aClassHeaders.getDeclaredMethod("value", int.class).invoke(invokeHeaders, i);
//                                if ("Content-Encoding".equalsIgnoreCase(name) && "gzip".equals(value)) {
//                                    //标记下数据是否压缩
//                                    isGzip = true;
//                                }
//                                sbResponse.append(name).append(": ").append(value).append("\n");
//                            }
//                        }
//                        Method bodyResponseMethod = aClassResponse.getDeclaredMethod("body");
//                        Object invokeResponseBody = bodyResponseMethod.invoke(result);
//                        Class<?> aClassResponseBody = invokeResponseBody.getClass();
//                        Method sourceMethod = aClassResponseBody.getDeclaredMethod("source");
//                        Object invokeBufferedSource = sourceMethod.invoke(invokeResponseBody);
//                        //okio.e  BufferedSource
//                        Class<?> aClassBufferedSource = invokeBufferedSource.getClass();
//                        //b request
//                        Method requestMethod;//request
//                        try {
//                            requestMethod = aClassBufferedSource.getDeclaredMethod("request", long.class);//request
//                        } catch (Exception e) {
//                            requestMethod = aClassBufferedSource.getDeclaredMethod("b", long.class);//request
//                        }
//                        requestMethod.invoke(invokeBufferedSource, Long.MAX_VALUE);
//                        // c b() buffer
//                        Method bufferMethod;//buffer
//                        try {
//                            bufferMethod = aClassBufferedSource.getDeclaredMethod("buffer");//buffer
//                        } catch (Exception e) {
//                            bufferMethod = aClassBufferedSource.getDeclaredMethod("b");//buffer
//                        }
//                        Object invokeBuffer = bufferMethod.invoke(invokeBufferedSource);
//                        Class<?> aClassBuffer = invokeBuffer.getClass();
//                        Method cloneMethod = aClassBuffer.getDeclaredMethod("clone");
//                        Object invoke = cloneMethod.invoke(invokeBuffer);
//                        Class<?> aClass = invoke.getClass();
//                        Method readString = null;
//                        try {
//                            readString = aClass.getDeclaredMethod("readString", Charset.class);//readString
//                        } catch (Exception e) {
//                            readString = aClass.getDeclaredMethod("a", Charset.class);//readString
//                        }
//                        String repResult = (String) readString.invoke(invoke, Charset.forName("UTF-8"));
//
//                        sbResponse.append("response result: ").append(isGzip ? new String(uncompress(repResult.getBytes())) : repResult).append("\n");
//                    }
//
//                    sbResponse.append("----> End Net");
//
//                    sb.append(sbRequest);
//                    sb.append(sbResponse);
//
//                    XposedBridge.log(sb.toString());

                    }
                });
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            Logger.logError(e);
        }
    }


    private boolean bodyEncoded(Object invokeHeaders) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String contentEncoding = (String) invokeHeaders.getClass().getDeclaredMethod("get", String.class).invoke(invokeHeaders, "Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (Exception e) {
            //Logger.logError(e);
            return bytes;
        }
    }

}
