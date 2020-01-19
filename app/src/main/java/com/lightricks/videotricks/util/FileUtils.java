package com.lightricks.videotricks.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static boolean copyAssetToFilesDir(Context context, String filename) {
        AssetManager manager = context.getAssets();
        try (InputStream in = manager.open(filename)) {
            File file = new File(context.getFilesDir(), filename);
            try (OutputStream out = new FileOutputStream(file)) {
                copyFile(in, out);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
