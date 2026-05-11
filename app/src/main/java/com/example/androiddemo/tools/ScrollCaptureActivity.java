package com.example.androiddemo.tools;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ScrollCaptureActivity extends AppCompatActivity {
    private ImageView ivCapture;
    private Button btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_capture);
        initViews();
    }

    private void initViews() {
        ivCapture = findViewById(R.id.iv_capture);
        btnCapture = findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(v -> captureScreen());
    }

    private void captureScreen() {
        Toast.makeText(this, "长截图功能需要系统支持", Toast.LENGTH_SHORT).show();
    }
}