package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class CaesarCipherActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private TextView tvShift;
    private SeekBar seekBarShift;
    private int shift = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caesar_cipher);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("凯撒密码");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        tvShift = findViewById(R.id.tv_shift);
        seekBarShift = findViewById(R.id.seek_bar_shift);
        Button btnEncrypt = findViewById(R.id.btn_encrypt);
        Button btnDecrypt = findViewById(R.id.btn_decrypt);
        Button btnCopy = findViewById(R.id.btn_copy);

        seekBarShift.setProgress(shift);
        tvShift.setText("偏移量: " + shift);

        seekBarShift.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                shift = progress;
                tvShift.setText("偏移量: " + shift);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnEncrypt.setOnClickListener(v -> encrypt());
        btnDecrypt.setOnClickListener(v -> decrypt());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Caesar", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void encrypt() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }
        tvOutput.setText(caesar(input, shift));
    }

    private void decrypt() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }
        tvOutput.setText(caesar(input, 26 - shift));
    }

    private String caesar(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                result.append((char) ('a' + (c - 'a' + shift) % 26));
            } else if (c >= 'A' && c <= 'Z') {
                result.append((char) ('A' + (c - 'A' + shift) % 26));
            } else {
                result.append(c);
            }
        }
        return result.toString();
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