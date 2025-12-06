package com.example.dailyselfie.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import org.jcodec.api.android.AndroidSequenceEncoder;

import java.io.File;
import java.util.List;

public class TimeLapseHelper {

    private static final int VIDEO_WIDTH = 480;
    private static final int VIDEO_HEIGHT = 640;

    public static File createTimeLapse(List<File> imageFiles, File outputFile, int fps) {
        AndroidSequenceEncoder encoder = null;
        try {
            encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, fps);

            for (File imageFile : imageFiles) {
                Bitmap finalFrame = null;
                Bitmap sourceBitmap = null;

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    options.inSampleSize = calculateInSampleSize(options, VIDEO_WIDTH, VIDEO_HEIGHT);
                    options.inJustDecodeBounds = false;

                    sourceBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    if (sourceBitmap != null) {
                        finalFrame = Bitmap.createBitmap(VIDEO_WIDTH, VIDEO_HEIGHT, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(finalFrame);
                        canvas.drawColor(Color.BLACK);

                        float scaleX = (float) VIDEO_WIDTH / sourceBitmap.getWidth();
                        float scaleY = (float) VIDEO_HEIGHT / sourceBitmap.getHeight();
                        float scale = Math.min(scaleX, scaleY);

                        int newWidth = Math.round(sourceBitmap.getWidth() * scale);
                        int newHeight = Math.round(sourceBitmap.getHeight() * scale);

                        int left = (VIDEO_WIDTH - newWidth) / 2;
                        int top = (VIDEO_HEIGHT - newHeight) / 2;

                        Rect destRect = new Rect(left, top, left + newWidth, top + newHeight);
                        canvas.drawBitmap(sourceBitmap, null, destRect, null);

                        encoder.encodeImage(finalFrame);
                    }

                } catch (Exception e) {
                    Log.e("TimeLapse", "Lỗi frame: " + imageFile.getName() + " - " + e.getMessage());
                } finally {
                    if (sourceBitmap != null && !sourceBitmap.isRecycled()) {
                        sourceBitmap.recycle();
                    }
                    if (finalFrame != null && !finalFrame.isRecycled()) {
                        finalFrame.recycle();
                    }
                }
            }

            encoder.finish();
            return outputFile;

        } catch (Exception e) {
            Log.e("TimeLapse", "Lỗi tổng thể", e);
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}