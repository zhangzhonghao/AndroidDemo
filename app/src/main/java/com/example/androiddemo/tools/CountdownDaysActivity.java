package com.example.androiddemo.tools;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CountdownDaysActivity extends AppCompatActivity {

    private TextView tvTargetDate;
    private TextView tvDaysLeft;
    private TextView tvCountdownDetails;
    private Button btnPickDate;
    private long targetTimeInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_days);

        tvTargetDate = findViewById(R.id.tv_target_date);
        tvDaysLeft = findViewById(R.id.tv_days_left);
        tvCountdownDetails = findViewById(R.id.tv_countdown_details);
        btnPickDate = findViewById(R.id.btn_pick_date);

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth, 0, 0, 0);
                        targetTimeInMillis = selected.getTimeInMillis();
                        updateCountdown();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateCountdown() {
        long now = System.currentTimeMillis();
        long diff = targetTimeInMillis - now;

        if (diff <= 0) {
            tvDaysLeft.setText("已到日期");
            tvCountdownDetails.setText("00天 00时 00分 00秒");
            tvTargetDate.setText("目标日期: -");
            return;
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        tvTargetDate.setText("目标日期: " + sdf.format(new Date(targetTimeInMillis)));
        tvDaysLeft.setText("剩余 " + days + " 天");
        tvCountdownDetails.setText(String.format(Locale.getDefault(),
                "%02d天 %02d时 %02d分 %02d秒", days, hours, minutes, seconds));
    }
}
