package com.softsec.mobsec.dae.apimonitor.util;

import android.annotation.SuppressLint;

import com.softsec.mobsec.dae.apimonitor.hook.hookUtils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinmu997
 */
public class FileUtil {

    private static volatile Map<String, File> fileMap;

    static {
        fileMap = new ConcurrentHashMap<>();
    }

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
            if(!fileMap.containsKey(absolutePath)) {
                synchronized (FileUtil.class) {
                    if(!fileMap.containsKey(absolutePath)) {
                        File file = new File(absolutePath);
                        fileMap.put(absolutePath, file);
                    }
                }
            }
            File file = fileMap.get(absolutePath);
            synchronized (file) {
                boolean f = false;

                if (!file.exists()) {

                    File path = new File(String.valueOf(file.getParentFile()));
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);

                    path.mkdirs();
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);

                    file.createNewFile();

                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                    file.setWritable(true, false);

                    f = true;
                }

                FileOutputStream fos = new FileOutputStream(file, true);
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

    public static void removeKey(String absolutePath) {
        synchronized (FileUtil.class) {
            fileMap.remove(absolutePath);
        }
    }
}

