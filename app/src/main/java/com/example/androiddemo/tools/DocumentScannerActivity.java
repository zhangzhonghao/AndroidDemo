package com.example.androiddemo.tools;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.androiddemo.R;
import com.example.androiddemo.tools.scanner.DpiCompressor;
import com.example.androiddemo.tools.scanner.FilterEngine;
import com.example.androiddemo.tools.scanner.NativeClass;
import com.example.androiddemo.tools.scanner.helpers.ImageUtils;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DocumentScannerActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CAMERA_ACTIVITY = 101;

    private boolean pendingManualMode = false;

    private LinearLayout layoutMain;
    private LinearLayout layoutResult;
    private ImageView ivResult;
    private LinearLayout filterContainer;
    private View btnCamera;
    private View btnCameraManual;
    private View btnGallery;

    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private String currentFilter = "增强";
    private NativeClass nativeClass;
    private boolean opencvAvailable = true;
    private HandlerThread filterThread;
    private Handler filterHandler;
    private boolean isFiltering = false;

    private final String[] filterNames = {"原图", "增强", "黑白", "灰度", "水印"};

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        processGalleryImage(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String path = result.getData().getStringExtra("result_path");
                    if (path != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        if (bitmap != null) {
                            showResult(bitmap);
                        }
                        new File(path).delete();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_scanner);

        try {
            nativeClass = new NativeClass();
        } catch (Exception e) {
            opencvAvailable = false;
        }

        filterThread = new HandlerThread("FilterThread");
        filterThread.start();
        filterHandler = new Handler(filterThread.getLooper());

        layoutMain = findViewById(R.id.layoutMain);
        layoutResult = findViewById(R.id.layoutResult);
        ivResult = findViewById(R.id.ivResult);
        filterContainer = findViewById(R.id.filterContainer);
        btnCamera = findViewById(R.id.btnCamera);
        btnCameraManual = findViewById(R.id.btnCameraManual);
        btnGallery = findViewById(R.id.btnGallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Toolbar toolbarResult = findViewById(R.id.toolbarResult);
        toolbarResult.setNavigationOnClickListener(v -> backToMain());

        btnCamera.setOnClickListener(v -> {
            pendingManualMode = false;
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera(false);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        btnCameraManual.setOnClickListener(v -> {
            pendingManualMode = true;
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera(true);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryLauncher.launch(intent);
        });

        findViewById(R.id.btnSaveImage).setOnClickListener(v -> saveImage());
        findViewById(R.id.btnSavePdf).setOnClickListener(v -> savePdf());
        findViewById(R.id.btnShare).setOnClickListener(v -> showShareDialog());
    }

    private void startCamera(boolean manualMode) {
        Intent intent = new Intent(this, DocumentCameraActivity.class);
        intent.putExtra("manual_mode", manualMode);
        cameraLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(pendingManualMode);
            } else {
                Toast.makeText(this, "需要相机权限才能拍照扫描", Toast.LENGTH_SHORT).show();
                btnCamera.setEnabled(false);
                btnCamera.setAlpha(0.5f);
            }
        }
    }

    private void processGalleryImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (bitmap == null) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            // Try rotate and detect
            Bitmap processed = tryDetectAndCrop(bitmap);
            if (processed == null) processed = bitmap;

            showResult(processed);
        } catch (IOException e) {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap tryDetectAndCrop(Bitmap src) {
        if (!opencvAvailable || nativeClass == null) {
            Toast.makeText(this, "文档检测不可用，已使用原图", Toast.LENGTH_SHORT).show();
            return src;
        }

        // Try four rotations
        for (int i = 0; i < 4; i++) {
            Bitmap rotated = i == 0 ? src : ImageUtils.rotateBitmap(src, 90 * i);
            try {
                MatOfPoint2f points = nativeClass.getPoint(rotated);
                if (points != null && points.rows() == 4) {
                    Point[] pts = points.toArray();
                    return nativeClass.getScannedBitmap(rotated,
                            (float) pts[0].x, (float) pts[0].y,
                            (float) pts[1].x, (float) pts[1].y,
                            (float) pts[2].x, (float) pts[2].y,
                            (float) pts[3].x, (float) pts[3].y);
                }
                if (i > 0) rotated.recycle();
            } catch (Exception e) {
                if (i > 0) rotated.recycle();
            }
        }
        return src;
    }

    private void showResult(Bitmap bitmap) {
        originalBitmap = DpiCompressor.scaleTo300Dpi(bitmap);
        currentFilter = "增强";
        currentBitmap = FilterEngine.applyEnhance(originalBitmap);
        ivResult.setImageBitmap(currentBitmap);

        layoutMain.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
        buildFilterChips();
    }

    private void backToMain() {
        layoutResult.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        originalBitmap = null;
        currentBitmap = null;
    }

    private void buildFilterChips() {
        filterContainer.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int padH = (int) (16 * density);
        int padV = (int) (8 * density);
        int marginEnd = (int) (8 * density);

        for (String name : filterNames) {
            TextView chip = new TextView(this);
            chip.setText(name);
            chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setPadding(padH, padV, padH, padV);
            boolean selected = name.equals(currentFilter);
            chip.setBackgroundResource(selected
                    ? R.drawable.filter_chip_selected : R.drawable.filter_chip_normal);
            chip.setTextColor(selected ? 0xFFFFFFFF : 0xFF212121);
            chip.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(marginEnd);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> applyFilter(name, chip));
            filterContainer.addView(chip);
        }
    }

    private void applyFilter(String name, View chip) {
        if (originalBitmap == null || isFiltering) return;
        isFiltering = true;
        currentFilter = name;
        buildFilterChips();

        filterHandler.post(() -> {
            Bitmap result;
            switch (name) {
                case "原图":
                    result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    break;
                case "增强":
                    result = FilterEngine.applyEnhance(originalBitmap);
                    break;
                case "黑白":
                    result = FilterEngine.applyBlackWhite(originalBitmap);
                    break;
                case "灰度":
                    result = FilterEngine.applyGrayScale(originalBitmap);
                    break;
                case "水印":
                    Bitmap enhanced = null;
                    try {
                        enhanced = FilterEngine.applyEnhance(originalBitmap);
                    } catch (Exception e) {
                        enhanced = null;
                    }
                    if (enhanced != null) {
                        result = FilterEngine.applyWatermark(enhanced);
                    } else {
                        result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    break;
                default:
                    result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    break;
            }

            Bitmap finalResult = result;
            runOnUiThread(() -> {
                currentBitmap = finalResult;
                ivResult.setImageBitmap(currentBitmap);
                isFiltering = false;
            });
        });
    }

    private void saveImage() {
        if (currentBitmap == null) return;
        try {
            byte[] jpegData = DpiCompressor.compressToTargetSize(currentBitmap, 2 * 1024 * 1024, 3 * 1024 * 1024);
            String fileName = "doc_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri)) {
                        fos.write(jpegData);
                    }
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                    Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DocumentScanner");
                dir.mkdirs();
                File file = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(jpegData);
                }
                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, null);
                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdf() {
        if (currentBitmap == null) return;
        try {
            PdfDocument document = new PdfDocument();
            int pageWidth = 595;
            int pageHeight = 842;

            float ratio = (float) pageWidth / currentBitmap.getWidth();
            int imgHeight = (int) (currentBitmap.getHeight() * ratio);
            int yOffset = (pageHeight - imgHeight) / 2;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            page.getCanvas().drawBitmap(currentBitmap, null,
                    new android.graphics.Rect(0, yOffset, pageWidth, yOffset + imgHeight), null);
            document.finishPage(page);

            String fileName = "doc_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri)) {
                        document.writeTo(fos);
                    }
                    values.clear();
                    values.put(MediaStore.Downloads.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                    Toast.makeText(this, "PDF 已保存", Toast.LENGTH_SHORT).show();
                }
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                Toast.makeText(this, "PDF 已保存", Toast.LENGTH_SHORT).show();
            }
            document.close();
        } catch (IOException e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showShareDialog() {
        new AlertDialog.Builder(this)
                .setTitle("分享")
                .setItems(new String[]{"分享图片", "分享 PDF"}, (dialog, which) -> {
                    if (which == 0) shareImage();
                    else sharePdf();
                })
                .show();
    }

    private void shareImage() {
        if (currentBitmap == null) return;
        try {
            byte[] jpegData = DpiCompressor.compressToTargetSize(currentBitmap, 2 * 1024 * 1024, 3 * 1024 * 1024);
            File cacheFile = new File(getCacheDir(), "share_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(jpegData);
            }

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cacheFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "分享图片"));
        } catch (IOException e) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf() {
        if (currentBitmap == null) return;
        try {
            PdfDocument document = new PdfDocument();
            int pageWidth = 595;
            int pageHeight = 842;
            float ratio = (float) pageWidth / currentBitmap.getWidth();
            int imgHeight = (int) (currentBitmap.getHeight() * ratio);
            int yOffset = (pageHeight - imgHeight) / 2;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            page.getCanvas().drawBitmap(currentBitmap, null,
                    new android.graphics.Rect(0, yOffset, pageWidth, yOffset + imgHeight), null);
            document.finishPage(page);

            File cacheFile = new File(getCacheDir(), "share_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                document.writeTo(fos);
            }
            document.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cacheFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "分享 PDF"));
        } catch (IOException e) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (filterThread != null) {
            filterThread.quitSafely();
        }
        cleanupCacheDir();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
    }

    private void cleanupCacheDir() {
        File cacheDir = getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles((dir, name) -> name.startsWith("share_"));
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
    }
}
