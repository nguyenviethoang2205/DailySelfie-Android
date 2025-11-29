package com.example.dailyselfie.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.jcodec.api.android.AndroidSequenceEncoder;

import java.io.File;
import java.util.List;

public class TimeLapseHelper {

    private static final int VIDEO_WIDTH = 480;

    public static File createTimeLapse(List<File> imageFiles, File outputFile, int fps) {
        AndroidSequenceEncoder encoder = null;
        try {
            encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, fps);

            for (File imageFile : imageFiles) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    options.inSampleSize = calculateInSampleSize(options, VIDEO_WIDTH, VIDEO_WIDTH);
                    options.inJustDecodeBounds = false;

                    Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    if (originalBitmap != null) {
                        int originalWidth = originalBitmap.getWidth();
                        int originalHeight = originalBitmap.getHeight();
                        float aspectRatio = (float) originalWidth / originalHeight;

                        int targetHeight = (int) (VIDEO_WIDTH / aspectRatio);
                        if (targetHeight % 2 != 0) targetHeight++;
                        if (targetHeight < 2) targetHeight = 2;

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, VIDEO_WIDTH, targetHeight, true);

                        encoder.encodeImage(scaledBitmap);

                        if (originalBitmap != scaledBitmap) {
                            originalBitmap.recycle();
                        }
                        scaledBitmap.recycle();
                    }
                } catch (Exception e) {
                    Log.e("TimeLapse", "Bỏ qua frame lỗi: " + imageFile.getName() + " - " + e.getMessage());
                }
                // ------------------------------------------
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