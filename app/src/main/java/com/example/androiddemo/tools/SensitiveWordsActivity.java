package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Set;

public class SensitiveWordsActivity extends AppCompatActivity {

    private EditText etInput;
    private Button btnDetect;
    private Button btnClear;
    private LinearLayout layoutResult;
    private TextView tvResultTitle;
    private TextView tvFoundCount;
    private TextView tvSensitiveWords;
    private LinearLayout layoutSafe;
    private LinearLayout layoutDanger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensitive_words);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        btnDetect = findViewById(R.id.btn_detect);
        btnClear = findViewById(R.id.btn_clear);
        layoutResult = findViewById(R.id.layout_result);
        tvResultTitle = findViewById(R.id.tv_result_title);
        tvFoundCount = findViewById(R.id.tv_found_count);
        tvSensitiveWords = findViewById(R.id.tv_sensitive_words);
        layoutSafe = findViewById(R.id.layout_safe);
        layoutDanger = findViewById(R.id.layout_danger);
    }

    private void setupListeners() {
        btnDetect.setOnClickListener(v -> detectSensitiveWords());

        btnClear.setOnClickListener(v -> {
            etInput.setText("");
            layoutResult.setVisibility(View.GONE);
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutResult.getVisibility() == View.VISIBLE) {
                    detectSensitiveWords();
                }
            }
        });
    }

    private void detectSensitiveWords() {
        String text = etInput.getText().toString().trim();

        if (text.isEmpty()) {
            layoutResult.setVisibility(View.GONE);
            return;
        }

        layoutResult.setVisibility(View.VISIBLE);
        Set<String> foundWords = SensitiveWordDb.detect(text);

        if (foundWords.isEmpty()) {
            layoutSafe.setVisibility(View.VISIBLE);
            layoutDanger.setVisibility(View.GONE);
            tvResultTitle.setText("检测通过");
        } else {
            layoutSafe.setVisibility(View.GONE);
            layoutDanger.setVisibility(View.VISIBLE);
            tvResultTitle.setText("发现敏感词");
            tvFoundCount.setText("共发现 " + foundWords.size() + " 个敏感词");

            StringBuilder sb = new StringBuilder();
            for (String word : foundWords) {
                sb.append(word).append("、");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            tvSensitiveWords.setText(sb.toString());
        }
    }
}