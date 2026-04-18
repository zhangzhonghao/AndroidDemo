package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.security.SecureRandom;
import java.util.Random;

public class RandomStringActivity extends AppCompatActivity {

    private EditText etLength;
    private TextView tvOutput;
    private CheckBox cbUppercase;
    private CheckBox cbLowercase;
    private CheckBox cbDigits;
    private CheckBox cbSpecial;
    private Random random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_string);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("随机字符串");
        }

        initViews();
    }

    private void initViews() {
        etLength = findViewById(R.id.et_length);
        tvOutput = findViewById(R.id.tv_output);
        cbUppercase = findViewById(R.id.cb_uppercase);
        cbLowercase = findViewById(R.id.cb_lowercase);
        cbDigits = findViewById(R.id.cb_digits);
        cbSpecial = findViewById(R.id.cb_special);
        Button btnGenerate = findViewById(R.id.btn_generate);
        Button btnCopy = findViewById(R.id.btn_copy);

        // Default selections
        cbUppercase.setChecked(true);
        cbLowercase.setChecked(true);
        cbDigits.setChecked(true);
        etLength.setText("16");

        btnGenerate.setOnClickListener(v -> generate());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("RandomString", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void generate() {
        int length;
        try {
            length = Integer.parseInt(etLength.getText().toString());
            if (length <= 0 || length > 1024) {
                tvOutput.setText("长度必须在1-1024之间");
                return;
            }
        } catch (NumberFormatException e) {
            tvOutput.setText("请输入有效长度");
            return;
        }

        StringBuilder chars = new StringBuilder();
        if (cbUppercase.isChecked()) chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (cbLowercase.isChecked()) chars.append("abcdefghijklmnopqrstuvwxyz");
        if (cbDigits.isChecked()) chars.append("0123456789");
        if (cbSpecial.isChecked()) chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");

        if (chars.length() == 0) {
            tvOutput.setText("请至少选择一种字符类型");
            return;
        }

        String charSet = chars.toString();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(charSet.length());
            result.append(charSet.charAt(idx));
        }

        tvOutput.setText(result.toString());
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