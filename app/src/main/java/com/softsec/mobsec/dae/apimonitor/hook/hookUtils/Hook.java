package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class Hook {

    protected Logger logger = new Logger();
    protected MethodHookHandler methodHookImpl = new MethodHookHandler();

    public abstract void initAllHooks(final XC_LoadPackage.LoadPackageParam packageParam);



}
