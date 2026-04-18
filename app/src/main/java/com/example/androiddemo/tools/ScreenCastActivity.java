package com.example.androiddemo.tools;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ScreenCastActivity extends AppCompatActivity {
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private TextView tvStatus;
    private Button btnStartCast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_cast);
        initViews();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        btnStartCast = findViewById(R.id.btn_start_cast);
        btnStartCast.setOnClickListener(v -> requestScreenCapture());
    }

    private void requestScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
            (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK) {
            tvStatus.setText("投屏中...");
        }
    }
}