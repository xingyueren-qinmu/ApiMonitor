package com.softsec.mobsec.dae.apimonitor.util;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;

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
            out = sb.toString().replace("\n","");

        } else {
            out = Base64.encodeToString(input, Base64.NO_WRAP);
        }

        return out;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDate() {
        return new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date());
    }

    public static String execRootCmdWithResult(String cmd) {
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
            Logger.logError(e);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    Logger.logError(e);
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    Logger.logError(e);
                }
            }
        }
        return result.toString();
    }

    public static int execRootCmdWithExitValue(String cmd) {
        Log.i("CMD", cmd);
        DataOutputStream dos = null;
        DataInputStream dis = null;
        int exitValue = -1;
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
            p.waitFor();
            exitValue = p.exitValue();
        } catch (Exception e) {
            Logger.logError(e);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    Logger.logError(e);
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    Logger.logError(e);
                }
            }
        }
        return exitValue;
    }
}