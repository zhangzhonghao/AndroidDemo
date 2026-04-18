package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class UnicodeActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unicode);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Unicode转换");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        Button btnEncode = findViewById(R.id.btn_encode);
        Button btnDecode = findViewById(R.id.btn_decode);
        Button btnCopy = findViewById(R.id.btn_copy);

        btnEncode.setOnClickListener(v -> encode());
        btnDecode.setOnClickListener(v -> decode());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Unicode", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void encode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append(String.format("\\u%04X", (int) c));
        }
        tvOutput.setText(result.toString());
    }

    private void decode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入Unicode字符串");
            return;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                if (i + 5 < input.length()) {
                    try {
                        String hex = input.substring(i + 2, i + 6);
                        int code = Integer.parseInt(hex, 16);
                        result.append((char) code);
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        // ignore and process normally
                    }
                }
            }
            result.append(input.charAt(i));
            i++;
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