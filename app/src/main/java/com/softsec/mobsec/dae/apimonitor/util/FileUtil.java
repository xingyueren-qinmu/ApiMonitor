package com.softsec.mobsec.dae.apimonitor.util;

import android.annotation.SuppressLint;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author qinmu997
 */
public class FileUtil {

    @SuppressLint("SetWorldReadable")
    public static void fixSharedPreference() {
        File folder = new File(Config.PATH_DAEAM_INTERNAL);
        folder.setExecutable(true, false);

        String mPrefFile = Config.PATH_DAEAM_INTERNAL + Config.PATH_SP + Config.SP_NAME + ".xml";
        (new File(mPrefFile)).setReadable(true, false);
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    public static void writeToFile(String log, String absolutePath) {
        try {
            synchronized (FileUtil.class) {
                boolean f = false;

                File logFile = new File(absolutePath);
                if (!logFile.exists()) {

                    File path = new File(String.valueOf(logFile.getParentFile()));
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);

                    path.mkdirs();
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);

                    logFile.createNewFile();

                    logFile.setReadable(true, false);
                    logFile.setExecutable(true, false);
                    logFile.setWritable(true, false);

                    f = true;
                }

                FileOutputStream fos = new FileOutputStream(logFile, true);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                if(f) {
                    osw.write("{");
                    if(log.startsWith(",")) {
                        log = log.substring(1);
                    }
                }

                osw.write(log);
                osw.close();
                fos.close();
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
    }
}

