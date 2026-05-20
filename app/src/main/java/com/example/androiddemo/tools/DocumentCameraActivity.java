package com.example.androiddemo.tools;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.androiddemo.R;
import com.example.androiddemo.tools.scanner.NativeClass;
import com.example.androiddemo.tools.scanner.OverlayView;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentCameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private OverlayView overlayView;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private NativeClass nativeClass;
    private boolean opencvAvailable = true;
    private boolean manualMode = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private boolean isCapturing = false;
    private RectF acceptedDetectionBounds = null;
    private double acceptedDetectionScore = 0.0;
    private int missedDetectionFrames = 0;
    private static final long ANALYSIS_INTERVAL_MS = 250;
    private static final double SWITCH_SCORE_RATIO = 1.06;
    private static final double SAME_DOCUMENT_IOU = 0.68;
    private static final double MIN_DEFAULT_AREA_RATIO = 0.70;
    private static final double MAX_DEFAULT_AREA_RATIO = 1.20;
    private static final int MAX_MISSED_DETECTION_FRAMES = 8;
    private static final String TAG = "DocumentCamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_camera);

        manualMode = getIntent().getBooleanExtra("manual_mode", false);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);

        try {
            nativeClass = new NativeClass();
        } catch (Exception e) {
            opencvAvailable = false;
        }

        findViewById(R.id.btnCapture).setOnClickListener(v -> capturePhoto());

        startCamera();
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get();
                bindCameraUseCases();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

        handler.post(analyzePreviewTask);
    }

    // Auto mode: detect document edges and update blue overlay rect.
    // Manual mode: skip detection, blue rect stays centered.
    private final Runnable analyzePreviewTask = new Runnable() {
        @Override
        public void run() {
            if (isCapturing || isFinishing() || isDestroyed()) return;

            if (manualMode) {
                handler.postDelayed(this, ANALYSIS_INTERVAL_MS);
                return;
            }

            if (!opencvAvailable || nativeClass == null) {
                handler.postDelayed(this, ANALYSIS_INTERVAL_MS);
                return;
            }

            Bitmap previewBitmap;
            try {
                previewBitmap = previewView.getBitmap();
            } catch (Exception e) {
                handler.postDelayed(this, ANALYSIS_INTERVAL_MS);
                return;
            }

            if (previewBitmap == null) {
                handler.postDelayed(this, ANALYSIS_INTERVAL_MS);
                return;
            }

            int bitmapW = previewBitmap.getWidth();
            int bitmapH = previewBitmap.getHeight();

            analysisExecutor.execute(() -> {
                try {
                    NativeClass.DetectionResult detection = nativeClass.getScoredPoint(previewBitmap);
                    Bitmap finalBitmap = previewBitmap;

                    handler.post(() -> {
                        try {
                            if (detection != null && detection.points != null && detection.points.rows() == 4) {
                                Point[] pts = detection.points.toArray();
                                float viewW = previewView.getWidth();
                                float viewH = previewView.getHeight();

                                // FILL_CENTER: bitmap→view with uniform aspect-ratio-preserving scale
                                float scale, transX = 0f, transY = 0f;
                                if (viewW * bitmapH > viewH * bitmapW) {
                                    scale = viewH / bitmapH;
                                    transX = (viewW - bitmapW * scale) / 2f;
                                } else {
                                    scale = viewW / bitmapW;
                                    transY = (viewH - bitmapH * scale) / 2f;
                                }

                                Point[] mappedPoints = new Point[pts.length];
                                for (int i = 0; i < pts.length; i++) {
                                    Point pt = pts[i];
                                    float x = (float) pt.x * scale + transX;
                                    float y = (float) pt.y * scale + transY;
                                    mappedPoints[i] = new Point(x, y);
                                }
                                RectF candidateBounds = boundsOf(mappedPoints);
                                if (isReasonablePreviewArea(candidateBounds) && shouldAcceptDetection(candidateBounds, detection.score)) {
                                    overlayView.setPolygon(mappedPoints);
                                    overlayView.onAnalysisFrame(true);
                                    acceptedDetectionBounds = candidateBounds;
                                    acceptedDetectionScore = Math.max(acceptedDetectionScore, detection.score);
                                    missedDetectionFrames = 0;
                                } else {
                                    overlayView.onAnalysisFrame(true);
                                }
                            } else {
                                missedDetectionFrames++;
                                if (missedDetectionFrames > MAX_MISSED_DETECTION_FRAMES) {
                                    acceptedDetectionBounds = null;
                                    acceptedDetectionScore = 0.0;
                                    overlayView.setRect(null);
                                }
                                overlayView.onAnalysisFrame(false);
                            }
                        } finally {
                            finalBitmap.recycle();
                            handler.postDelayed(analyzePreviewTask, ANALYSIS_INTERVAL_MS);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Preview analysis exception", e);
                    handler.post(() -> {
                        overlayView.onAnalysisFrame(false);
                        previewBitmap.recycle();
                        handler.postDelayed(analyzePreviewTask, ANALYSIS_INTERVAL_MS);
                    });
                }
            });
        }
    };

    private boolean shouldAcceptDetection(RectF candidateBounds, double candidateScore) {
        if (acceptedDetectionBounds == null || acceptedDetectionScore <= 0.0) {
            return true;
        }

        double iou = intersectionOverUnion(candidateBounds, acceptedDetectionBounds);
        if (iou >= SAME_DOCUMENT_IOU) {
            acceptedDetectionScore = Math.max(acceptedDetectionScore * 0.96, candidateScore);
            return true;
        }

        return candidateScore > acceptedDetectionScore * SWITCH_SCORE_RATIO;
    }

    private boolean isReasonablePreviewArea(RectF candidateBounds) {
        RectF defaultRect = overlayView.getDefaultRect();
        double defaultArea = area(defaultRect);
        double candidateArea = area(candidateBounds);
        if (defaultArea <= 0.0 || candidateArea <= 0.0) {
            return false;
        }

        double ratio = candidateArea / defaultArea;
        return ratio >= MIN_DEFAULT_AREA_RATIO && ratio <= MAX_DEFAULT_AREA_RATIO;
    }

    private RectF boundsOf(Point[] points) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (Point point : points) {
            float x = (float) point.x;
            float y = (float) point.y;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new RectF(minX, minY, maxX, maxY);
    }

    private double intersectionOverUnion(RectF a, RectF b) {
        float intersectionLeft = Math.max(a.left, b.left);
        float intersectionTop = Math.max(a.top, b.top);
        float intersectionRight = Math.min(a.right, b.right);
        float intersectionBottom = Math.min(a.bottom, b.bottom);
        float intersectionWidth = Math.max(0f, intersectionRight - intersectionLeft);
        float intersectionHeight = Math.max(0f, intersectionBottom - intersectionTop);
        double intersectionArea = intersectionWidth * intersectionHeight;
        double unionArea = area(a) + area(b) - intersectionArea;
        return unionArea > 0.0 ? intersectionArea / unionArea : 0.0;
    }

    private double area(RectF rect) {
        return Math.max(0f, rect.width()) * Math.max(0f, rect.height());
    }

    // Unified capture for both modes: auto mode uses detected corners, manual mode uses the guide rect.
    private void capturePhoto() {
        if (imageCapture == null || isCapturing) return;
        isCapturing = true;

        RectF captureRect = new RectF(overlayView.getRect());
        Point[] capturePolygon = overlayView.getPolygon();
        float captureViewW = previewView.getWidth();
        float captureViewH = previewView.getHeight();

        File rawFile = new File(getCacheDir(), "capture_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(rawFile).build();
        imageCapture.takePicture(outputOptions, analysisExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                try {
                    File resultFile = processCapturedPhoto(rawFile, captureRect, capturePolygon, captureViewW, captureViewH);
                    handler.post(() -> {
                        Intent intent = new Intent();
                        intent.putExtra("result_path", resultFile.getAbsolutePath());
                        setResult(RESULT_OK, intent);
                        finish();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Capture processing failed", e);
                    handler.post(() -> isCapturing = false);
                } finally {
                    if (rawFile.exists()) {
                        rawFile.delete();
                    }
                }
            }

            @Override
            public void onError(ImageCaptureException exception) {
                Log.e(TAG, "ImageCapture failed", exception);
                if (rawFile.exists()) {
                    rawFile.delete();
                }
                handler.post(() -> isCapturing = false);
            }
        });
    }

    private File processCapturedPhoto(File rawFile, RectF captureRect, Point[] capturePolygon,
                                      float captureViewW, float captureViewH) throws IOException {
        Bitmap capturedBitmap = decodeOrientedBitmap(rawFile);
        if (capturedBitmap == null) {
            throw new IOException("Unable to decode captured image");
        }

        try {
            Bitmap result = cropCapturedBitmap(capturedBitmap, captureRect, capturePolygon, captureViewW, captureViewH);
            File resultFile = new File(getCacheDir(), "result_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(resultFile)) {
                result.compress(Bitmap.CompressFormat.JPEG, 98, fos);
            }

            if (result != capturedBitmap) {
                result.recycle();
            }
            return resultFile;
        } finally {
            capturedBitmap.recycle();
        }
    }

    private Bitmap decodeOrientedBitmap(File rawFile) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(rawFile.getAbsolutePath());
        if (bitmap == null) {
            return null;
        }

        ExifInterface exifInterface = new ExifInterface(rawFile.getAbsolutePath());
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        );

        int rotation;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
            default:
                rotation = 0;
                break;
        }

        if (rotation == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotated;
    }

    private Bitmap cropCapturedBitmap(Bitmap capturedBitmap, RectF viewRect, Point[] polygon,
                                      float viewW, float viewH) {
        int bmpW = capturedBitmap.getWidth();
        int bmpH = capturedBitmap.getHeight();

        float scale = Math.max(viewW / bmpW, viewH / bmpH);
        float transX = (viewW - bmpW * scale) / 2f;
        float transY = (viewH - bmpH * scale) / 2f;

        Bitmap result = null;
        if (!manualMode && opencvAvailable && nativeClass != null && polygon != null && polygon.length == 4) {
            try {
                Point[] bitmapPoints = new Point[4];
                for (int i = 0; i < polygon.length; i++) {
                    double x = (polygon[i].x - transX) / scale;
                    double y = (polygon[i].y - transY) / scale;
                    x = Math.max(0, Math.min(x, bmpW - 1));
                    y = Math.max(0, Math.min(y, bmpH - 1));
                    bitmapPoints[i] = new Point(x, y);
                }
                result = nativeClass.getScannedBitmap(
                        capturedBitmap,
                        (float) bitmapPoints[0].x, (float) bitmapPoints[0].y,
                        (float) bitmapPoints[1].x, (float) bitmapPoints[1].y,
                        (float) bitmapPoints[2].x, (float) bitmapPoints[2].y,
                        (float) bitmapPoints[3].x, (float) bitmapPoints[3].y
                );
            } catch (Exception e) {
                Log.w(TAG, "Perspective crop failed, fallback to overlay rect", e);
            }
        }

        if (result != null) {
            return result;
        }

        int left = Math.round((viewRect.left - transX) / scale);
        int top = Math.round((viewRect.top - transY) / scale);
        int right = Math.round((viewRect.right - transX) / scale);
        int bottom = Math.round((viewRect.bottom - transY) / scale);

        left = Math.max(0, Math.min(left, bmpW));
        top = Math.max(0, Math.min(top, bmpH));
        right = Math.max(0, Math.min(right, bmpW));
        bottom = Math.max(0, Math.min(bottom, bmpH));

        if (right > left && bottom > top) {
            return Bitmap.createBitmap(capturedBitmap, left, top, right - left, bottom - top);
        }
        return capturedBitmap;
    }

    private void capturePhotoOldPreviewPath() {
        try {
            Bitmap previewBitmap = previewView.getBitmap();
            if (previewBitmap == null) {
                isCapturing = false;
                return;
            }

            RectF viewRect = overlayView.getRect();
            float viewW = previewView.getWidth();
            float viewH = previewView.getHeight();
            int bmpW = previewBitmap.getWidth();
            int bmpH = previewBitmap.getHeight();

            // Reverse FILL_CENTER: view coords → bitmap coords
            float scale, transX = 0f, transY = 0f;
            if (viewW * bmpH > viewH * bmpW) {
                scale = viewH / bmpH;
                transX = (viewW - bmpW * scale) / 2f;
            } else {
                scale = viewW / bmpW;
                transY = (viewH - bmpH * scale) / 2f;
            }

            Bitmap result = null;
            Point[] polygon = overlayView.getPolygon();
            if (!manualMode && opencvAvailable && nativeClass != null && polygon != null && polygon.length == 4) {
                try {
                    Point[] bitmapPoints = new Point[4];
                    for (int i = 0; i < polygon.length; i++) {
                        double x = (polygon[i].x - transX) / scale;
                        double y = (polygon[i].y - transY) / scale;
                        x = Math.max(0, Math.min(x, bmpW - 1));
                        y = Math.max(0, Math.min(y, bmpH - 1));
                        bitmapPoints[i] = new Point(x, y);
                    }
                    result = nativeClass.getScannedBitmap(
                            previewBitmap,
                            (float) bitmapPoints[0].x, (float) bitmapPoints[0].y,
                            (float) bitmapPoints[1].x, (float) bitmapPoints[1].y,
                            (float) bitmapPoints[2].x, (float) bitmapPoints[2].y,
                            (float) bitmapPoints[3].x, (float) bitmapPoints[3].y
                    );
                } catch (Exception e) {
                    Log.w(TAG, "Perspective crop failed, fallback to overlay rect", e);
                }
            }

            if (result == null) {
                int left = Math.round((viewRect.left - transX) / scale);
                int top = Math.round((viewRect.top - transY) / scale);
                int right = Math.round((viewRect.right - transX) / scale);
                int bottom = Math.round((viewRect.bottom - transY) / scale);

                left = Math.max(0, Math.min(left, bmpW));
                top = Math.max(0, Math.min(top, bmpH));
                right = Math.max(0, Math.min(right, bmpW));
                bottom = Math.max(0, Math.min(bottom, bmpH));

                if (right > left && bottom > top) {
                    result = Bitmap.createBitmap(previewBitmap, left, top, right - left, bottom - top);
                } else {
                    result = previewBitmap;
                }
            }

            File resultFile = new File(getCacheDir(), "result_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(resultFile)) {
                result.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            }

            if (result != previewBitmap) result.recycle();
            previewBitmap.recycle();

            Intent intent = new Intent();
            intent.putExtra("result_path", resultFile.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Capture failed", e);
            isCapturing = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        analysisExecutor.shutdownNow();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        cleanupCacheDir();
    }

    private void cleanupCacheDir() {
        File cacheDir = getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith("result_") || f.getName().startsWith("capture_")) {
                        f.delete();
                    }
                }
            }
        }
    }
}
