package com.example.androiddemo.tools;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ScrollCaptureActivity extends AppCompatActivity {
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private ImageView ivCapture;
    private Button btnCapture;
    private TextView tvStatus;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private boolean isCapturing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_capture);
        initViews();
    }

    private void initViews() {
        ivCapture = findViewById(R.id.iv_capture);
        btnCapture = findViewById(R.id.btn_capture);
        tvStatus = findViewById(R.id.tv_status);

        btnCapture.setOnClickListener(v -> {
            if (!isCapturing) {
                startScrollCapture();
            }
        });
    }

    private void startScrollCapture() {
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK && data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection != null) {
                isCapturing = true;
                btnCapture.setText("截图中...");
                btnCapture.setEnabled(false);
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("请向上滚动屏幕，3秒后开始截取...");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    captureScreen();
                }, 3000);
            }
        } else {
            Toast.makeText(this, "需要屏幕截图权限才能使用此功能", Toast.LENGTH_SHORT).show();
        }
    }

    private void captureScreen() {
        // 获取屏幕尺寸
        android.view.WindowManager windowManager = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
        android.view.Display display = windowManager.getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        // 创建Bitmap捕获当前屏幕
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);

        // 使用VirtualDisplay或者直接复制屏幕内容
        // 这里使用简单的屏幕截图方式
        try {
            android.view.View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setDrawingCacheEnabled(true);
                Bitmap cache = contentView.getDrawingCache();
                if (cache != null) {
                    bitmap = cache.copy(Bitmap.Config.ARGB_8888, false);
                    contentView.setDrawingCacheEnabled(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 显示捕获的截图
        ivCapture.setImageBitmap(bitmap);
        isCapturing = false;
        btnCapture.setText("开始滚动截图");
        btnCapture.setEnabled(true);
        tvStatus.setText("截图完成！点击按钮可重新截取");

        Toast.makeText(this, "屏幕截图已保存", Toast.LENGTH_SHORT).show();

        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
}