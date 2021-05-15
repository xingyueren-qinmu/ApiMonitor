package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;

public abstract class MethodHookCallBack extends XC_MethodHook {

    protected MethodHookCallBack() {

    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    //调用栈
    protected String[] getCallingInfo() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();

        String[] result = new String[2];
        result[0] = "";
        result[1] = "";

        String builtInPkgRegex = "^(android\\.|com\\.google\\.android\\.|dalvik\\.|java\\.|javax\\." +
                "|junit\\.|org\\.apache\\.http\\.|org\\.json\\.|org\\.w3c\\.|org\\.xml\\.|org\\.xmlpull\\." +
                "|kotlin\\.|kotlinx\\.|com\\.android\\.|okhttp3\\.|okio\\.)";
        Pattern patternOri = Pattern.compile(builtInPkgRegex);

        String filterPkgRegex = "^(de\\.robv\\.android\\.xposed|" +
                "com\\.softsec\\.mobsec\\.dae\\.apimonitor|" +
                "java\\.lang\\.reflect\\.Proxy|" +
                "\\$Proxy0)";
        Pattern pattern = Pattern.compile(filterPkgRegex);

        StringBuilder sb = new StringBuilder();
        boolean f = false;
        for(StackTraceElement st : stackElements) {
            if(!f && st.getFileName().equals("<Xposed>")) {
                sb.append(st.getClassName()).append('.').append(st.getMethodName()).append(';');
                f = !f;
                continue;
            }
            if(f) {
                Matcher matcher = pattern.matcher(st.getClassName());
                if(!matcher.find()){
                    // 简单过滤调用栈，apimonitor、Xposed一类
                    Matcher matcherOri = patternOri.matcher(st.getClassName());
                    // 过滤掉Android自己的API，剩下第三方sdk包和应用自己的包
                    if(!matcherOri.find() && result[0].equals("")){
                        result[0] = st.getClassName() + "---" + st.getMethodName();
                    }
                    sb.append(st.getClassName()).append('.').append(st.getMethodName()).append(';');
                }
            }
        }

        result[1] = sb.toString();

        return result;
    }



//    //调用栈
//    protected String getCallingInfo() {
//        Throwable ex = new Throwable();
//        StackTraceElement[] stackElements = ex.getStackTrace();
//
//        int index = 0;
//        int startIndex = 0;
//        List<Integer> eleIndex = new ArrayList<>();
//        Class clazz = null;
//        String builtInPkgRegex = "^(android\\.|com\\.google\\.android\\.|dalvik\\.|java\\.|javax\\." +
//                "|junit\\.|org\\.apache\\.http\\.|org\\.json\\.|org\\.w3c\\.|org\\.xml\\.|org\\.xmlpull\\." +
//                "|kotlin\\.|kotlinx\\.|com\\.android\\.)";
//        Pattern pattern = Pattern.compile(builtInPkgRegex);
//
//        for(StackTraceElement st : stackElements) {
//            StringBuffer buffer = new StringBuffer();
//            buffer.append("index: ").append(index).append(" ClassName: " + st.getClassName()).append(" MethodName: "
//                    + st.getMethodName()).append(" FileName: " + st.getFileName());
//            XposedBridge.log(buffer.toString());
//
//
//
//            if(startIndex == 0 && st.getFileName().equals("<Xposed>")){
//                startIndex = index;
//                eleIndex.add(index);
//            }
//
//            if(startIndex > 0 && index > startIndex){
//                Matcher matcher = pattern.matcher(st.getClassName());
//                if(!matcher.find()){
//                    eleIndex.add(index);
//                    if(st.getClassName().startsWith(Config.INTENT_DAE_BC_PKGNAME + ".")){
//                        clazz = st.getClass();
//                        break;
//                    }
//                }
//            }
//
//            index += 1;
//        }
//
//        for(Integer eindex : eleIndex){
//            StringBuffer buffer = new StringBuffer();
//            StackTraceElement st = stackElements[eindex];
//            buffer.append("filtered stack: ").append(" ClassName: " + st.getClassName()).append(" MethodName: "
//                    + st.getMethodName()).append(" FileName: " + st.getFileName());
//            XposedBridge.log(buffer.toString());
//        }
//
//        XposedBridge.log("--------------------------------------------");
//
//        if(null == clazz){
//            return "";
//        }
//
//        return clazz.getPackage().toString() + "---" + clazz.getName();
//    }



}
