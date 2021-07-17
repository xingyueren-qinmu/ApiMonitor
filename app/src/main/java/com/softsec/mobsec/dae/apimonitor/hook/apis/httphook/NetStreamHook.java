package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook;

import android.util.Base64;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.CLogUtils;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NetStreamHook extends Hook {
    public static final String TAG = "NetStream";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {

        XposedBridge.log("Hook 底层");

        try {
            Method socketOutputStreamMethod = Reflector.findMethod(XposedHelpers.findClass("java.net.SocketOutputStream", packageParam.classLoader), "write", byte[].class, int.class, int.class);
            MethodHookHandler.hookMethod(socketOutputStreamMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    StringBuilder TraceString = new StringBuilder();
                    TraceString.append(new String((byte[]) param.args[0], StandardCharsets.UTF_8));
                    CLogUtils.NetLoggerOri("Request:\n" + TraceString.toString());
                    String rawString = TraceString.toString().split("\n\r\n")[0];
                    Map<String, String> header = analyzeRequest(rawString);
                    String request_raw = Base64.encodeToString((byte[])param.args[0], Base64.NO_WRAP);
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
//                    logger.addRelatedAttrs("request_raw",request_raw);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    logger.recordAPICalling(param, "Socket请求",
                            "method", header.get("method"),
                            "url", header.get("url"),
                            "host", header.get("Host"),
                            "request_size", header.get("Content-Length"),
                            "protocol", header.get("protocol"));
                }
            });
        } catch (Throwable e) {
            XposedBridge.log("HookGetOutPushStream     " + e.toString());
            Logger.logError(e);
        }


        try {
            Method socketInputStreamMethod = Reflector.findMethod(XposedHelpers.findClass("java.net.SocketInputStream", packageParam.classLoader), "read", byte[].class, int.class, int.class);
            MethodHookHandler.hookMethod(socketInputStreamMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    StringBuilder TraceString = new StringBuilder();
                    TraceString.append(new String((byte[]) param.args[0], StandardCharsets.UTF_8));
                    CLogUtils.NetLoggerOri("Response:\n" + TraceString.toString());
                    String rawString = TraceString.toString().split("\n\r\n")[0];
                    Map<String, String> result = analyzeResponse(rawString);
                    // String rawResponse = Util.byteArrayToString((byte[]) param.args[0]);
                    String response_raw = Base64.encodeToString((byte[])param.args[0], Base64.NO_WRAP);
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
//                    logger.addRelatedAttrs("response_raw",response_raw);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    logger.recordAPICalling(param, "Socket响应",
                            "code", result.get("code"),
                            "url", result.get("url"),
                            "host", result.get("Host"),
                            "response_size", result.get("Content-Length"));
                }
            });

        } catch (Throwable e) {
            XposedBridge.log("HookGetInputStream     " + e.toString());
            Logger.logError(e);
        }
    }

    /*
     * 分析底层方法得到的request信息头
     * */
    private Map<String, String> analyzeRequest(String rawString){
        Map<String, String> header = new HashMap<String, String>();
        String[] strings = rawString.split("\n");
        for(String s : strings){
            if(s.contains(":")){
                String key = s.split(":")[0].replace(" ","");
                String value = s.split(":")[1].replace(" ","").replace("\r","");
                if(key.equals("Host")){
                    header.put(key, value);
                }else if(key.equals("Content-Length")){
                    header.put(key, value);
                }
            }else{
                String[] firstLine = s.split("\\s");
                if(firstLine[0].equals("POST") || firstLine[0].equals("GET")){
                    header.put("method", firstLine[0].replace(" ",""));
                    header.put("url", firstLine[1].replace(" ",""));
                    header.put("protocol", firstLine[2].replace(" ",""));
                }
            }
        }

        return header;
    }

    private Map<String, String> analyzeResponse(String rawString){
        Map<String, String> result = new HashMap<String, String>();
        String[] strings = rawString.split("\n");

        String[] firstLine = strings[0].split("\\s");
        if(firstLine.length > 1){
            if(firstLine[1].startsWith("http")){
                result.put("url",firstLine[1]);
            }else if(firstLine[1].contains(":")){
                String[] hostToCode = firstLine[1].split(":");
                if(hostToCode.length == 2){
                    result.put("Host", hostToCode[0]);
                }
            }else{
                result.put("code", firstLine[1]);
            }
        }

        for(String s : strings){
            if(s.contains(":")){
                String key = s.split(":")[0].replace(" ", "");
                String value = s.split(":")[1].replace(" ", "").replace("\r","");
                if(key.equals("Content-Length")){
                    result.put("Content-Length", value);
                    return result;
                }
            }
        }
        return result;
    }
}
