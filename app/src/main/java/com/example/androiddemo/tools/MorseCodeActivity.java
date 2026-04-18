package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class MorseCodeActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private boolean isEncoding = true;

    // 摩斯密码表
    private static final String[][] MORSE_CODE = {
        {"A", ".-"}, {"B", "-..."}, {"C", "-.-."}, {"D", "-.."}, {"E", "."},
        {"F", "..-."}, {"G", "--."}, {"H", "...."}, {"I", ".."}, {"J", ".---"},
        {"K", "-.-"}, {"L", ".-.."}, {"M", "--"}, {"N", "-."}, {"O", "---"},
        {"P", ".--."}, {"Q", "--.-"}, {"R", ".-."}, {"S", "..."}, {"T", "-"},
        {"U", "..-"}, {"V", "...-"}, {"W", ".--"}, {"X", "-..-"}, {"Y", "-.--"},
        {"Z", "--.."}, {"0", "-----"}, {"1", ".----"}, {"2", "..---"}, {"3", "...--"},
        {"4", "....-"}, {"5", "....."}, {"6", "-...."}, {"7", "--..."}, {"8", "---.."},
        {"9", "----."}, {".", ".-.-.-"}, {",", "--..--"}, {"?", "..--.."},
        {"'", ".----."}, {"!", "-.-.--"}, {"/", "-..-."}, {"(", "-.--."},
        {")", "-.--.-"}, {"&", ".-..."}, {":", "---..."}, {";", "-.-.-."},
        {"=", "-...-"}, {"+", ".-.-."}, {"-", "-....-"}, {"_", "..--.-"},
        {"\"", ".-..-."}, {"$", "...-..-"}, {"@", ".--.-."}, {" ", "/"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morse_code);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("摩斯密码");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        Button btnEncode = findViewById(R.id.btn_encode);
        Button btnDecode = findViewById(R.id.btn_decode);
        Button btnCopy = findViewById(R.id.btn_copy);

        btnEncode.setOnClickListener(v -> {
            isEncoding = true;
            encode();
        });
        btnDecode.setOnClickListener(v -> {
            isEncoding = false;
            decode();
        });
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("MorseCode", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void encode() {
        String input = etInput.getText().toString().toUpperCase();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            String morse = getMorse(c);
            if (morse != null) {
                result.append(morse).append(" ");
            } else if (c == '\n') {
                result.append("/ ");
            }
        }
        tvOutput.setText(result.toString().trim());
    }

    private void decode() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            tvOutput.setText("请输入摩斯密码");
            return;
        }

        String[] codes = input.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String code : codes) {
            if (code.equals("/")) {
                result.append(" ");
            } else {
                char ch = getChar(code);
                result.append(ch);
            }
        }
        tvOutput.setText(result.toString());
    }

    private String getMorse(char c) {
        for (String[] pair : MORSE_CODE) {
            if (pair[0].charAt(0) == c) {
                return pair[1];
            }
        }
        return null;
    }

    private char getChar(String morse) {
        for (String[] pair : MORSE_CODE) {
            if (pair[1].equals(morse)) {
                return pair[0].charAt(0);
            }
        }
        return '?';
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