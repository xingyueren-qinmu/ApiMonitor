package com.softsec.mobsec.dae.apimonitor.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    public static String byteArrayToString(byte[] input) {
        if(input==null)
            return "";
        String out = new String(input);
        int tmp = 0;
        for (int i = 0; i < out.length(); i++) {
            int c = out.charAt(i);

            if (c >= 32 && c < 127) {
                tmp++;
            }
        }

        if (tmp > (out.length() * 0.60)) {
            StringBuilder sb = new StringBuilder();
            for (byte b : input) {
                if (b >= 32 && b < 127)
                    sb.append(String.format("%c", b));
                else
                    sb.append('.');
            }
            out = sb.toString();

        } else {
            out = Base64.encodeToString(input, Base64.NO_WRAP);
        }

        return out;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDate() {
        return new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date());
    }
}