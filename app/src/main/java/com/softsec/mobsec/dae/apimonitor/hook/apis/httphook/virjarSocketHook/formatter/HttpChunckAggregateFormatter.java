package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter;

import android.text.TextUtils;
import android.util.Log;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketPackEvent;

import java.util.Locale;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.orgApacheCommons.io.output.ByteArrayOutputStream;

public class HttpChunckAggregateFormatter implements EventFormatter {
    @Override
    public void formatEvent(SocketPackEvent socketPackEvent) {
        if (!socketPackEvent.needDecodeHttpBody()) {
            return;
        }
        if (!"chunked".equalsIgnoreCase(socketPackEvent.httpHeaders.get("Transfer-Encoding".toLowerCase(Locale.US)))) {
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int index = 0;
        while (index < socketPackEvent.httpBodyContent.length) {
            int lineEnd = findLineEnd(index, socketPackEvent.httpBodyContent);
            if (lineEnd < index) {
                Log.w("RATEL", "http chunked decode error");
                return;
            }
            String chunkLengthString = new String(socketPackEvent.httpBodyContent, index, lineEnd - index).trim();
            int chunkLength = 0;
            if (!TextUtils.isEmpty(chunkLengthString)) {
                //最后的换行符，会导致最后一次读取到空行
                chunkLength = Integer.parseInt(chunkLengthString, 16);
            }
            if (chunkLength == 0) {
                //the chunck end
                socketPackEvent.httpBodyContent = byteArrayOutputStream.toByteArray();
                return;
            }

            byteArrayOutputStream.write(socketPackEvent.httpBodyContent, lineEnd, chunkLength);
            index = lineEnd + chunkLength;
        }

        Log.w("RATEL", "http chunked decode error");
    }

    private int findLineEnd(int start, byte[] data) {
        int index = start;
        while (index < data.length - 1) {
            if (data[index] == '\r' && data[index + 1] == '\n') {
                return index + 2;
            } else if (data[index] == '\n') {
                return index + 1;
            }
            index++;
        }
        return -1;
    }
}
