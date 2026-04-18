package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateDiffActivity extends AppCompatActivity {
    private DatePicker dpStart, dpEnd;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_diff);
        dpStart = findViewById(R.id.dp_start);
        dpEnd = findViewById(R.id.dp_end);
        tvResult = findViewById(R.id.tv_result);
        dpStart.init(2026, 0, 1, null);
        dpEnd.init(2026, 3, 18, null);
    }

    public void calculate(View view) {
        Calendar c1 = Calendar.getInstance();
        c1.set(dpStart.getYear(), dpStart.getMonth(), dpStart.getDayOfMonth());
        Calendar c2 = Calendar.getInstance();
        c2.set(dpEnd.getYear(), dpEnd.getMonth(), dpEnd.getDayOfMonth());
        long diff = Math.abs(c2.getTimeInMillis() - c1.getTimeInMillis());
        long days = diff / (1000 * 60 * 60 * 24);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvResult.setText("开始日期: " + sdf.format(c1.getTime()) + "\n结束日期: " + sdf.format(c2.getTime()) + "\n相差: " + days + " 天");
    }
}