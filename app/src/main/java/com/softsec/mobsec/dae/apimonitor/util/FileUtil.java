package com.softsec.mobsec.dae.apimonitor.util;

import android.annotation.SuppressLint;

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
        File file = new File(absolutePath);

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

        }

            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            osw.write(log + '\n');
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

