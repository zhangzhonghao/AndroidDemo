package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexTesterActivity extends AppCompatActivity {
    private EditText etPattern, etText;
    private TextView tvResult, tvMatches;
    private Pattern currentPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regex_tester);
        etPattern = findViewById(R.id.et_pattern);
        etText = findViewById(R.id.et_text);
        tvResult = findViewById(R.id.tv_result);
        tvMatches = findViewById(R.id.tv_matches);
        etPattern.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { testRegex(); }
        });
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { testRegex(); }
        });
    }

    private void testRegex() {
        String patternStr = etPattern.getText().toString();
        String text = etText.getText().toString();
        if (patternStr.isEmpty()) {
            tvResult.setText("请输入正则表达式");
            tvMatches.setText("");
            return;
        }
        try {
            currentPattern = Pattern.compile(patternStr);
            tvResult.setText("✓ 正则表达式有效\n\n修饰符:\n" + getPatternInfo(currentPattern));
            if (!text.isEmpty()) {
                Matcher matcher = currentPattern.matcher(text);
                StringBuilder matches = new StringBuilder("匹配结果:\n");
                int count = 0;
                while (matcher.find()) {
                    count++;
                    matches.append("匹配").append(count).append(": \"").append(matcher.group()).append("\" 位置:").append(matcher.start()).append("-").append(matcher.end()).append("\n");
                }
                if (count == 0) matches.append("无匹配");
                tvMatches.setText(matches.toString());
            }
        } catch (PatternSyntaxException e) {
            tvResult.setText("✗ 正则表达式语法错误:\n" + e.getMessage());
            tvMatches.setText("");
        }
    }

    private String getPatternInfo(Pattern p) {
        return "大小写敏感\n多行模式: " + ((p.flags() & Pattern.CASE_INSENSITIVE) != 0 ? "是" : "否") + "\nUnicode: " + ((p.flags() & Pattern.UNICODE_CASE) != 0 ? "是" : "否");
    }
}