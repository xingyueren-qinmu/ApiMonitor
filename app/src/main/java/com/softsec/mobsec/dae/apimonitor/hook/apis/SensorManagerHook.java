package com.softsec.mobsec.dae.apimonitor.hook.apis;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Hook;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.MethodHookCallBack;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Reflector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SensorManagerHook extends Hook {

    private static Map<Integer, String> sensorTypeMap;
    static {
        sensorTypeMap = new HashMap<>();
        sensorTypeMap.put(Sensor.TYPE_GRAVITY, "重力传感器");
        sensorTypeMap.put(Sensor.TYPE_LINEAR_ACCELERATION, "线性加速度传感器");
        sensorTypeMap.put(Sensor.TYPE_ROTATION_VECTOR, "旋转矢量传感器");
        sensorTypeMap.put(Sensor.TYPE_SIGNIFICANT_MOTION, "有效运动传感器");
        sensorTypeMap.put(Sensor.TYPE_STEP_COUNTER, "计步传感器");
        sensorTypeMap.put(Sensor.TYPE_STEP_DETECTOR, "步测器传感器");
    }

    public static final String TAG = "DAEAM_SensorManager";

    @Override
    public void initAllHooks(XC_LoadPackage.LoadPackageParam packageParam) {
        logger.setTag(TAG);
        Method getDefaultSensorMethod = Reflector.findMethod(SensorManager.class, "getDefaultSensor", int.class);
        methodHookImpl.hookMethod(getDefaultSensorMethod, new MethodHookCallBack() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String[] callingInfo = getCallingInfo();
                logger.setCallingInfo(callingInfo[0]);
                logger.addRelatedAttrs("xrefFrom", callingInfo[1]);
                logger.recordAPICalling(param, "获取传感器信息",
                        "类型", sensorTypeMap.get((int)param.args[0]));
            }
        });
    }
}
