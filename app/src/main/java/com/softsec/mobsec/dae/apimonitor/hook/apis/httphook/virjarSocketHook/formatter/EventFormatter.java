package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketPackEvent;

public interface EventFormatter {
    String HTTP_BASE = "http_base";
    String HTTP_CHUNKED_AGGRE = "http_chuncked_aggre";

    String HTTP_UNZIP_GZIP = "http_unzip_gzip";

    String HTTP_1_1_REQUEST = "http_1.1_req";

    void formatEvent(SocketPackEvent socketPackEvent);
}
