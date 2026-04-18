package com.example.androiddemo.tools;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class GifPreviewActivity extends AppCompatActivity {
    private GifImageView ivGif;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_preview);
        initViews();
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivGif.loadGif(uri);
                }
            });
        findViewById(R.id.btn_select).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void initViews() {
        ivGif = findViewById(R.id.iv_gif);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ivGif != null) {
            ivGif.stopAnimation();
        }
    }
}