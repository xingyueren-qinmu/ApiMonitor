package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import java.util.Date;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class Hook {

    protected Logger logger = new Logger();
    protected MethodHookHandler methodHookImpl = new MethodHookHandler();

    public abstract void initAllHooks(final XC_LoadPackage.LoadPackageParam packageParam);

    protected String[] hookMonitorCallbackHandler(String packageName, int actionhook, String methodName, String log){
        Date time = new Date();
        return null;
    }

}
