package com.example.androiddemo.tools.scanner;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class DpiCompressor {

    private static final int TARGET_SHORT_SIDE_PX = 2480; // ~300 DPI for A4 short side

    public static Bitmap scaleTo300Dpi(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int shortSide = Math.min(width, height);

        if (shortSide >= TARGET_SHORT_SIDE_PX) {
            return src;
        }

        float scale = (float) TARGET_SHORT_SIDE_PX / shortSide;
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
    }

    public static byte[] compressToTargetSize(Bitmap src, int minBytes, int maxBytes) {
        int qualityHigh = 100;
        int qualityLow = 50;
        byte[] lastBelowMax = null;
        byte[] result = null;

        while (qualityHigh - qualityLow > 1) {
            int mid = (qualityHigh + qualityLow) / 2;
            byte[] data = compress(src, mid);

            if (data == null) break;

            int size = data.length;

            if (size > maxBytes) {
                qualityLow = mid;
            } else if (size < minBytes) {
                qualityHigh = mid;
                lastBelowMax = data;
            } else {
                result = data;
                break;
            }
        }

        if (result == null) {
            result = compress(src, qualityLow);
            if (result != null && result.length > maxBytes && lastBelowMax != null) {
                result = lastBelowMax;
            }
        }

        return result != null ? result : compress(src, 85);
    }

    private static byte[] compress(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)) {
            return null;
        }
        return baos.toByteArray();
    }
}
