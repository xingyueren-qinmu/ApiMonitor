package com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.formatter;

import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.SocketPackEvent;
import com.softsec.mobsec.dae.apimonitor.hook.apis.httphook.virjarSocketHook.orgApacheCommons.lang3.StringUtils;
import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 解析http头部
 */
public class HttpBaseFormatter implements EventFormatter {
    public static final int CR = 13; // <US-ASCII CR, carriage return (13)>
    public static final int LF = 10; // <US-ASCII LF, linefeed (10)>
    public static final int SP = 32; // <US-ASCII SP, space (32)>
    public static final int HT = 9;  // <US-ASCII HT, horizontal-tab (9)>

    private static final int MAX_HEADER_LENGTH = 8129;

    private static final String httpResponseMagic = "HTTP/";
    //:method: GET
    private static final String http2RequestMagic = ":method: ";
    private static final String http2ResponseMagic = ":status: ";

    @Override
    public void formatEvent(SocketPackEvent socketPackEvent) {

        byte[] originBody = socketPackEvent.body;
        if (!formatInternal(socketPackEvent)) {
            socketPackEvent.body = originBody;
            socketPackEvent.isHttp = false;
            socketPackEvent.http2Params.clear();
            socketPackEvent.httpHeaders.clear();
            return;
        }

        //解析一下charset
        String contentType = socketPackEvent.httpHeaders.get("Content-Type".toLowerCase(Locale.US));
        //如果是文本，解析数据的编码集，防止数据乱码
        if (contentType != null && contentType.contains(";")) {
            String[] arr2 = contentType.split(";");
            if (arr2[1].contains("=")) {
                arr2 = arr2[1].split("=");
                try {
                    socketPackEvent.charset = Charset.forName(StringUtils.trimToNull(arr2[1]));
                } catch (UnsupportedCharsetException e) {
                    //ignore
                }
            }
        }

        if (StringUtils.containsIgnoreCase(contentType, "text") || StringUtils.containsIgnoreCase(contentType, "application/json")) {
            //此时，必然是文本，如果charset解析失败，使用默认兜底
            if (socketPackEvent.charset == null) {
                socketPackEvent.charset = StandardCharsets.UTF_8;
            }
        }
        if (StringUtils.startsWithIgnoreCase(contentType, "image/")) {
            socketPackEvent.charset = null;
        }
    }


    private boolean formatInternal(SocketPackEvent socketPackEvent) {
        trimHead(socketPackEvent);

        //报文太短，不可能是http协议
        if (socketPackEvent.body.length < 10) {
            return false;
        }

        // http，请求响应都在这里
        String featureKey = httpFeatureTrie.find(socketPackEvent.body, 0);
        if (featureKey == null) {
            return false;
        }

        int headerEnd = findHeaderEnd(socketPackEvent.body, MAX_HEADER_LENGTH);
        if (headerEnd <= 0) {
            //找不到header区域
            return false;
        }
        BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(socketPackEvent.body, 0, headerEnd)));
        if (!decodeHeader(hin, socketPackEvent)) {
            return false;
        }

        socketPackEvent.httpHeaderContent = new byte[headerEnd];
        System.arraycopy(socketPackEvent.body, 0, socketPackEvent.httpHeaderContent, 0, socketPackEvent.httpHeaderContent.length);

        if (headerEnd < socketPackEvent.body.length) {
            //GET方法没有body
            socketPackEvent.httpBodyContent = new byte[socketPackEvent.body.length - headerEnd];
            System.arraycopy(socketPackEvent.body, headerEnd, socketPackEvent.httpBodyContent, 0, socketPackEvent.httpBodyContent.length);
        }

        socketPackEvent.httpFeatureKey = featureKey;

        return true;
    }

    private boolean decodeHeader(BufferedReader in, SocketPackEvent socketPackEvent) {
        try {
            // Read the request line
            String firstLine = in.readLine();
            if (firstLine == null) {
                // not happen
                return false;
            }

            if (firstLine.startsWith(":")) {
                //http 2.0
                do {
                    if (!firstLine.startsWith(":")) {
                        break;
                    }
                    firstLine = firstLine.substring(1).trim();//consume

                    int p = firstLine.indexOf(':');
                    if (p >= 0) {
                        socketPackEvent.http2Params.put(firstLine.substring(0, p).trim().toLowerCase(Locale.US), firstLine.substring(p + 1).trim());
                    }

                } while ((firstLine = in.readLine()) != null);
            } else {
                socketPackEvent.httpFirstLine = firstLine;
            }

            String line = in.readLine();
            while (line != null && !line.trim().isEmpty()) {
                int p = line.indexOf(':');
                if (p >= 0) {
                    socketPackEvent.httpHeaders.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                }
                line = in.readLine();
            }
            socketPackEvent.isHttp = true;
            return true;
        } catch (IOException ioe) {
            //the exception will not happen
            Logger.logError(ioe);
            return false;
        }
    }


    private static boolean isWhitespace(final byte ch) {
        return ch == HttpBaseFormatter.SP || ch == HttpBaseFormatter.HT || ch == HttpBaseFormatter.CR || ch == HttpBaseFormatter.LF;
    }

    private void trimHead(SocketPackEvent socketPackEvent) {
        int dataIndex = 0;
        for (int i = 0; i < socketPackEvent.body.length; i++) {
            if (isWhitespace(socketPackEvent.body[i])) {
                dataIndex = i;
            } else {
                break;
            }
        }
        if (dataIndex > 0) {
            byte[] newBody = new byte[socketPackEvent.body.length - dataIndex];
            System.arraycopy(socketPackEvent.body, dataIndex, newBody, 0, newBody.length);
            socketPackEvent.body = newBody;
        }
    }

    enum Method {
        GET,
        PUT,
        POST,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE,
        CONNECT,
        PATCH,
        PROPFIND,
        PROPPATCH,
        MKCOL,
        MOVE,
        COPY,
        LOCK,
        UNLOCK;
    }

    private static class Trie {
        private Map<Byte, Trie> values = new HashMap<>();
        private String method = null;

        void addToTree(byte[] data, int index, String worldEntry) {
            if (index >= data.length) {
                //the last
                if (this.method == null) {
                    this.method = worldEntry;
                }
                return;
            }
            Trie trie = values.get(data[index]);
            if (trie == null) {
                trie = new Trie();
                values.put(data[index], trie);
            }
            trie.addToTree(data, index + 1, worldEntry);
        }

        String find(byte[] testData, int index) {
            if (index >= testData.length) {
                return this.method;
            }

            Trie trie = values.get(testData[index]);
            if (trie == null) {
                return this.method;
//                return null;
            }
            return trie.find(testData, index + 1);

        }

    }

    /**
     * :method: GET
     * :authority: game.egame.qq.com
     * :scheme: https
     * <p>
     * :status: 200
     * date: Tue, 22 Oct 2019 06:26:00 GMT
     * content-type: application/json; charset=utf-8
     */
    private static Trie httpFeatureTrie = new Trie();

    static {
        for (Method method : Method.values()) {
            String name = method.name();
            httpFeatureTrie.addToTree(name.getBytes(), 0, name);
        }
        httpFeatureTrie.addToTree(httpResponseMagic.getBytes(), 0, httpResponseMagic);
        httpFeatureTrie.addToTree(http2RequestMagic.getBytes(), 0, http2RequestMagic);
        httpFeatureTrie.addToTree(http2ResponseMagic.getBytes(), 0, http2ResponseMagic);
    }

    /**
     * Find byte index separating header from body. It must be the last byte of
     * the first two sequential new lines.
     */
    public static int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }
            splitbyte++;
        }
        return 0;
    }

}
