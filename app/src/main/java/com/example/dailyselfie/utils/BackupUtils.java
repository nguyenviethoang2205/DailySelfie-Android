package com.example.dailyselfie.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupUtils {

    public static File createBackupZip(Context context) {
        File sourceFolder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (sourceFolder == null || sourceFolder.listFiles() == null || sourceFolder.listFiles().length == 0) {
            return null;
        }
        File zipFile = new File(context.getExternalCacheDir(), "DailySelfie_Backup.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            File[] files = sourceFolder.listFiles();
            byte[] buffer = new byte[2048];
            for (File file : files) {
                if (file.isFile() && !file.getName().startsWith(".")) {
                    FileInputStream fis = new FileInputStream(file);
                    ZipEntry entry = new ZipEntry(file.getName());
                    entry.setTime(file.lastModified());
                    zos.putNextEntry(entry);

                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
            return zipFile;
        } catch (Exception e) {
            Log.e("Backup", "Error", e);
            return null;
        }
    }

    public static boolean restoreFromZip(Context context, Uri zipUri) {
        File targetFolder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try (InputStream is = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {

            ZipEntry ze;
            byte[] buffer = new byte[2048];

            while ((ze = zis.getNextEntry()) != null) {
                File newFile = new File(targetFolder, ze.getName());

                if (newFile.exists()) {
                    newFile.delete();
                }

                FileOutputStream fos = new FileOutputStream(newFile);
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                fos.close();

                long time = ze.getTime();
                if (time > 0) {
                    newFile.setLastModified(time);
                }

                zis.closeEntry();
            }
            return true;
        } catch (Exception e) {
            Log.e("Restore", "Error", e);
            return false;
        }
    }
}