package com.example.androiddemo.tools;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.androiddemo.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRGeneratorActivity extends AppCompatActivity {

    private EditText etContent;
    private RadioGroup rgSize;
    private CheckBox cbWithLogo;
    private ImageView ivQrCode;
    private TextView tvPlaceholder;
    private Button btnSave;
    private Button btnShare;

    private Bitmap currentQrBitmap;

    // 二维码尺寸（像素）
    private static final int SIZE_SMALL = 256;
    private static final int SIZE_MEDIUM = 512;
    private static final int SIZE_LARGE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etContent = findViewById(R.id.et_content);
        rgSize = findViewById(R.id.rg_size);
        cbWithLogo = findViewById(R.id.cb_with_logo);
        ivQrCode = findViewById(R.id.iv_qr_code);
        tvPlaceholder = findViewById(R.id.tv_placeholder);
        btnSave = findViewById(R.id.btn_save);
        btnShare = findViewById(R.id.btn_share);

        // 默认选中中等尺寸
        RadioButton rbMedium = findViewById(R.id.rb_medium);
        rbMedium.setChecked(true);

        // 初始状态下保存和分享按钮不可用
        btnSave.setEnabled(false);
        btnShare.setEnabled(false);
    }

    private void setupListeners() {
        // 生成按钮
        findViewById(R.id.btn_generate).setOnClickListener(v -> generateQRCode());

        // 保存按钮
        btnSave.setOnClickListener(v -> saveQRCode());

        // 分享按钮
        btnShare.setOnClickListener(v -> shareQRCode());
    }

    private void generateQRCode() {
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取选中的尺寸
        int size = getSelectedSize();

        // 获取是否带 Logo
        boolean withLogo = cbWithLogo.isChecked();

        try {
            currentQrBitmap = createQRBitmap(content, size, withLogo);
            ivQrCode.setImageBitmap(currentQrBitmap);
            tvPlaceholder.setVisibility(View.GONE);

            btnSave.setEnabled(true);
            btnShare.setEnabled(true);

            Toast.makeText(this, "二维码生成成功", Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            Toast.makeText(this, "生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int getSelectedSize() {
        int checkedId = rgSize.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_small) {
            return SIZE_SMALL;
        } else if (checkedId == R.id.rb_large) {
            return SIZE_LARGE;
        } else {
            return SIZE_MEDIUM;
        }
    }

    private Bitmap createQRBitmap(String content, int size, boolean withLogo) throws WriterException {
        // 创建二维码位图
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        // 如果需要添加 Logo
        if (withLogo) {
            bitmap = addLogo(bitmap, size);
        }

        return bitmap;
    }

    private Bitmap addLogo(Bitmap qrBitmap, int qrSize) {
        // Logo 大小为二维码的 1/5
        int logoSize = qrSize / 5;

        // 创建 Logo 位图（使用文字作为 Logo）
        Bitmap logoBitmap = createLogoBitmap(logoSize);

        // 合并到二维码
        Bitmap result = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(qrBitmap, 0, 0, null);

        // 在中心绘制 Logo
        int left = (qrSize - logoSize) / 2;
        int top = (qrSize - logoSize) / 2;
        canvas.drawBitmap(logoBitmap, left, top, null);

        return result;
    }

    private Bitmap createLogoBitmap(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 绘制背景
        canvas.drawColor(Color.WHITE);

        // 绘制简单的 Logo 图案（圆形）
        PaintUtils.drawCenteredCircle(canvas, size);

        return bitmap;
    }

    private void saveQRCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etContent.getText().toString().trim();
        String fileName = "QR_" + System.currentTimeMillis() + ".png";

        try {
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用 MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCode");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                        Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Android 10 以下
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File qrDir = new File(picturesDir, "QRCode");
                if (!qrDir.exists()) {
                    qrDir.mkdirs();
                }
                File file = new File(qrDir, fileName);
                outputStream = new FileOutputStream(file);
                currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Toast.makeText(this, "已保存: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQRCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 保存到缓存目录
            String fileName = "qr_share.png";
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // 使用 FileProvider 获取 URI
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            // 创建分享 Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享二维码"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 绘图工具类
     */
    private static class PaintUtils {
        static void drawCenteredCircle(Canvas canvas, int size) {
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.rgb(26, 82, 224)); // Material Blue

            float radius = size / 3f;
            float centerX = size / 2f;
            float centerY = size / 2f;

            canvas.drawCircle(centerX, centerY, radius, paint);
        }
    }
}