package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class UUEncodeActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uu_encode);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("UUEncode解码");
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
            android.content.ClipData clip = android.content.ClipData.newPlainText("UUEncode", tvOutput.getText());
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

        try {
            byte[] data = input.getBytes(StandardCharsets.UTF_8);
            StringBuilder result = new StringBuilder();
            result.append("begin 644 data.txt\n");

            int i = 0;
            while (i < data.length) {
                int len = Math.min(45, data.length - i);
                result.append(encode3to4(data, i, len));
                result.append("\n");
                i += 45;
            }
            result.append("end\n");
            tvOutput.setText(result.toString());
        } catch (Exception e) {
            tvOutput.setText("编码失败: " + e.getMessage());
        }
    }

    private void decode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入UUEncode文本");
            return;
        }

        try {
            StringBuilder result = new StringBuilder();
            String[] lines = input.split("\n");
            boolean started = false;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("begin")) {
                    started = true;
                    continue;
                }
                if (line.equals("end") || line.equals("`")) {
                    break;
                }
                if (!started || line.isEmpty()) continue;

                int len = line.length();
                if (len > 0) {
                    int charCount = line.charAt(0) - 32;
                    if (charCount > 0 && charCount <= 45) {
                        byte[] decoded = decode4to3(line.substring(1), charCount);
                        if (decoded != null) {
                            result.append(new String(decoded, 0, charCount, StandardCharsets.UTF_8));
                        }
                    }
                }
            }
            tvOutput.setText(result.toString());
        } catch (Exception e) {
            tvOutput.setText("解码失败: " + e.getMessage());
        }
    }

    private String encode3to4(byte[] data, int offset, int len) {
        StringBuilder result = new StringBuilder();
        int charCount = len;
        result.append((char) (charCount + 32));

        int i = offset;
        while (len >= 3) {
            int t = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8) | (data[i + 2] & 0xFF);
            result.append(encodeChar((t >> 18) & 0x3F));
            result.append(encodeChar((t >> 12) & 0x3F));
            result.append(encodeChar((t >> 6) & 0x3F));
            result.append(encodeChar(t & 0x3F));
            i += 3;
            len -= 3;
        }

        if (len == 2) {
            int t = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8);
            result.append(encodeChar((t >> 18) & 0x3F));
            result.append(encodeChar((t >> 12) & 0x3F));
            result.append(encodeChar((t >> 6) & 0x3F));
            result.append('`');
        } else if (len == 1) {
            int t = (data[i] & 0xFF) << 16;
            result.append(encodeChar((t >> 18) & 0x3F));
            result.append(encodeChar((t >> 12) & 0x3F));
            result.append("``");
        }

        return result.toString();
    }

    private char encodeChar(int v) {
        return (char) ((v & 0x3F) + 32);
    }

    private byte[] decode4to3(String data, int charCount) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = data.length();
        int i = 0;

        while (i < len && bos.size() < charCount) {
            int c1 = decodeChar(data.charAt(i++));
            if (i >= len) break;
            int c2 = decodeChar(data.charAt(i++));
            if (i >= len) break;
            int c3 = decodeChar(data.charAt(i++));
            if (i >= len) break;
            int c4 = decodeChar(data.charAt(i++));

            int t = (c1 << 18) | (c2 << 12) | (c3 << 6) | c4;
            if (bos.size() < charCount) bos.write((t >> 16) & 0xFF);
            if (bos.size() < charCount) bos.write((t >> 8) & 0xFF);
            if (bos.size() < charCount) bos.write(t & 0xFF);
        }

        return bos.toByteArray();
    }

    private int decodeChar(char c) {
        if (c == '`') return 0;
        return c - 32;
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