package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.UnknownHostException;
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

    private static final String ERROR = "ERROR";

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
        methodClass = param.getClass().toString();
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

    public void recordAPICalling(String behaviorName, String... argPairs) {
        methodName = "";
        this.behaviorName = behaviorName;
        methodClass = "";
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

        public void recordAPICallingAdd(String behaviorName, String... argPairs) {
        methodName = "";
        this.behaviorName = behaviorName;
        methodClass = "";
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
        if(null == relatedAttrs) {
            relatedAttrs = new LinkedHashMap<>();
        }
        relatedAttrs.put(attrName, content);
    }

    private void generateLog() {
        StringBuilder sb = new StringBuilder();
        JsonObject json = new JsonObject();
        json.addProperty("tag", "DAEAM_" + tag);
        json.addProperty("behavior", behaviorName);
        json.addProperty("callingClass", callingClass);
        json.addProperty("callingMethod", callingMethod);
        if(!"".equals(methodClass)) {
            json.addProperty("methodClass", methodClass);
        }
        if(!"".equals(methodName)) {
            json.addProperty("method", methodName);
        }

        if(methodArgs != null && methodArgs.size() > 0) {
            JsonObject args = new JsonObject();
            for (String key : methodArgs.keySet()) {
                String value = methodArgs.get(key) == null ? "null" : methodArgs.get(key);
                args.addProperty(key, value);
            }
            json.add("methodArgs", args);
        }

        if(null != relatedAttrs && !relatedAttrs.isEmpty()) {
            JsonObject attrs = new JsonObject();
            for (String key : relatedAttrs.keySet()) {
                String value = relatedAttrs.get(key);
                if("xrefFrom".equals(key)) {
                    JsonArray array = new JsonArray();
                    for(String s : value.split(";")) {
                        array.add(s);
                    }
                    attrs.add(key, array);
                    continue;
                }
                attrs.addProperty(key, value);
            }
            json.add("relatedAttrs", attrs);
        }


        JsonObject res = new JsonObject();
        res.add(String.valueOf(System.currentTimeMillis()), json);
        String s = res.toString();
        String finalLog = "," + s.substring(1, s.length() - 1);
        Log.w("DAEAM", "," + s.substring(1, s.length() - 1));
        // 此处目前只能手动调整，因为如果在用户界面调整，需要读取 SharedPreference 内容，性能消耗太大
        if(false) {
            XposedBridge.log("DAEAM" + finalLog);
        }
        clear();
    }

    private void clear() {
        methodName = "";
        methodClass = "";
        behaviorName = "";
        methodArgs = null;
        relatedAttrs = null;
    }

    public static void logError(Throwable t) {
        while (t.getCause() != null) {
            if (t instanceof UnknownHostException) {
                return;
            }
            t = t.getCause();
        }
        XposedBridge.log("Found:" + t);
        for(StackTraceElement st : t.getStackTrace()) {
            XposedBridge.log("catched at:" + st.toString());
            if(checkST(st, "")) {
                break;
            }
        }
    }

    public void setCallingInfo(String callingInfo) {
        callingInfo = "".equals(callingInfo) ? "null---null" : callingInfo;
        callingClass = callingInfo.split("---")[0];
        callingMethod = callingInfo.split("---")[1];
    }

    public static boolean checkST(StackTraceElement st, String methodName) {
        return null == st.getFileName() ||
                "<Xposed>".equals(st.getFileName()) ||
                st.getClassName().contains("EdHooker") ||
                st.getClassName().contains("LSPosed") ||
                methodName.equals(st.getMethodName());
    }
}
