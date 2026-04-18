package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WorldTimezonesActivity extends AppCompatActivity {

    private Spinner spinnerFromZone;
    private Spinner spinnerToZone;
    private TextView tvFromTime;
    private TextView tvToTime;
    private TextView tvTimeDiff;

    private String[] timezones = {
            "Asia/Shanghai", "Asia/Hong_Kong", "Asia/Tokyo", "Asia/Seoul",
            "Asia/Singapore", "Asia/Dubai", "Asia/Kolkata", "Asia/Bangkok",
            "Europe/London", "Europe/Paris", "Europe/Berlin", "Europe/Moscow",
            "America/New_York", "America/Los_Angeles", "America/Chicago",
            "America/Toronto", "Australia/Sydney", "Pacific/Auckland"
    };

    private String[] timezoneNames = {
            "中国上海", "中国香港", "日本东京", "韩国首尔",
            "新加坡", "迪拜", "印度加尔各答", "泰国曼谷",
            "英国伦敦", "法国巴黎", "德国柏林", "俄罗斯莫斯科",
            "美国纽约", "美国洛杉矶", "美国芝加哥",
            "加拿大多伦多", "澳大利亚悉尼", "新西兰奥克兰"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_timezones);

        spinnerFromZone = findViewById(R.id.spinner_from_zone);
        spinnerToZone = findViewById(R.id.spinner_to_zone);
        tvFromTime = findViewById(R.id.tv_from_time);
        tvToTime = findViewById(R.id.tv_to_time);
        tvTimeDiff = findViewById(R.id.tv_time_diff);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("世界时区转换");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timezoneNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFromZone.setAdapter(adapter);
        spinnerToZone.setAdapter(adapter);
        spinnerFromZone.setSelection(0);
        spinnerToZone.setSelection(4);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convertTime();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFromZone.setOnItemSelectedListener(listener);
        spinnerToZone.setOnItemSelectedListener(listener);

        convertTime();
    }

    private void convertTime() {
        int fromIndex = spinnerFromZone.getSelectedItemPosition();
        int toIndex = spinnerToZone.getSelectedItemPosition();

        TimeZone fromZone = TimeZone.getTimeZone(timezones[fromIndex]);
        TimeZone toZone = TimeZone.getTimeZone(timezones[toIndex]);

        Calendar calendar = Calendar.getInstance(fromZone);
        Date fromDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(fromZone);
        String fromTimeStr = sdf.format(fromDate) + " (" + timezoneNames[fromIndex] + ")";

        sdf.setTimeZone(toZone);
        String toTimeStr = sdf.format(fromDate) + " (" + timezoneNames[toIndex] + ")";

        int offsetDiff = (toZone.getRawOffset() - fromZone.getRawOffset()) / 3600000;
        String diffStr = offsetDiff >= 0 ? "+" + offsetDiff : String.valueOf(offsetDiff);

        tvFromTime.setText(fromTimeStr);
        tvToTime.setText(toTimeStr);
        tvTimeDiff.setText("时差: " + diffStr + " 小时");
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