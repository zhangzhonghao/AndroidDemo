package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class WordCountActivity extends AppCompatActivity {
    private EditText etInput;
    private TextView tvCharCount;
    private TextView tvWordCount;
    private TextView tvLineCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_count);
        etInput = findViewById(R.id.et_text_input);
        tvCharCount = findViewById(R.id.tv_char_count);
        tvWordCount = findViewById(R.id.tv_word_count);
        tvLineCount = findViewById(R.id.tv_line_count);

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                countText();
            }
        });
    }

    private void countText() {
        String text = etInput.getText().toString();
        int charCount = text.length();
        tvCharCount.setText("字符数（含空格）：" + charCount);

        String trimmed = text.trim();
        int wordCount = 0;
        if (!trimmed.isEmpty()) {
            wordCount = trimmed.split("\\s+").length;
        }
        tvWordCount.setText("字数：" + trimmed.length() + "\n单词数（英文）：" + wordCount);

        int lineCount = 0;
        if (!text.isEmpty()) {
            lineCount = text.split("\n").length;
        }
        tvLineCount.setText("行数：" + lineCount);
    }
}