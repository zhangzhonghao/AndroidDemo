package com.example.androiddemo.tools;

import android.os.Bundle;
import java.security.MessageDigest;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class HashCalculatorActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvSha1;
    private TextView tvSha256;
    private TextView tvSha512;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_calculator);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("哈希值计算");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvSha1 = findViewById(R.id.tv_sha1);
        tvSha256 = findViewById(R.id.tv_sha256);
        tvSha512 = findViewById(R.id.tv_sha512);
        btnCalculate = findViewById(R.id.btn_calculate);
    }

    private void setupListeners() {
        btnCalculate.setOnClickListener(v -> calculate());
    }

    private void calculate() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvSha1.setText("请输入文本");
            tvSha256.setText("");
            tvSha512.setText("");
            return;
        }
        try {
            tvSha1.setText(sha("SHA-1", input));
            tvSha256.setText(sha("SHA-256", input));
            tvSha512.setText(sha("SHA-512", input));
        } catch (Exception e) {
            tvSha1.setText("计算失败: " + e.getMessage());
            tvSha256.setText("");
            tvSha512.setText("");
        }
    }

    private String sha(String algorithm, String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
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