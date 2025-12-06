package com.example.dailyselfie.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupUtils {

    private static final String PREF_NOTES_NAME = "my_photo_notes";
    private static final String NOTES_FILE_IN_ZIP = "notes_backup.json";

    public static File createBackupZip(Context context) {
        File sourceFolder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (sourceFolder == null || sourceFolder.listFiles() == null) {
            return null;
        }

        File zipFile = new File(context.getExternalCacheDir(), "DailySelfie_Backup.zip");

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {

            File[] files = sourceFolder.listFiles();
            byte[] buffer = new byte[2048];
            for (File file : files) {
                if (file.isFile() && !file.getName().startsWith(".") && !file.getName().equals(NOTES_FILE_IN_ZIP)) {
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

            SharedPreferences prefs = context.getSharedPreferences(PREF_NOTES_NAME, Context.MODE_PRIVATE);
            Map<String, ?> allNotes = prefs.getAll();

            if (!allNotes.isEmpty()) {
                JSONObject jsonNotes = new JSONObject();

                for (Map.Entry<String, ?> entry : allNotes.entrySet()) {

                    String fullPath = entry.getKey();
                    String fileName = new File(fullPath).getName();
                    String noteContent = entry.getValue().toString();

                    jsonNotes.put(fileName, noteContent);
                }

                ZipEntry noteEntry = new ZipEntry(NOTES_FILE_IN_ZIP);
                zos.putNextEntry(noteEntry);
                zos.write(jsonNotes.toString().getBytes());
                zos.closeEntry();
            }

            return zipFile;
        } catch (Exception e) {
            Log.e("Backup", "Lỗi Backup", e);
            return null;
        }
    }

    public static boolean restoreFromZip(Context context, Uri zipUri) {
        File targetFolder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NOTES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try (InputStream is = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {

            ZipEntry ze;
            byte[] buffer = new byte[2048];

            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();

                if (fileName.equals(NOTES_FILE_IN_ZIP)) {
                    StringBuilder jsonString = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonString.append(line);
                    }

                    JSONObject jsonNotes = new JSONObject(jsonString.toString());
                    Iterator<String> keys = jsonNotes.keys();

                    while (keys.hasNext()) {
                        String keyFileName = keys.next();
                        String noteValue = jsonNotes.getString(keyFileName);

                        File localFile = new File(targetFolder, keyFileName);
                        editor.putString(localFile.getAbsolutePath(), noteValue);
                    }
                    editor.apply();
                }
                else {
                    File newFile = new File(targetFolder, fileName);

                    if (newFile.exists()) newFile.delete();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int count;
                    while ((count = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                    fos.flush();
                    fos.close();

                    long time = ze.getTime();
                    if (time > 0) newFile.setLastModified(time);
                }
                zis.closeEntry();
            }

            return true;
        } catch (Exception e) {
            Log.e("Restore", "Lỗi Restore", e);
            return false;
        }
    }
}