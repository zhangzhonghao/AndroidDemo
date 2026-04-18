package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class LoveMatchActivity extends AppCompatActivity {

    private EditText etName1;
    private EditText etName2;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_match);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("缘分速配");
        }

        etName1 = findViewById(R.id.et_name1);
        etName2 = findViewById(R.id.et_name2);
        tvResult = findViewById(R.id.tv_result);

        findViewById(R.id.btn_calculate).setOnClickListener(v -> calculateLove());
    }

    private void calculateLove() {
        String name1 = etName1.getText().toString().trim();
        String name2 = etName2.getText().toString().trim();

        if (name1.isEmpty() || name2.isEmpty()) {
            tvResult.setText("请输入双方姓名");
            return;
        }

        // 使用姓名计算缘分值
        int sum1 = name1.hashCode();
        int sum2 = name2.hashCode();
        int lovePercent = Math.abs((sum1 + sum2) % 100);

        String[] comments = {
            "你们的缘分还很浅，需要多多培养",
            "初步有好感，继续努力会有好结果",
            "缘分不错，你们很般配",
            "心有灵犀一点通，天生一对",
            "前世修来的缘分的，好好珍惜"
        };
        int commentIndex = lovePercent / 20;

        String result = name1 + " ♥ " + name2 + "\n\n" +
                       "缘分指数：" + lovePercent + "%\n\n" +
                       "分析结果：\n" + comments[commentIndex];

        tvResult.setText(result);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}