package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.observer;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketPackEvent;

public interface EventObserver {
    void onSocketPackageArrival(SocketPackEvent socketPackEvent);
}
