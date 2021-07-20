package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.CLogUtils;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookHandler;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.LogInterceptorImp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OkHttpHookInterceptor extends Hook implements InvocationHandler {

    private ArrayList<ClassLoader> AppAllClassLoaderList = new ArrayList<>();
    private Context mOtherContext;
    private ClassLoader mLoader = null;
    //OkHttp里面的 类
    private Class<?> OkHttpBuilder = null;
    private Class<?> OkHttpClient = null;

    private final List<String> AllClassNameList = new ArrayList<>();  //存放 全部类名字的 集合
    public static ArrayList<Class> mClassList = new ArrayList<>();  //存放 全部类的 集合
    private ArrayList<ClassLoader> AppAllCLassLoaderList = new ArrayList<>();
    private boolean isShowStacktTrash = true; //是否打印栈信息的 开关
    int getHttpLoggingInterceptorFlag = 0;
    Class interceptorClass;
    private static Object httpLoggingInterceptor = null; //已经初始化过的拦截器实例

    //拦截器里面的类
    private Class<?> mHttpLoggingInterceptorClass;
    private Class<?> mHttpLoggingInterceptorLoggerClass;
    private Class<?> mHttpLoggingInterceptorLoggerEnum;
    private DexClassLoader mDexClassLoader;
    private Class<?> SocketInputStreamClass;
    private String INTERCEPTORPATH = "/storage/emulated/0/apimonitor/bbb.dex";
    private Object mProxyInstance;

    private static int flag = 0;
    public static final String TAG = "OkHttpHookInterceptor";
//    private static Vector<StringBuilder> QoSstringbuilder = new Vector<>();
//    private static Vector<Integer> QoS = new Vector<>();  // 1为response 2为request
    private static Queue<String> queue = new ConcurrentLinkedQueue<String>();
    private static StringBuilder sb = new StringBuilder();
//    private static int type = 0; // 1为response  2为request


    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {


        HookLoadClass();
        HookAttach();

    }

    //获取应用自己所有的classloader，防止应用加固
    private void HookLoadClass(){
        MethodHookHandler.hookAllMethods(ClassLoader.class, "loadClass", new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if(param == null){ return; }
                Class cls = (Class) param.getResult();
                if(cls == null){ return; }
                for(ClassLoader loader : AppAllClassLoaderList){
                    if(loader.hashCode() == cls.getClassLoader().hashCode()){ return; }
                }
                AppAllClassLoaderList.add(cls.getClassLoader());
                XposedBridge.log("1 HookLoadClass");
            }
        });
    }


    /*
        final void attach(Context context) {
            attachBaseContext(context);
            mLoadedApk = ContextImpl.getImpl(context).mPackageInfo;
        }
    */
    private void HookAttach(){
        Method attachMethod = Reflector.findMethod(Application.class, "attach", Context.class);
        MethodHookHandler.hookMethod(attachMethod, new MethodHookCallBack() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                mOtherContext = (Context) param.args[0];
                mLoader = mOtherContext.getClassLoader();
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String processName = getCurProcessName(mOtherContext);
                if (processName != null && processName.startsWith(mOtherContext.getPackageName())) {
                    HookOKClient();
                }
            }
        });
    }

    /**
     * 获得当前进程的名字
     *
     * @return 进程号
     */
    private String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : Objects.requireNonNull(activityManager).getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


    /**
     * 判断 OkHttp是否存在 混淆 这步骤
     */
    private synchronized void HookOKClient() {

        try {
            if (OkHttpClient == null) {
                OkHttpClient = Class.forName("okhttp3.OkHttpClient", false, mLoader);
            }
        } catch (ClassNotFoundException e) {
            XposedBridge.log("出现异常 发现对方没有使用OkHttp或者被混淆,开始尝试自动获取路径 ");
            HookAndInitProguardClass();
            return;
        }

        try {
            if (OkHttpBuilder == null) {
                OkHttpBuilder = Class.forName("okhttp3.OkHttpClient$Builder", false, mLoader);
            }
        } catch (ClassNotFoundException e) {
            XposedBridge.log("出现异常 发现对方没有使用OkHttp或者被混淆,开始尝试自动获取路径 ");
            HookAndInitProguardClass();
            return;
        }

        if (isExactness()) {
            XposedBridge.log("okHttp本身未混淆");
            HookClientAndBuilderConstructor();
        } else {
            HookAndInitProguardClass();
        }


    }

    /**
     * @return 这个App是否是被混淆的okHttp
     */
    private boolean isExactness() {
        return OkHttpClient.getName().equals("okhttp3.OkHttpClient")
                && OkHttpBuilder.getName().equals("okhttp3.OkHttpClient$Builder")
                //拦截器里面常用的类不等于Null 才可以保证插件正常加载
                && getClass("okio.Buffer") != null
                && getClass("okio.BufferedSource") != null
                && getClass("okio.GzipSource") != null
                && getClass("okhttp3.Request") != null
                && getClass("okhttp3.Response") != null
                && getClass("okio.Okio") != null
                && getClass("okio.Base64") != null
                ;
    }

    private synchronized void HookAndInitProguardClass() {
        //放在子线程去执行，防止卡死

        //先拿到 app里面全部的
        getAllClassName();

        initAllClass();


        //第一步 先开始 拿到 OkHttp 里面的 类  如Client 和 Builder
        getClientClass();
        getBuilder();

        if (OkHttpBuilder != null && OkHttpClient != null) {
            XposedBridge.log("使用了okHttp 开始添加拦截器");
            HookClientAndBuilderConstructor();
        } else {
            XposedBridge.log("对方App可能没有使用okHttp 开始Hook底层方法");
            //可能对方没有使用OkHttp
//            HookGetOutPushStream();
        }

    }

    /**
     * Hook 底层的方法
     * 这个是不管 什么 框架请求都会走的 函数
     */
    private void HookGetOutPushStream() {

        XposedBridge.log("开始Hook底层实现 ");

        try {
            Method socketOutputStreamMethod = Reflector.findMethod(XposedHelpers.findClass("java.net.SocketOutputStream", mLoader), "write", byte[].class, int.class, int.class);
            MethodHookHandler.hookMethod(socketOutputStreamMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    StringBuilder TraceString = new StringBuilder();
                    TraceString.append(new String((byte[]) param.args[0], StandardCharsets.UTF_8));
                    String rawString = TraceString.toString().split("\n\r\n")[0];
                    Map<String, String> header = analyzeRequest(rawString);
//                    String rawRequest = Util.byteArrayToString((byte[]) param.args[0]);
                    String request_raw = Base64.encodeToString((byte[])param.args[0], Base64.NO_WRAP|Base64.NO_PADDING|Base64.URL_SAFE);
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
                    logger.setTag(TAG);
                    logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("request_raw",request_raw);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    logger.recordAPICalling(param, "Socket请求",
                            "method",header.get("method"),
                            "url",header.get("url"),
                            "host",header.get("Host"),
                            "request_size",header.get("Content-Length"),
                            "protocol",header.get("protocol"));
                }
            });

        } catch (Throwable e) {
            XposedBridge.log("HookGetOutPushStream     " + e.toString());
            Logger.logError(e);
        }


        try {
            Method socketInputStreamMethod = Reflector.findMethod(XposedHelpers.findClass("java.net.SocketInputStream", mLoader), "read", byte[].class, int.class, int.class);
            MethodHookHandler.hookMethod(socketInputStreamMethod, new MethodHookCallBack() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    StringBuilder TraceString = new StringBuilder();
                    TraceString.append(new String((byte[]) param.args[0], StandardCharsets.UTF_8));
                    String rawString = TraceString.toString().split("\n\r\n")[0];
                    Map<String, String> result = analyzeResponse(rawString);

//                    String rawResponse = Util.byteArrayToString((byte[]) param.args[0]);
                    String response_raw = Base64.encodeToString((byte[])param.args[0], Base64.NO_WRAP|Base64.NO_PADDING|Base64.URL_SAFE);
                    String[] callingInfo = getCallingInfo(param.method.getName());
                    Logger logger = new Logger();
				logger.setTag(TAG);
				logger.setCallingInfo(callingInfo[0]);
                    logger.addRelatedAttrs("response_raw",response_raw);
                    logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                    logger.recordAPICalling(param, "Socket响应",
                            "code",result.get("code"),
                            "url",result.get("url"),
                            "response_size",result.get("Content-Length"));
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
                String value = s.split(":")[1].replace(" ","");
                if(key.equals("Host")){
                    header.put(key, value);
                }else if(key.equals("Content-Length")){
                    header.put(key, value);
                }
            }else{
                String[] firstLine = s.split("\\s");
                if(firstLine[0].equals("POST") || firstLine[0].equals("GET")){
                    header.put("method",firstLine[0].replace(" ",""));
                    header.put("url",firstLine[1].replace(" ",""));
                    header.put("protocol",firstLine[2].replace(" ",""));
                }
            }
        }

        return header;
    }

    private Map<String, String> analyzeResponse(String rawString){
        Map<String, String> result = new HashMap<String, String>();
        String[] strings = rawString.split("\n");

        result.put("url","unknown");

        String[] firstLine = strings[0].split("\\s");
        result.put("code",firstLine[1]);

        for(String s : strings){
            if(s.contains(":")){
                String key = s.split(":")[0].replace(" ","");
                String value = s.split(":")[1].replace(" ","");
                if(key.equals("Content-Length")){
                    result.put("Content-Length",value);
                    return result;
                }
            }
        }
        return result;
    }

    private synchronized void getAllClassName() {

        //保证每次初始化 之前 保证干净
        AllClassNameList.clear();
        XposedBridge.log("开始 获取全部的类名  ");

        try {
            //系统的 classloader是 Pathclassloader需要 拿到他的 父类 BaseClassloader才有 pathList
            if (mLoader == null) {
                return;
            }
            Field pathListField = mLoader.getClass().getSuperclass().getDeclaredField("pathList");
            if (pathListField != null) {
                pathListField.setAccessible(true);
                Object dexPathList = pathListField.get(mLoader);
                Field dexElementsField = dexPathList.getClass().getDeclaredField("dexElements");
                if (dexElementsField != null) {
                    dexElementsField.setAccessible(true);
                    Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
                    for (Object dexElement : dexElements) {
                        Field dexFileField = dexElement.getClass().getDeclaredField("dexFile");
                        if (dexFileField != null) {
                            dexFileField.setAccessible(true);
                            DexFile dexFile = (DexFile) dexFileField.get(dexElement);
                            getDexFileClassName(dexFile);
                            XposedBridge.log("获取 dexFileField");
                        } else {
                            XposedBridge.log("获取 dexFileField Null ");
                        }
                    }
                } else {
                    XposedBridge.log("获取 dexElements Null ");
                }
            } else {
                XposedBridge.log("获取 pathListField Null ");
            }
        } catch (Throwable e) {
            XposedBridge.log("getAllClassName   Throwable   " + e.toString());
            Logger.logError(e);
        }
    }


    private void getDexFileClassName(DexFile dexFile) {
        if (dexFile == null) {
            return;
        }

        //获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
        Enumeration<String> enumeration = dexFile.entries();
        while (enumeration.hasMoreElements()) {//遍历
            String className = enumeration.nextElement();
            //添加过滤信息
            if (className.contains("okhttp") || className.contains("okio")) {
                AllClassNameList.add(className);
            }
        }
    }


    /**
     * 初始化 需要的 class的 方法
     */
    private void initAllClass() {
        mClassList.clear();

        try {
            XposedBridge.log("需要初始化Class的个数是  " + AllClassNameList.size());

            Class<?> MClass = null;

            for (int i = 0; i < AllClassNameList.size(); i++) {

                MClass = getClass(AllClassNameList.get(i));
                if (MClass != null) {
                    mClassList.add(MClass);
                }

            }

            XposedBridge.log("初始化全部类的个数   " + mClassList.size());
        } catch (Throwable e) {
            XposedBridge.log("initAllClass error " + e.toString());
        }
    }

    /**
     * 遍历当前进程的Classloader 尝试进行获取指定类
     *
     * @param className
     * @return
     */
    private Class getClass(String className) {
        Class<?> aClass = null;
        try {
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException classNotFoundE) {

                try {
                    aClass = Class.forName(className, false, mLoader);
                } catch (ClassNotFoundException e) {
                    Logger.logError(e);
                }
                if (aClass != null) {
                    return aClass;
                }
                try {
                    for (ClassLoader classLoader : AppAllCLassLoaderList) {
                        try {
                            aClass = Class.forName(className, false, classLoader);
                        } catch (Throwable e) {
                            continue;
                        }
                        if (aClass != null) {
                            return aClass;
                        }
                    }
                } catch (Throwable e) {
                    Logger.logError(e);
                }
            }

            return aClass;
        } catch (Throwable e) {

        }
        return null;
    }


    /**
     * 获取 ClientCLass的方法
     */
    private void getClientClass() {
        if (mClassList.size() == 0) {
            XposedBridge.log("全部的集合mClassList的个数为 0  ");
            return;
        }
        XposedBridge.log("开始查找ClientClass");
        try {
            for (Class mClient : mClassList) {
                //判断 集合 个数 先拿到 四个集合 可以 拿到 Client
                if (isClient(mClient)) {
                    OkHttpClient = mClient;
                    return;
                }
            }
        } catch (Throwable e) {
            Logger.logError(e);
        }
        XposedBridge.log("没找到client ");
    }

    private boolean isClient(@NonNull Class<?> mClass) {
        try {
            int typeCount = 0;
            int StaticCount = 0;

            //getDeclaredFields 是个 获取 全部的
            Field[] fields = mClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String type = field.getType().getName();

                //四个 集合 四个final 特征
                if (type.contains(List.class.getName()) && Modifier.isFinal(field.getModifiers())) {
                    typeCount++;
                }
                if (type.contains(List.class.getName()) && Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                    StaticCount++;
                }
            }

            if (StaticCount >= 2 && typeCount == 6 && mClass.getInterfaces().length >= 1) {
                XposedBridge.log("找到OkHttpClient  该类的名字是  " + mClass.getName());
                return true;
            }
        } catch (Throwable e) {
            XposedBridge.log("isClient error " + e.toString());
            Logger.logError(e);
        }
        // mFieldArrayList.clear();
        return false;
    }

    /**
     * 混淆 以后 获取  集合并添加 拦截器的方法
     */
    private void getBuilder() {
        if (OkHttpClient != null) {
            //开始查找 build
            for (Class builder : mClassList) {
                if (isBuilder(builder)) {
                    OkHttpBuilder = builder;
                }
            }
        }
    }

    private boolean isBuilder(@NonNull Class ccc) {

        try {
            int ListTypeCount = 0;
            int FinalTypeCount = 0;
            Field[] fields = ccc.getDeclaredFields();
            for (Field field : fields) {
                String type = field.getType().getName();
                //四个 集合
                if (type.contains(List.class.getName())) {
                    ListTypeCount++;
                }
                //2 个 为 final类型
                if (type.contains(List.class.getName()) && Modifier.isFinal(field.getModifiers())) {
                    FinalTypeCount++;
                }
            }
            //四个 List 两个 2 final  并且 包含父类名字
            if (ListTypeCount == 4 && FinalTypeCount == 2 && ccc.getName().contains(OkHttpClient.getName())) {
                XposedBridge.log(" 找到Builer  " + ccc.getName());
                return true;
            }
        } catch (Throwable e) {
            Logger.logError(e);
        }
        return false;
    }

    /**
     * 当 OkHttpBuilderClass 和OkHttpClient不为Null
     * Hook Client和 Builder 的构造
     * <p>
     * 如果两个都没找到可能没有使用okHttp
     * 尝试Hook 底层函数
     */
    private void HookClientAndBuilderConstructor() {

        try {
            interceptorClass = getInterceptorClass();
            if (interceptorClass == null) {
                XposedBridge.log("HookClientAndBuilderConstructor 出现问题没有拿到解释器类 ");
                return;
            }
        } catch (Throwable e) {
            XposedBridge.log("getInterceptorClass  error " + e.toString());
        }

        if (OkHttpClient != null) {
            try {
                XposedHelpers.findAndHookConstructor(
                        OkHttpClient,
                        OkHttpBuilder,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                XposedBridge.log("Hook 到 构造函数  OkHttpClient");
                                AddInterceptors2(param);
                            }

//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                super.afterHookedMethod(param);
//                                if(flag > 2){  //添加拦截器失败
//                                    XposedBridge.log("开始Hook底层方法");
//                                    HookGetOutPushStream();
//                                }
//                            }
                        });
            } catch (Throwable e) {
                XposedBridge.log("findAndHookConstructor OkHttpClient error " + e.toString());
                Logger.logError(e);
            }
        }

    }

    private Class getInterceptorClass() {
        try {
            Class aClass = getClass("okhttp3.Interceptor");
            if (aClass != null) {
                return aClass;
            }

            for (Class mClass : mClassList) {
                if (isInterceptorClass(mClass)) {
                    XposedBridge.log("找到了 Interceptor 类名是 " + mClass.getName());
                    return mClass;
                }
            }
        } catch (Throwable e) {
            XposedBridge.log("getInterceptorClass error " + e.toString());
        }
        return null;
    }

    /**
     * 判断是否是 Interceptor
     */
    private boolean isInterceptorClass(Class mClass) {
        if (mClass == null) {
            return false;
        }
        try {
            Method[] declaredMethods = mClass.getDeclaredMethods();
            //一个方法 并且 方法参数 是 内部的接口
            if (declaredMethods.length == 1
                    && mClass.isInterface()
            ) {
                Method declaredMethod = declaredMethods[0];
                Class<?>[] parameterTypes = declaredMethod.getParameterTypes();

                return parameterTypes.length == 1 &&
                        parameterTypes[0].getName().contains(mClass.getName()) &&
                        declaredMethod.getExceptionTypes().length == 1 &&
                        declaredMethod.getExceptionTypes()[0].getName().equals(IOException.class.getName());

            }
        } catch (Throwable e) {
            XposedBridge.log("isInterceptorClass error " + e.toString());
        }
        return false;
    }


    /**
     * 核心方法
     * <p>
     * 尝试添加拦截器
     *
     * @param param
     */
    private synchronized void AddInterceptors2(XC_MethodHook.MethodHookParam param) {
        if (interceptorClass == null) {
            XposedBridge.log("interceptorClass == null");
            return;
        }
        try {
//            if(flag <= 2) {
            //找到添加拦截器的方法
            Object httpLoggingInterceptor = getHttpLoggingInterceptorClass();

            if (httpLoggingInterceptor != null) {
                XposedBridge.log("拿到的拦截器,动态代理实现，开始添加名字是 " + httpLoggingInterceptor.getClass().getName());
                OkHttpHookInterceptor.httpLoggingInterceptor = httpLoggingInterceptor;
                //添加拦截器到集合里面
                if (AddInterceptorForList(param, httpLoggingInterceptor)) {
                    XposedBridge.log("添加拦截器完毕");
                }
            } else {
                XposedBridge.log("添加拦截器失败 getHttpLoggingInterceptorClass==null");
//                    flag += 1;
            }
//            }
        } catch (Throwable e) {
            XposedBridge.log("AddInterceptors2 异常 " + e.toString());
        }
    }


    /**
     * 尝试获取拦截器
     *
     * @return
     */
    @NonNull
    private synchronized Object getHttpLoggingInterceptorClass() {
        //防止多次初始化影响性能
        if (httpLoggingInterceptor != null) {
            return httpLoggingInterceptor;
        }

        try {
            //第一步 首先  判断 本身项目里 是否存在 拦截器
            //okhttp3.logging.HttpLoggingInterceptor
            try {
                mHttpLoggingInterceptorClass = getClass("okhttp3.logging.HttpLoggingInterceptor");
                mHttpLoggingInterceptorLoggerClass = getClass("okhttp3.logging.HttpLoggingInterceptor$Logger");

            } catch (Throwable e) {

            }
            if (mHttpLoggingInterceptorClass != null && mHttpLoggingInterceptorLoggerClass != null) {
                XposedBridge.log("拿到了App本身的 拦截器 log");
                return InitInterceptor();
            }

            if (mHttpLoggingInterceptorLoggerClass == null || mHttpLoggingInterceptorClass == null) {
                //当前 App 使用了OkHttp 三种种情况，需分别进行处理
                //1,App没有被混淆，没有拦截器
                if (isExactness()) {
                    XposedBridge.log("当前App的OkHttp没有被混淆,可直接动态添加拦截器");
                    //直接尝试动态加载即可
                    return initLoggingInterceptor();
                } else {
                    XposedBridge.log("当前App的OkHttp被混淆");
                    Object httpLoggingInterceptorForClass = getHttpLoggingInterceptorForClass();
                    if (httpLoggingInterceptorForClass == null) {
                        XposedBridge.log("App Okhttp被混淆 并且没有拦截器");
                        //3,App被混淆，没有拦截器
                        return getHttpLoggingInterceptorImp();
                    } else {
                        //2,App被混淆，有拦截器,根据拦截器特征获取
                        XposedBridge.log("App Okhttp被混淆 存在拦截器");
                        return httpLoggingInterceptorForClass;
                    }
                }

            }

        } catch (Throwable e) {
            XposedBridge.log("getHttpLoggingInterceptor  拦截器初始化出现异常    " + e.toString());
            Logger.logError(e);
        }
        return null;
    }



    private boolean AddInterceptorForList(XC_MethodHook.MethodHookParam param, Object httpLoggingInterceptor)
            throws IllegalAccessException {
        //参数1 是 interceptors 可以修改
        try {
            Object object;
            if (param.args == null || param.args.length == 0) {
                object = param.thisObject;
                XposedBridge.log("object=param.thisObject");
            } else {
                object = param.args[0];
                XposedBridge.log("object=param.args[0] " + param.args.length);
            }
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.getType().getName().equals(List.class.getName())) {
                    Type genericType = field.getGenericType();
                    if (null != genericType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        // 得到泛型里的class类型对象
                        Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
                        if (actualTypeArgument.getName().equals(interceptorClass.getName())) {
                            field.setAccessible(true);
                            List list;
                            if (param.args.length == 0) {
                                list = (List) field.get(param.thisObject);
                            } else {
                                list = (List) field.get(param.args[0]);
                            }

                            list.add(httpLoggingInterceptor);
                            XposedBridge.log("添加拦截器成功");
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            XposedBridge.log("AddInterceptorForList error  " + e.getMessage());
        }
        return false;
    }



    private Object InitInterceptor() throws
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        //通过 动态代理 拿到 这个 接口 实体类
        Object logger;
        logger = Proxy.newProxyInstance(mLoader, new Class[]{mHttpLoggingInterceptorLoggerClass}, OkHttpHookInterceptor.this);
        Object loggingInterceptor = mHttpLoggingInterceptorClass.getConstructor(mHttpLoggingInterceptorLoggerClass).newInstance(logger);
        Object level;
        level = mLoader.loadClass("okhttp3.logging.HttpLoggingInterceptor$Level").getEnumConstants()[3];
        XposedHelpers.findMethodBestMatch(mHttpLoggingInterceptorClass, "setLevel",
                level.getClass()).invoke(loggingInterceptor, level);
        XposedBridge.log("拦截器实例初始化成功  ");
        return loggingInterceptor;
    }



    /**
     * 用 classloader 加载 这个jar 包 防止 classloader 不统一
     */
    private Object initLoggingInterceptor() {
        XposedBridge.log("开始 动态 加载 初始化 ");

        File dexOutputDir = mOtherContext.getDir("dex", 0);

        XposedBridge.log("dexOutputDir  dex  666 " + dexOutputDir.getAbsolutePath());
        // 定义DexClassLoader
        // 第一个参数：是dex压缩文件的路径
        // 第二个参数：是dex解压缩后存放的目录
        // 第三个参数：是C/C++依赖的本地库文件目录,可以为null
        // 第四个参数：是上一级的类加载器
        mDexClassLoader = new DexClassLoader(INTERCEPTORPATH, dexOutputDir.getAbsolutePath(), null, mLoader);
        try {

            if (AddElements()) {

                //mHttpLoggingInterceptor = mDexClassLoader.loadClass("okhttp3.logging.HttpLoggingInterceptor");
                //mHttpLoggingInterceptorLoggerClass = mDexClassLoader.loadClass("okhttp3.logging.HttpLoggingInterceptor$Logger");

                mHttpLoggingInterceptorClass = mLoader.loadClass("okhttp3.logging.HttpLoggingInterceptor");
                mHttpLoggingInterceptorLoggerClass = mLoader.loadClass("okhttp3.logging.HttpLoggingInterceptor$Logger");
                if (mHttpLoggingInterceptorLoggerClass != null && mHttpLoggingInterceptorClass != null) {
                    XposedBridge.log("动态 加载 classloader 成功 ");

                    return InitInterceptor();
                } else {
                    return null;
                }
            }
            return null;


        } catch (Throwable e) {
            XposedBridge.log("initLoggingInterceptor 动态加载异常  " + e.toString());
            Logger.logError(e);
        }
        return null;

    }

    /**
     * @return Dex 是否合并 成功
     */
    private boolean AddElements() {
        //自己的 classloader 里面的 element数组
        Object[] myDexClassLoaderElements = getMyDexClassLoaderElements();
        if (myDexClassLoaderElements == null) {
            XposedBridge.log("AddElements  myDexClassLoaderElements null");
            return false;
        } else {
            XposedBridge.log("AddElements  成功 拿到 myDexClassLoaderElements 自己的Elements 长度是   " + myDexClassLoaderElements.length);
        }
        //系统的  classloader 里面的 element数组
        Object[] classLoaderElements = getClassLoaderElements();
        //将数组合并
        if (classLoaderElements == null) {
            XposedBridge.log("AddElements  classLoaderElements null");
            return false;
        } else {
            XposedBridge.log("AddElements  成功 拿到 classLoaderElements 系统的Elements 长度是   " + classLoaderElements.length);
        }

        //DexElements合并
        Object[] combined = (Object[]) Array.newInstance(classLoaderElements.getClass().getComponentType(),
                classLoaderElements.length + myDexClassLoaderElements.length);

        System.arraycopy(classLoaderElements, 0, combined, 0, classLoaderElements.length);
        System.arraycopy(myDexClassLoaderElements, 0, combined, classLoaderElements.length, myDexClassLoaderElements.length);


        //Object[] dexElementsResut = concat(myDexClassLoaderElements, classLoaderElements);

        if ((classLoaderElements.length + myDexClassLoaderElements.length) != combined.length) {
            XposedBridge.log("合并 elements数组 失败  null");
        }
        //合并成功 重新 加载
        return SetDexElements(combined, myDexClassLoaderElements.length + classLoaderElements.length);
    }



    /**
     * 将自己 创建的 classloader 里面的 内容添加到 原来的 classloader里面
     */
    private Object[] getMyDexClassLoaderElements() {
        try {
            Field pathListField = mDexClassLoader.getClass().getSuperclass().getDeclaredField("pathList");
            if (pathListField != null) {
                pathListField.setAccessible(true);
                Object dexPathList = pathListField.get(mDexClassLoader);
                Field dexElementsField = dexPathList.getClass().getDeclaredField("dexElements");
                if (dexElementsField != null) {
                    dexElementsField.setAccessible(true);
                    Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
                    if (dexElements != null) {
                        return dexElements;
                    } else {
                        XposedBridge.log("AddElements  获取 dexElements == null");
                    }
                    //ArrayUtils.addAll(first, second);
                } else {
                    XposedBridge.log("AddElements  获取 dexElements == null");
                }
            } else {
                XposedBridge.log("AddElements  获取 pathList == null");
            }
        } catch (NoSuchFieldException e) {
            XposedBridge.log("AddElements  NoSuchFieldException   " + e.toString());
            Logger.logError(e);
        } catch (IllegalAccessException e) {
            XposedBridge.log("AddElements  IllegalAccessException   " + e.toString());
            Logger.logError(e);
        }
        return null;
    }

    /**
     * 获取系统的 classaLoder
     */
    private Object[] getClassLoaderElements() {
        try {
            Field pathListField = mLoader.getClass().getSuperclass().getDeclaredField("pathList");
            if (pathListField != null) {
                pathListField.setAccessible(true);
                Object dexPathList = pathListField.get(mLoader);
                Field dexElementsField = dexPathList.getClass().getDeclaredField("dexElements");
                if (dexElementsField != null) {
                    dexElementsField.setAccessible(true);
                    Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
                    if (dexElements != null) {
                        return dexElements;
                    } else {
                        XposedBridge.log("AddElements  获取 dexElements == null");
                    }
                    //ArrayUtils.addAll(first, second);
                } else {
                    XposedBridge.log("AddElements  获取 dexElements == null");
                }
            } else {
                XposedBridge.log("AddElements  获取 pathList == null");
            }
        } catch (NoSuchFieldException e) {
            XposedBridge.log("AddElements  NoSuchFieldException   " + e.toString());
            Logger.logError(e);
        } catch (IllegalAccessException e) {
            XposedBridge.log("AddElements  IllegalAccessException   " + e.toString());
            Logger.logError(e);
        }
        return null;
    }

    /**
     * 将 Elements 数组 set回原来的 classloader里面
     *
     * @param dexElementsResut
     */
    private boolean SetDexElements(Object[] dexElementsResut, int conunt) {
        try {
            Field pathListField = mLoader.getClass().getSuperclass().getDeclaredField("pathList");
            if (pathListField != null) {
                pathListField.setAccessible(true);
                Object dexPathList = pathListField.get(mLoader);
                Field dexElementsField = dexPathList.getClass().getDeclaredField("dexElements");
                if (dexElementsField != null) {
                    dexElementsField.setAccessible(true);
                    //先 重新设置一次
                    dexElementsField.set(dexPathList, dexElementsResut);
                    //重新 get 用
                    Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
                    if (dexElements.length == conunt && Arrays.hashCode(dexElements) == Arrays.hashCode(dexElementsResut)) {
                        return true;
                    } else {
                        XposedBridge.log("合成   长度  " + dexElements.length + "传入 数组 长度   " + conunt);

                        XposedBridge.log("   dexElements hashCode " + Arrays.hashCode(dexElements) + "  " + Arrays.hashCode(dexElementsResut));

                        return false;
                    }
                } else {
                    XposedBridge.log("SetDexElements  获取 dexElements == null");
                }
            } else {
                XposedBridge.log("SetDexElements  获取 pathList == null");
            }
        } catch (NoSuchFieldException e) {
            XposedBridge.log("SetDexElements  NoSuchFieldException   " + e.toString());
            Logger.logError(e);
        } catch (IllegalAccessException e) {
            XposedBridge.log("SetDexElements  IllegalAccessException   " + e.toString());
            Logger.logError(e);
        }
        return false;
    }


    /**
     * App okHttp混淆以后尝试获取混淆以后的拦截器
     * 对HttpLoggingInterceptor 进行判断
     *
     * @return
     */
    private Object getHttpLoggingInterceptorForClass() {
        for (Class MClass : mClassList) {
            if (isHttpLoggingInterceptor(MClass)) {
                mHttpLoggingInterceptorClass = MClass;
                return getOkHttpLoggingInterceptorLogger();
            }
        }
        return null;
    }

    private boolean isHttpLoggingInterceptor(Class MClass) {
        //Class本身是final类型 并且实现了拦截器接口，拦截器接口个数1
        try {
            if (Modifier.isFinal(MClass.getModifiers()) && MClass.getInterfaces().length == 1) {

                Field[] declaredFields = MClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    int setCount = 0;
                    int charSetCount = 0;
                    //  private volatile Set<String> headersToRedact = Collections.emptySet();
                    if (field.getType().getName().equals(Set.class.getName())
                            && Modifier.isPrivate(field.getModifiers())
                            && Modifier.isVolatile(field.getModifiers())
                    ) {
                        setCount++;
                    }
                    //  private static final Charset UTF8 = Charset.forName("UTF-8");
                    if (field.getType().getName().equals(Charset.class.getName())
                            && Modifier.isPrivate(field.getModifiers())
                            && Modifier.isStatic(field.getModifiers())
                            && Modifier.isFinal(field.getModifiers())
                    ) {
                        charSetCount++;
                    }
                    if (setCount == 1 && charSetCount == 1) {
                        XposedBridge.log("发现HttpLoggingInterceptor名字是 " + MClass.getName());
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            Logger.logError(e);
        }
        return false;
    }

    private Object getOkHttpLoggingInterceptorLogger() {
        try {
            if (mHttpLoggingInterceptorClass == null) {
                XposedBridge.log("getOkHttpLoggingInterceptorLogger  mHttpLoggingInterceptor==null");
                return null;
            }
            XposedBridge.log("开始查找 logger 和 Leave");

            for (Class stringName : mClassList) {
                String ClassName = stringName.getName();
                //包含外部类 并且 是 接口
                if (ClassName.contains(mHttpLoggingInterceptorClass.getName() + "$") && stringName.isInterface()) {
                    mHttpLoggingInterceptorLoggerClass = stringName;
                    XposedBridge.log("找到了 mHttpLoggingInterceptorLoggerClass  " + mHttpLoggingInterceptorLoggerClass.getName());
                }
                if (ClassName.contains(mHttpLoggingInterceptorClass.getName() + "$") && stringName.isEnum()) {
                    mHttpLoggingInterceptorLoggerEnum = stringName;
                    XposedBridge.log("找到了 mHttpLoggingInterceptorLoggerEnum   " + mHttpLoggingInterceptorLoggerEnum.getName());
                }
            }
            if (mHttpLoggingInterceptorClass != null &&
                    mHttpLoggingInterceptorLoggerClass != null &&
                    mHttpLoggingInterceptorLoggerEnum != null) {
                return InitInterceptor2();
            } else {
                XposedBridge.log("没有找到 mHttpLoggingInterceptorLoggerClass 和 mHttpLoggingInterceptorLoggerEnum");
            }
        } catch (Throwable e) {
            Logger.logError(e);
        }
        return null;
    }

    private Object InitInterceptor2() {
        try {

            Object logger = Proxy.newProxyInstance(mLoader, new Class[]{mHttpLoggingInterceptorLoggerClass}, OkHttpHookInterceptor.this);

            XposedBridge.log("拿到  动态代理的 class");
            Object loggingInterceptor = mHttpLoggingInterceptorClass.getConstructor(mHttpLoggingInterceptorLoggerClass).newInstance(logger);
            XposedBridge.log("拿到  拦截器的实体类  ");

            Object level = mHttpLoggingInterceptorLoggerEnum.getEnumConstants()[3];
            XposedBridge.log("拿到  Level 枚举   ");

            Method setLevelMethod = getSetLevelMethod(mHttpLoggingInterceptorLoggerEnum);

            if (setLevelMethod == null) {
                XposedBridge.log("没有找到 setLevelMethod 方法体   ");
                //HookGetOutPushStream();
            } else {
                setLevelMethod.setAccessible(true);
                Object invoke = setLevelMethod.invoke(loggingInterceptor, level);
                XposedBridge.log("调用 setLevel成功  返回对应的  对象    " + invoke.getClass().getName());
                return invoke;
            }


        } catch (Throwable e) {
            XposedBridge.log("拦截器初始化出现异常  InitInterceptor2  " + e.toString());
            Logger.logError(e);
        }
        return null;
    }

    @Nullable
    private Method getSetLevelMethod(Class level) {
        XposedBridge.log("getSetLevelMethod 需要的参数类型 是 " + level.getName());

        Method[] declaredMethods = mHttpLoggingInterceptorClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            XposedBridge.log("该方法的 名字是   " + declaredMethod.getName() + "  参数的 个数是  " + parameterTypes.length);
            if (parameterTypes.length == 1) {
                XposedBridge.log("长度是 1 参数类型是  " + parameterTypes[0].getName());
                //比较 传入的类型 是 HttpLoggingInterceptor.Level 类的 方法
                if (parameterTypes[0].getName().equals(level.getName())) {
                    return declaredMethod;
                }
            }
        }
        return null;
    }

    /**
     * 自实现 loggerInterceptor 类
     */
    private Object getHttpLoggingInterceptorImp() {

        if (mProxyInstance != null) {
            return mProxyInstance;
        }
        try {
            if (interceptorClass != null) {
                XposedBridge.log("在init函数执行之前，拦截器Class名字  " + interceptorClass.getName());
                if (LogInterceptorImp.init(mClassList, interceptorClass, mLoader)) {
                    LogInterceptorImp logInterceptorImp = new LogInterceptorImp();
                    Object proxyInstance = Proxy.newProxyInstance(mLoader, new Class[]{interceptorClass}, logInterceptorImp);
                    mProxyInstance = proxyInstance;
                    return proxyInstance;
                } else {
                    //动态初始化拦截器失败
                    XposedBridge.log("动态初始化My拦截器失败");
                }
                return null;
            } else {
                XposedBridge.log("getInterceptorClass 返回 Null");
                return null;
            }
        } catch (Throwable e) {
            XposedBridge.log("getHttpLoggingInterceptorImp error " + e.toString());
            Logger.logError(e);
        }
        return null;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String msg = (String) args[0];
        boolean outpush = false;

        queue.add(msg);

        synchronized(queue) {
            int type = 0;
            if(!queue.isEmpty()) {
                String curMsg = queue.poll();
                sb.append(curMsg).append("\n");
                if(curMsg.startsWith("<--")){  //response
                    type = 1;
                    if(curMsg.startsWith("<-- END")){
                        outpush = true;
                    }else{
                        sb = new StringBuilder();
                        sb.append(curMsg).append("\n");
                    }
                }else if(msg.startsWith("-->")){   //request
                    type = 2;
                    if(msg.startsWith("--> END")){
                        outpush = true;
                    }else{
                        sb = new StringBuilder();
                        sb.append(curMsg).append("\n");
                    }
                }
            }

            if(outpush){
                Throwable ex = new Throwable();
                StackTraceElement[] stackElements = ex.getStackTrace();
                String[] callingInfo = getCalling(stackElements);

                if(type == 1){
                    CLogUtils.NetLogger("Response:\n" + sb.toString() + "StackElements:\n" + callingInfo[0] + "\nxrefFrom:\n" + callingInfo[1]);
                }else if(type == 2){
                    CLogUtils.NetLogger("Request:\n" + sb.toString() + "StackElements:\n" + callingInfo[0] + "\nxrefFrom:\n" + callingInfo[1]);
                }
//                type = 0;
            }
        }


//        if(msg.startsWith("<--")){  //response
//            if(msg.startsWith("<-- END")){
//                outpush = true;
//            }else{
//                StringBuilder responseStringBuilder = new StringBuilder();
//                QoSstringbuilder.add(responseStringBuilder);
//                QoS.add(1);
//            }
//        }else if(msg.startsWith("-->")){   //request
//            if(msg.startsWith("--> END")){
//                outpush = true;
//            }else{
//                StringBuilder requestStringBuilder = new StringBuilder();
//                QoSstringbuilder.add(requestStringBuilder);
//                QoS.add(2);
//            }
//        }
//
//        if(QoSstringbuilder.size() > 0){
//            int index = -1;
//            for(int i = QoS.size(); i>=0; i--){
//                if(QoS.get(i) > 0){
//                    index = i;
//                    break;
//                }
//            }
//            CLogUtils.NetLoggerOri("index: " + index);
//            if(index > -1){
//                QoSstringbuilder.get(index).append(msg).append("\n");
//                if(outpush){
//                    if(QoS.get(index) == 1){
//                        CLogUtils.NetLogger("Response:\n" + QoSstringbuilder.get(index).toString());
//                    }else if(QoS.get(index) == 2){
//                        CLogUtils.NetLogger("Request:\n" + QoSstringbuilder.get(index).toString());
//                    }
////                QoSstringbuilder.remove(index);
////                QoS.remove(index);
//                    QoS.set(index, 0);
//                    CLogUtils.NetLoggerOri("编程2：" + QoSstringbuilder.size());
//                }
//            }
//
//        }

        return null;
    }


    //调用栈
    protected String[] getCalling(StackTraceElement[] stackElements) {
        int index = 0;
        int startIndex = 0;
        List<Integer> eleIndex = new ArrayList<>();
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";

        String builtInPkgRegex = "^(android\\.|com\\.google\\.android\\.|dalvik\\.|java\\.|javax\\." +
                "|junit\\.|org\\.apache\\.http\\.|org\\.json\\.|org\\.w3c\\.|org\\.xml\\.|org\\.xmlpull\\." +
                "|kotlin\\.|kotlinx\\.|com\\.android\\.|okhttp3\\.)";
        Pattern patternOri = Pattern.compile(builtInPkgRegex);

        String filterPkgRegex = "^(de\\.robv\\.android\\.xposed|" +
                "com\\.softsec\\.mobsec\\.dae\\.apimonitor|" +
                "java\\.lang\\.reflect\\.Proxy|" +
                "\\$Proxy0)";
        Pattern pattern = Pattern.compile(filterPkgRegex);

        for(StackTraceElement st : stackElements) {
            //apimonitor hook的函数，即起始点
            if(startIndex == 0 && st.getClassName().startsWith("$Proxy0")){
                startIndex = index;
                eleIndex.add(index);
            }

            //之后调用栈过滤，利用filterPkgRegex
            if(startIndex > 0 && index > startIndex){
                Matcher matcher = pattern.matcher(st.getClassName());
                if(!matcher.find()){
                    eleIndex.add(index);  //简单过滤调用栈，apimonitor、xposed一类
                    Matcher matcherOri = patternOri.matcher(st.getClassName());
                    if(!matcherOri.find() && result[0].equals("")){  //过滤掉Android自己的API,剩下第三方sdk包和应用自己的包
                        String[] strSplits = st.getClassName().split("\\.");
                        if(strSplits.length > 0){
                            result[0] = st.getClassName().replace(strSplits[strSplits.length-1],"") + "---" + st.getMethodName();
                        }
                    }

                }
            }
            index += 1;
        }

        StringBuffer buffer1 = new StringBuffer();
        for(Integer eindex : eleIndex){
            StackTraceElement st1 = stackElements[eindex];
            buffer1.append(" ClassName: " + st1.getClassName()).append(" MethodName: "
                    + st1.getMethodName()).append(" FileName: " + st1.getFileName()).append("\n");
        }
        result[1] = buffer1.toString();


        return result;
    }

}
