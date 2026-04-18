package com.example.androiddemo.tools;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CountdownActivity extends AppCompatActivity {

    private TextView tvDays, tvHours, tvMinutes, tvSeconds, tvTargetDate;
    private Button btnSetDate, btnSetTime, btnStart, btnStop, btnReset;
    private CountDownTimer countDownTimer;
    private Calendar targetCalendar = Calendar.getInstance();
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        initViews();
        setupListeners();
        updateTargetDateDisplay();
    }

    private void initViews() {
        tvDays = findViewById(R.id.tv_days);
        tvHours = findViewById(R.id.tv_hours);
        tvMinutes = findViewById(R.id.tv_minutes);
        tvSeconds = findViewById(R.id.tv_seconds);
        tvTargetDate = findViewById(R.id.tv_target_date);
        btnSetDate = findViewById(R.id.btn_set_date);
        btnSetTime = findViewById(R.id.btn_set_time);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnReset = findViewById(R.id.btn_reset);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("倒计时器");
        }

        // 默认目标：明天这个时候
        targetCalendar.add(Calendar.DAY_OF_YEAR, 1);
        updateTargetDateDisplay();
    }

    private void setupListeners() {
        btnSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(CountdownActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                targetCalendar.set(Calendar.YEAR, year);
                                targetCalendar.set(Calendar.MONTH, month);
                                targetCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                updateTargetDateDisplay();
                            }
                        },
                        targetCalendar.get(Calendar.YEAR),
                        targetCalendar.get(Calendar.MONTH),
                        targetCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(CountdownActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                targetCalendar.set(Calendar.MINUTE, minute);
                                targetCalendar.set(Calendar.SECOND, 0);
                                updateTargetDateDisplay();
                            }
                        },
                        targetCalendar.get(Calendar.HOUR_OF_DAY),
                        targetCalendar.get(Calendar.MINUTE),
                        true);
                dialog.show();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdown();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCountdown();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCountdown();
            }
        });
    }

    private void updateTargetDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        tvTargetDate.setText("目标: " + sdf.format(targetCalendar.getTime()));
    }

    private void startCountdown() {
        if (isRunning) return;

        long diff = targetCalendar.getTimeInMillis() - System.currentTimeMillis();
        if (diff <= 0) {
            Toast.makeText(this, "目标时间已过，请设置未来时间", Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        countDownTimer = new CountDownTimer(diff, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                tvDays.setText("00");
                tvHours.setText("00");
                tvMinutes.setText("00");
                tvSeconds.setText("00");
                Toast.makeText(CountdownActivity.this, "倒计时结束！", Toast.LENGTH_LONG).show();
                isRunning = false;
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        }.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    private void resetCountdown() {
        stopCountdown();
        targetCalendar = Calendar.getInstance();
        targetCalendar.add(Calendar.DAY_OF_YEAR, 1);
        updateTargetDateDisplay();
        tvDays.setText("00");
        tvHours.setText("00");
        tvMinutes.setText("00");
        tvSeconds.setText("00");
    }

    private void updateDisplay(long millisUntilFinished) {
        int days = (int) (millisUntilFinished / (1000 * 60 * 60 * 24));
        int hours = (int) ((millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        int minutes = (int) ((millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((millisUntilFinished % (1000 * 60)) / 1000);

        tvDays.setText(String.format(Locale.getDefault(), "%02d", days));
        tvHours.setText(String.format(Locale.getDefault(), "%02d", hours));
        tvMinutes.setText(String.format(Locale.getDefault(), "%02d", minutes));
        tvSeconds.setText(String.format(Locale.getDefault(), "%02d", seconds));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}