package com.softsec.mobsec.dae.apimonitor.util;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * @author qinmu997
 */
public class Util {

    public static String byteArrayToString(byte[] input) {
        if(input==null) {
            return "";
        }
        String out = new String(input);
        int tmp = 0;
        for (int i = 0; i < out.length(); i++) {
            int c = out.charAt(i);

            if (c >= 32 && c < 127) {
                tmp ++;
            }
        }

        if (tmp > (out.length() * 0.60)) {
            StringBuilder sb = new StringBuilder();
            for (byte b : input) {
                if (b >= 32 && b < 127) {
                    sb.append(String.format("%c", b));
                } else {
                    sb.append('.');
                }
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

    public static String execRootCmd(String cmd) {
        Log.i("CMD", cmd);
        StringBuilder result = new StringBuilder();
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            // 经过Root处理的android系统即有su命令
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());
            Scanner scanner = new Scanner(dis);
            dos.writeBytes(cmd +"\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line;
            if(scanner.hasNext()) {
                while ((line = scanner.nextLine()) != null) {
                    result.append(line);
                }
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }
}