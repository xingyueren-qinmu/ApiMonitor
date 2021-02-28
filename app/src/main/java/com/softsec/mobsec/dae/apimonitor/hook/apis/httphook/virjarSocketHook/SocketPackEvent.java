package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个网络报文事件
 */
public class SocketPackEvent {
    public Socket socket;
    public Throwable stackTrace;
    public byte[] body;

    public int readAndWrite;

    public boolean isHttp = false;


    /**
     * 第一行，不是header区域，如在响应中：HTTP/1.1 200 OK ，如在请求中：GET /path?query
     */
    public String httpFirstLine = null;

    public String httpFeatureKey = null;

    /**
     * 在http2.0,firstLine的数据会分成多行了，而且是一个map
     */
    public Map<String, String> http2Params = new HashMap<>();

    public Map<String, String> httpHeaders = new HashMap<>();

    public byte[] httpHeaderContent = null;

    public byte[] httpBodyContent = null;

    public Charset charset;


    public boolean needDecodeHttpBody() {
        if (!isHttp) {
            return false;
        }

        return httpBodyContent != null && httpBodyContent.length != 0;
    }
}
