package com.example.androiddemo.tools.scanner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.example.androiddemo.tools.scanner.helpers.ImageUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class FilterEngine {

    private static final String TAG = "FilterEngine";

    public static Bitmap applyEnhance(Bitmap src) {
        Log.d(TAG, "applyEnhance entry");
        Mat srcMat = ImageUtils.bitmapToMat(src);
        Mat lab = new Mat();
        Imgproc.cvtColor(srcMat, lab, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(lab, lab, Imgproc.COLOR_RGB2Lab);

        java.util.List<Mat> channels = new java.util.ArrayList<>();
        Core.split(lab, channels);

        // CLAHE on L channel
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(channels.get(0), channels.get(0));

        // Unsharp mask on L channel in Lab space (before RGB conversion)
        Mat lBlurred = new Mat();
        Imgproc.GaussianBlur(channels.get(0), lBlurred, new Size(0, 0), 3);
        Core.addWeighted(channels.get(0), 1.5, lBlurred, -0.5, 0, channels.get(0));
        lBlurred.release();

        Core.merge(channels, lab);
        Imgproc.cvtColor(lab, lab, Imgproc.COLOR_Lab2RGB);

        Bitmap result = ImageUtils.matToBitmap(lab);
        srcMat.release();
        lab.release();
        for (Mat ch : channels) ch.release();
        Log.d(TAG, "applyEnhance done");
        return result;
    }

    public static Bitmap applyBlackWhite(Bitmap src) {
        Log.d(TAG, "applyBlackWhite entry");
        Mat srcMat = ImageUtils.bitmapToMat(src);
        Mat gray = new Mat();
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_RGBA2GRAY);

        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        Mat resultRgba = new Mat();
        Imgproc.cvtColor(binary, resultRgba, Imgproc.COLOR_GRAY2RGBA);

        Bitmap result = ImageUtils.matToBitmap(resultRgba);
        srcMat.release();
        gray.release();
        binary.release();
        resultRgba.release();
        Log.d(TAG, "applyBlackWhite done");
        return result;
    }

    public static Bitmap applyGrayScale(Bitmap src) {
        Log.d(TAG, "applyGrayScale entry");
        Mat srcMat = ImageUtils.bitmapToMat(src);
        Mat gray = new Mat();
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_RGBA2GRAY);

        Mat resultRgba = new Mat();
        Imgproc.cvtColor(gray, resultRgba, Imgproc.COLOR_GRAY2RGBA);

        Bitmap result = ImageUtils.matToBitmap(resultRgba);
        srcMat.release();
        gray.release();
        resultRgba.release();
        Log.d(TAG, "applyGrayScale done");
        return result;
    }

    public static Bitmap applyWatermark(Bitmap src) {
        Log.d(TAG, "applyWatermark entry");
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        float fontSize = Math.max(14f, result.getWidth() * 0.03f);
        float padding = 48f;

        Paint paint = new Paint();
        paint.setColor(0x80FFFFFF);
        paint.setTextSize(fontSize);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        float x = result.getWidth() - padding - paint.measureText("zzh");
        float y = result.getHeight() - padding;
        canvas.drawText("zzh", x, y, paint);

        Log.d(TAG, "applyWatermark done");
        return result;
    }
}
