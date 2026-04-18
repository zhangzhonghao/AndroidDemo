package com.example.androiddemo.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.Locale;

public class BaseConverterActivity extends AppCompatActivity {

    private EditText etInput;
    private ChipGroup chipGroupBase;
    private TextView tvBin, tvOct, tvDec, tvHex;

    private int inputBase = 10; // 默认十进制

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_converter);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        chipGroupBase = findViewById(R.id.chip_group_base);
        tvBin = findViewById(R.id.tv_bin);
        tvOct = findViewById(R.id.tv_oct);
        tvDec = findViewById(R.id.tv_dec);
        tvHex = findViewById(R.id.tv_hex);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("进制转换器");
        }
    }

    private void setupListeners() {
        // 输入监听
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                convert();
            }
        });

        // 进制选择监听
        chipGroupBase.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_bin) {
                inputBase = 2;
                updateInputHint("二进制 (0-1)");
                updateValidChars("01");
            } else if (checkedId == R.id.chip_oct) {
                inputBase = 8;
                updateInputHint("八进制 (0-7)");
                updateValidChars("01234567");
            } else if (checkedId == R.id.chip_dec) {
                inputBase = 10;
                updateInputHint("十进制 (0-9)");
                updateValidChars("0123456789");
            } else if (checkedId == R.id.chip_hex) {
                inputBase = 16;
                updateInputHint("十六进制 (0-9, A-F)");
                updateValidChars("0123456789ABCDEFabcdef");
            }
            convert();
        });

        // 复制按钮
        findViewById(R.id.btn_copy_bin).setOnClickListener(v -> copyToClipboard(tvBin.getText().toString(), "BIN"));
        findViewById(R.id.btn_copy_oct).setOnClickListener(v -> copyToClipboard(tvOct.getText().toString(), "OCT"));
        findViewById(R.id.btn_copy_dec).setOnClickListener(v -> copyToClipboard(tvDec.getText().toString(), "DEC"));
        findViewById(R.id.btn_copy_hex).setOnClickListener(v -> copyToClipboard(tvHex.getText().toString(), "HEX"));

        // 清除按钮
        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            etInput.setText("");
            etInput.requestFocus();
        });
    }

    private void updateInputHint(String hint) {
        etInput.setHint(hint);
    }

    private void updateValidChars(String validChars) {
        // 简单验证：只允许有效字符输入
        etInput.removeTextChangedListener(textWatcher);
        String text = etInput.getText().toString();
        StringBuilder filtered = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (validChars.indexOf(c) >= 0) {
                filtered.append(c);
            }
        }
        etInput.setText(filtered.toString());
        etInput.setSelection(etInput.getText().length());
        etInput.addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            convert();
        }
    };

    private void convert() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            resetOutputs();
            return;
        }

        try {
            // 将输入转换为十进制
            long decimalValue;
            if (inputBase == 10) {
                decimalValue = Long.parseLong(input);
            } else {
                decimalValue = Long.parseLong(input, inputBase);
            }

            // 转换为各进制
            String bin = Long.toBinaryString(decimalValue);
            String oct = Long.toOctalString(decimalValue);
            String dec = String.valueOf(decimalValue);
            String hex = Long.toHexString(decimalValue).toUpperCase(Locale.ROOT);

            tvBin.setText(bin);
            tvOct.setText(oct);
            tvDec.setText(dec);
            tvHex.setText(hex);

            // 设置正常颜色
            setOutputTextColor(ContextCompat.getColor(this, R.color.secondary));

        } catch (NumberFormatException e) {
            // 输入无效
            tvBin.setText("溢出/无效");
            tvOct.setText("溢出/无效");
            tvDec.setText("溢出/无效");
            tvHex.setText("溢出/无效");
            setOutputTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    private void resetOutputs() {
        tvBin.setText("0");
        tvOct.setText("0");
        tvDec.setText("0");
        tvHex.setText("0");
        setOutputTextColor(ContextCompat.getColor(this, R.color.secondary));
    }

    private void setOutputTextColor(int color) {
        tvBin.setTextColor(color);
        tvOct.setTextColor(color);
        tvDec.setTextColor(color);
        tvHex.setTextColor(color);
    }

    private void copyToClipboard(String text, String label) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, label + " 已复制", Toast.LENGTH_SHORT).show();
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