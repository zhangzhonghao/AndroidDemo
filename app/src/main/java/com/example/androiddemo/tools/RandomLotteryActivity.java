package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomLotteryActivity extends AppCompatActivity {
    private EditText etNumbers;
    private TextView tvResult;
    private TextView tvDrawnNumbers;
    private List<String> allNumbers = new ArrayList<>();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_lottery);
        etNumbers = findViewById(R.id.et_number_pool);
        tvResult = findViewById(R.id.tv_lottery_result);
        tvDrawnNumbers = findViewById(R.id.tv_drawn_numbers);
        findViewById(R.id.btn_start_lottery).setOnClickListener(v -> startLottery());
        findViewById(R.id.btn_reset).setOnClickListener(v -> resetLottery());
    }

    private void startLottery() {
        String input = etNumbers.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            tvResult.setText("请输入号码池");
            return;
        }
        if (allNumbers.isEmpty()) {
            String[] numbers = input.split("[,\n]");
            for (String n : numbers) {
                String trimmed = n.trim();
                if (!trimmed.isEmpty()) {
                    allNumbers.add(trimmed);
                }
            }
        }
        if (allNumbers.isEmpty()) {
            tvResult.setText("号码池为空");
            return;
        }
        int index = random.nextInt(allNumbers.size());
        String drawn = allNumbers.remove(index);
        tvResult.setText("恭喜！抽中：" + drawn);
        tvDrawnNumbers.setText("剩余号码：" + allNumbers.size() + "个");
    }

    private void resetLottery() {
        allNumbers.clear();
        tvResult.setText("摇号结果：");
        tvDrawnNumbers.setText("");
        etNumbers.setText("");
    }
}