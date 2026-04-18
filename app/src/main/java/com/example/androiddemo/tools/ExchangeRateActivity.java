package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExchangeRateActivity extends AppCompatActivity {
    private Map<String, Double> rates = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        initRates();
        displayRates();
    }

    private void initRates() {
        rates.put("人民币(CNY)", 1.0);
        rates.put("美元(USD)", 7.24);
        rates.put("欧元(EUR)", 7.85);
        rates.put("英镑(GBP)", 9.15);
        rates.put("日元(JPY)", 0.048);
        rates.put("韩元(KRW)", 0.0054);
        rates.put("港币(HKD)", 0.93);
        rates.put("澳元(AUD)", 4.72);
        rates.put("加元(CAD)", 5.35);
        rates.put("瑞士法郎(CHF)", 8.17);
    }

    private void displayRates() {
        LinearLayout container = findViewById(R.id.rates_container);
        container.removeAllViews();
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            TextView tv = new TextView(this);
            tv.setText(entry.getKey() + ": " + entry.getValue());
            tv.setTextSize(16);
            tv.setPadding(16, 8, 16, 8);
            container.addView(tv);
        }
        TextView note = new TextView(this);
        note.setText("\n* 汇率为静态参考数据，实际汇率以银行为准");
        note.setTextSize(12);
        note.setTextColor(0xFF888888);
        container.addView(note);
    }
}