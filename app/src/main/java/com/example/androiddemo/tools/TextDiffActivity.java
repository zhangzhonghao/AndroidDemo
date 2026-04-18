package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class TextDiffActivity extends AppCompatActivity {
    private EditText etText1, etText2;
    private TextView tvDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_diff);
        etText1 = findViewById(R.id.et_text1);
        etText2 = findViewById(R.id.et_text2);
        tvDiff = findViewById(R.id.tv_diff);
    }

    public void compare(View view) {
        String text1 = etText1.getText().toString();
        String text2 = etText2.getText().toString();
        if (TextUtils.isEmpty(text1) || TextUtils.isEmpty(text2)) {
            tvDiff.setText("请输入两个文本进行比较");
            return;
        }
        if (text1.equals(text2)) {
            tvDiff.setText("两个文本完全相同");
            return;
        }
        StringBuilder diff = new StringBuilder();
        String[] lines1 = text1.split("\n");
        String[] lines2 = text2.split("\n");
        int maxLines = Math.max(lines1.length, lines2.length);
        for (int i = 0; i < maxLines; i++) {
            String l1 = i < lines1.length ? lines1[i] : "";
            String l2 = i < lines2.length ? lines2[i] : "";
            if (!l1.equals(l2)) {
                diff.append("第").append(i + 1).append("行:\n");
                if (!l1.isEmpty()) diff.append("- ").append(l1).append("\n");
                if (!l2.isEmpty()) diff.append("+ ").append(l2).append("\n");
            }
        }
        tvDiff.setText(diff.length() > 0 ? diff.toString() : "两个文本不同");
    }
}