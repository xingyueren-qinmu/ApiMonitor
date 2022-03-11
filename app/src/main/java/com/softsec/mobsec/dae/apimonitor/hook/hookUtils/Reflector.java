package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Reflector {

    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap<>();
    static {
        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
    }

    public static Class findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, true, classLoader);
    }

    public static Method findMethod(String className,
                                    ClassLoader customerLoader,
                                    String methodName,
                                    Object... paramTypes) throws NoSuchMethodException, ClassNotFoundException {
        Class clazz = customerLoader.loadClass(className);
        Method method = clazz.getDeclaredMethod(methodName, getClasses(paramTypes));
        method.setAccessible(true);
        return method;
    }

    public static Method findMethod(Class clazz, String methodName, Object... paramTypes){
        try {
            Class cls = ClassLoader.getSystemClassLoader().loadClass(clazz.getName());
            Method method = cls.getDeclaredMethod(methodName, getClasses(paramTypes));
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            Logger.logError(e);
        }
        return null;
    }

    private static Class<?>[] getClasses(Object[] types) {
        if(types == null) return null;
        Class<?>[] classes = new Class[types.length];
        int i = 0;
        for(Object type : types) {
            if(type instanceof Class) {
                classes[i++] = (Class<?>) type;
                continue;
            }
            if(type instanceof String) classes[i++] = namePrimitiveMap.get(type);
        }
        return classes;
    }

    public static Constructor findConstructor(Class clazz, Class<?>... paramTypes) {

        try {
            Constructor<?> constructor = clazz.getConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            Logger.logError(e);
        }
        return null;
    }

    public static int getFieldInt(Class clazz, Object obj, String filedName){
        try {
            Field field = clazz.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.getInt(obj);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.logError(e);
        }
        return -1;
    }

    public static Object getFieldOjbect(Class clazz,Object obj, String filedName) {
        try {
            Field field = clazz.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.logError(e);
        }
        return null;
    }
}
