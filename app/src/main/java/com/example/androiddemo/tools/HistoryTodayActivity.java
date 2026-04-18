package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryTodayActivity extends AppCompatActivity {
    private Map<String, List<String>> historyEvents = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_today);
        initHistoryData();
        displayHistory();
    }

    private void initHistoryData() {
        historyEvents.put("04-18", new ArrayList<>(Arrays.asList(
                "1906年 - 芝加哥禁酒大会召开",
                "1918年 - 中国共产党早期领导人李大钊被捕",
                "1955年 - 万隆会议召开",
                "1968年 -马丁·路德·金遇刺",
                "1980年 - 中国发射第一颗洲际导弹",
                "1989年 - 柏林墙倒塌前苏联开始撤军"
        )));
        historyEvents.put("04-01", new ArrayList<>(Arrays.asList(
                "1950年 - 西藏和平解放",
                "1978年 - 中国决定大规模砍伐森林",
                "1997年 - 香港特别行政区筹委会成立"
        )));
        historyEvents.put("04-02", new ArrayList<>(Arrays.asList(
                "1969年 - 中国成功进行导弹热实验",
                "1975年 - 春季广交会在广州开幕"
        )));
    }

    private void displayHistory() {
        ScrollView scrollView = findViewById(R.id.scroll_history);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 32, 32, 32);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        String today = sdf.format(new Date());
        TextView title = new TextView(this);
        title.setText("历史上的今天（" + today + "）");
        title.setTextSize(20);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        container.addView(title);

        List<String> events = historyEvents.get(today);
        if (events != null) {
            for (String event : events) {
                TextView tv = new TextView(this);
                tv.setText(event);
                tv.setTextSize(16);
                tv.setPadding(0, 16, 0, 8);
                container.addView(tv);
            }
        } else {
            TextView empty = new TextView(this);
            empty.setText("今天没有记录的历史事件");
            container.addView(empty);
        }

        scrollView.removeAllViews();
        scrollView.addView(container);
    }
}