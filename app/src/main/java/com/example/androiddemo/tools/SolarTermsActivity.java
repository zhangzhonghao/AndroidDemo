package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;

public class SolarTermsActivity extends AppCompatActivity {

    private TextView tvCurrentTerm;
    private TextView tvNextTerm;
    private TextView tvTermList;

    private String[] solarTerms = {
            "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
            "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
            "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
            "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };

    private String[] termDescriptions = {
            "小寒：天气渐冷，但还未到极点",
            "大寒：一年中最冷的时节",
            "立春：春季的开始，万物复苏",
            "雨水：降雨开始，雨量渐增",
            "惊蛰：春雷惊醒冬眠动物",
            "春分：昼夜平分，春季过半",
            "清明：天气晴朗，草木繁茂",
            "谷雨：雨量增多，利于谷物生长",
            "立夏：夏季的开始",
            "小满：夏熟作物籽粒渐满",
            "芒种：有芒作物成熟，抢收抢种",
            "夏至：白天最长，夜晚最短",
            "小暑：暑气渐盛",
            "大暑：一年中最热的时节",
            "立秋：秋季的开始",
            "处暑：暑气消退",
            "白露：天气转凉，露水凝结",
            "秋分：昼夜平分，秋季过半",
            "寒露：露气寒冷，秋季渐深",
            "霜降：天气渐冷，开始降霜",
            "立冬：冬季的开始",
            "小雪：开始降雪但雪量不大",
            "大雪：雪量增多，积雪加厚",
            "冬至：白天最短，夜晚最长"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solar_terms);

        tvCurrentTerm = findViewById(R.id.tv_current_term);
        tvNextTerm = findViewById(R.id.tv_next_term);
        tvTermList = findViewById(R.id.tv_term_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("二十四节气");
        }

        displaySolarTerms();
    }

    private void displaySolarTerms() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int currentIndex = getCurrentSolarTermIndex(month, day);
        int nextIndex = (currentIndex + 1) % 24;

        tvCurrentTerm.setText("当前节气: " + solarTerms[currentIndex] + "\n" + termDescriptions[currentIndex]);
        tvNextTerm.setText("下一个节气: " + solarTerms[nextIndex] + "\n" + termDescriptions[nextIndex]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            String marker = i == currentIndex ? "【" + solarTerms[i] + "】" : solarTerms[i];
            sb.append(marker);
            if ((i + 1) % 4 == 0) {
                sb.append("\n");
            } else {
                sb.append("  ");
            }
        }
        tvTermList.setText(sb.toString());
    }

    private int getCurrentSolarTermIndex(int month, int day) {
        int[][] termDates = {
                {1, 6}, {1, 20}, {2, 4}, {2, 19}, {3, 6}, {3, 21},
                {4, 5}, {4, 20}, {5, 6}, {5, 21}, {6, 6}, {6, 21},
                {7, 7}, {7, 23}, {8, 8}, {8, 23}, {9, 8}, {9, 23},
                {10, 8}, {10, 23}, {11, 7}, {11, 22}, {12, 7}, {12, 22}
        };

        for (int i = 23; i >= 0; i--) {
            if (month > termDates[i][0] || (month == termDates[i][0] && day >= termDates[i][1])) {
                return i;
            }
        }
        return 23;
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