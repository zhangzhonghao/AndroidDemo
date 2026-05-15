package com.example.androiddemo.tools;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
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
    private static final long ANALYSIS_INTERVAL_MS = 250;
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
                    MatOfPoint2f points = nativeClass.getPoint(previewBitmap);
                    Bitmap finalBitmap = previewBitmap;

                    handler.post(() -> {
                        try {
                            if (points != null && points.rows() == 4) {
                                Point[] pts = points.toArray();
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

                                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
                                float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
                                for (Point pt : pts) {
                                    float x = (float) pt.x * scale + transX;
                                    float y = (float) pt.y * scale + transY;
                                    if (x < minX) minX = x;
                                    if (y < minY) minY = y;
                                    if (x > maxX) maxX = x;
                                    if (y > maxY) maxY = y;
                                }
                                overlayView.setRect(new RectF(minX, minY, maxX, maxY));
                                overlayView.onAnalysisFrame(true);
                            } else {
                                overlayView.setRect(null);
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

    // Unified capture for both modes: crop preview bitmap to the current overlay rect
    private void capturePhoto() {
        if (imageCapture == null || isCapturing) return;
        isCapturing = true;

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

            int left = Math.round((viewRect.left - transX) / scale);
            int top = Math.round((viewRect.top - transY) / scale);
            int right = Math.round((viewRect.right - transX) / scale);
            int bottom = Math.round((viewRect.bottom - transY) / scale);

            left = Math.max(0, Math.min(left, bmpW));
            top = Math.max(0, Math.min(top, bmpH));
            right = Math.max(0, Math.min(right, bmpW));
            bottom = Math.max(0, Math.min(bottom, bmpH));

            Bitmap result;
            if (right > left && bottom > top) {
                result = Bitmap.createBitmap(previewBitmap, left, top, right - left, bottom - top);
            } else {
                result = previewBitmap;
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
                    if (f.getName().startsWith("result_")) {
                        f.delete();
                    }
                }
            }
        }
    }
}
