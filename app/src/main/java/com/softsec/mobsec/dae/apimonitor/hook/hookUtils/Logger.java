package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import java.util.LinkedHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * @author qinmu997
 */
public class Logger {

    private String methodName;
    private String className;
    private String behaviorName;
    private String tag;
    private LinkedHashMap<String, String> methodArgs = null;
    private LinkedHashMap<String, String> relatedAttrs = null;

    private static final String ERROR = "DAEAM_ERROR";

    public Logger() {}

    public Logger(String tag, String methodName, String className, String behaviorName) {
        this.tag = tag;
        this.methodName = methodName;
        this.className = className;
        this.behaviorName = behaviorName;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void recordAPICalling(XC_MethodHook.MethodHookParam param, String behaviorName, String... argPairs) {
        methodName = param.method.getName();
        this.behaviorName = behaviorName;
        className = "";
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

    public void addMethodArgs(String argName, String content) {
        if(methodArgs == null) {
            methodArgs = new LinkedHashMap<>();
        }
        methodArgs.put(argName, content);
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
        sb.append("\"").append(tag).append("\":{");
        sb.append("\"timestamp\":").append(System.currentTimeMillis()).append(",");
        sb.append("\"behavior\":\"").append(behaviorName).append("\",");
        if(!"".equals(className)) {
            sb.append("\"class\":\"").append(className).append("\",");
        }
        if(!"".equals(methodName)) {
            sb.append("\"method\":\"").append(methodName).append("\",");
        }
        if(methodArgs != null && methodArgs.size() > 0) {
            sb.append("\"methodArgs\":{");
            for (String key : methodArgs.keySet()) {
                sb.append("\"").append(key).append("\":\"")
                        .append(methodArgs.get(key).replace("\"", "\\\""))
                        .append("\",");
            }
            sb.delete(sb.length() - 1, sb.length()).append("},");
        }
        if(relatedAttrs != null && relatedAttrs.size() > 0) {
            sb.append("\"relatedAttrs\":{");
            for (String key : relatedAttrs.keySet()) {
                sb.append("\"").append(key).append("\":\"")
                        .append(relatedAttrs.get(key).replace("\"", "\\\""))
                        .append("\",");
            }
            sb.delete(sb.length() - 1, sb.length()).append("},");
        }
        sb.delete(sb.length() - 1, sb.length()).append("},");
        XposedBridge.log(sb.toString());
        clear();
    }

    private void clear() {
        methodName = "";
        className = "";
        behaviorName = "";
        methodArgs = null;
        relatedAttrs = null;
    }

    public void logError(Exception e) {
        XposedBridge.log(ERROR + " " + e.getMessage());
    }
}
