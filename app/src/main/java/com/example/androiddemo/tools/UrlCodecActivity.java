package com.example.androiddemo.tools;

import android.os.Bundle;
import android.net.Uri;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class UrlCodecActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private Button btnEncode;
    private Button btnDecode;
    private Button btnCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_codec);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("URL编码解码");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        btnEncode = findViewById(R.id.btn_encode);
        btnDecode = findViewById(R.id.btn_decode);
        btnCopy = findViewById(R.id.btn_copy);
    }

    private void setupListeners() {
        btnEncode.setOnClickListener(v -> encode());
        btnDecode.setOnClickListener(v -> decode());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("URL", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void encode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入URL");
            return;
        }
        try {
            String encoded = Uri.encode(input, "UTF-8");
            tvOutput.setText(encoded);
        } catch (Exception e) {
            tvOutput.setText("编码失败: " + e.getMessage());
        }
    }

    private void decode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入URL");
            return;
        }
        try {
            String decoded = Uri.decode(input);
            tvOutput.setText(decoded);
        } catch (Exception e) {
            tvOutput.setText("解码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}