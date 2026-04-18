package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class RandomNumberGeneratorActivity extends AppCompatActivity {

    private TextView tvResult;
    private EditText etMin, etMax;
    private Button btnGenerate, btnCopy;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_number_generator);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvResult = findViewById(R.id.tv_result);
        etMin = findViewById(R.id.et_min);
        etMax = findViewById(R.id.et_max);
        btnGenerate = findViewById(R.id.btn_generate);
        btnCopy = findViewById(R.id.btn_copy);

        etMin.setText("1");
        etMax.setText("100");
    }

    private void setupListeners() {
        btnGenerate.setOnClickListener(v -> generateNumber());
        btnCopy.setOnClickListener(v -> copyResult());
    }

    private void generateNumber() {
        String minStr = etMin.getText().toString().trim();
        String maxStr = etMax.getText().toString().trim();

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            Toast.makeText(this, "请输入最小值和最大值", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int min = Integer.parseInt(minStr);
            int max = Integer.parseInt(maxStr);

            if (min > max) {
                Toast.makeText(this, "最小值不能大于最大值", Toast.LENGTH_SHORT).show();
                return;
            }

            int value = random.nextInt(max - min + 1) + min;
            tvResult.setText(String.valueOf(value));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的整数", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyResult() {
        String result = tvResult.getText().toString();
        if ("--".equals(result)) {
            Toast.makeText(this, "先生成数字", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("随机数", result);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
}