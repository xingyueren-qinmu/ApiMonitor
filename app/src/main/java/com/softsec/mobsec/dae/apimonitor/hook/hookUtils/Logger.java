package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.LinkedHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * @author qinmu997
 */
public class Logger {

    private String methodName;
    private String methodClass;
    private String behaviorName;
    private String callingClass;
    private String callingMethod;
    private String tag;
    private LinkedHashMap<String, String> methodArgs = null;
    private LinkedHashMap<String, String> relatedAttrs = null;

    private static final String ERROR = "DAEAM_ERROR";

    public Logger() {}

    public Logger(String tag, String methodName, String methodClass, String behaviorName) {
        this.tag = tag;
        this.methodName = methodName;
        this.methodClass = methodClass;
        this.behaviorName = behaviorName;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void recordAPICalling(XC_MethodHook.MethodHookParam param, String behaviorName, String... argPairs) {
        methodName = param.method.getName();
        this.behaviorName = behaviorName;
        methodClass = param.thisObject.getClass().toString();
        if(methodArgs == null) {
            methodArgs = new LinkedHashMap<>();
        }
        if(argPairs.length > 0) {
            for(int i = 0; i < argPairs.length; i += 2) {
                methodArgs.put(argPairs[i], argPairs[i + 1]);
            }
        }
        generateLog();
    }


    public void addRelatedAttrs(String attrName, String content) {
        if(relatedAttrs == null) {
            relatedAttrs = new LinkedHashMap<>();
        }
        relatedAttrs.put(attrName, content);
    }

    // 相当于手撸了一个json格式，懒得用JSON库了，毕竟层层叠叠的
    private void generateLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis()).append(":{");
        sb.append("\"tag\":\"").append(tag).append("\",");
        sb.append("\"behavior\":\"").append(behaviorName).append("\",");
        sb.append("\"callingClass\":\"").append(callingClass).append("\",");
        sb.append("\"callingMethod\":\"").append(callingMethod).append("\",");
        if(!"".equals(methodClass)) {
            sb.append("\"methodClass\":\"").append(methodClass).append("\",");
        }
        if(!"".equals(methodName)) {
            sb.append("\"method\":\"").append(methodName).append("\",");
        }
        if(methodArgs != null && methodArgs.size() > 0) {
            sb.append("\"methodArgs\":{");
            for (String key : methodArgs.keySet()) {
                String value = methodArgs.get(key) == null ? "null" : methodArgs.get(key);
                sb.append("\"").append(key).append("\":\"")
                        .append(value.replace("\"", "\\\""))
                        .append("\",");
            }
            sb.delete(sb.length() - 1, sb.length()).append("},");
        }
        if(relatedAttrs != null && relatedAttrs.size() > 0) {
            sb.append("\"relatedAttrs\":{");
            for (String key : relatedAttrs.keySet()) {
                sb.append("\"").append(key).append("\":");
                String value = relatedAttrs.get(key);
                try {
                    new Gson().fromJson(value, Object.class);
                    sb.append(value);
                } catch (JsonSyntaxException e) {
                    sb.append("\"")
                            .append(value.replace("\"", "\\\""))
                            .append("\"");
                }
                sb.append(",");
            }
            sb.delete(sb.length() - 1, sb.length()).append("},");
        }
        sb.delete(sb.length() - 1, sb.length()).append("},");
        XposedBridge.log(sb.toString());
        clear();
    }

    private void clear() {
        methodName = "";
        methodClass = "";
        behaviorName = "";
        methodArgs = null;
        relatedAttrs = null;
    }

    public void logError(Exception e) {
        XposedBridge.log(ERROR + " " + e.getMessage());
    }

    public void setCallingInfo(String callingInfo) {
        callingInfo = "".equals(callingInfo) ? "null---null" : callingInfo;
        callingClass = callingInfo.split("---")[0];
        callingMethod = callingInfo.split("---")[1];
    }
}
