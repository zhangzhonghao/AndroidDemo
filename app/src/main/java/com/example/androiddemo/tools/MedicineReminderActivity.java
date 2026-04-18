package com.example.androiddemo.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;

public class MedicineReminderActivity extends AppCompatActivity {

    private TimePicker tpTime;
    private EditText etMedicineName;
    private TextView tvStatus;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_reminder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("吃药提醒");
        }

        tpTime = findViewById(R.id.tp_time);
        etMedicineName = findViewById(R.id.et_medicine_name);
        tvStatus = findViewById(R.id.tv_status);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        findViewById(R.id.btn_set_reminder).setOnClickListener(v -> setReminder());
        findViewById(R.id.btn_cancel_reminder).setOnClickListener(v -> cancelReminder());
    }

    private void setReminder() {
        String medicineName = etMedicineName.getText().toString().trim();
        if (medicineName.isEmpty()) {
            tvStatus.setText("请输入药品名称");
            return;
        }

        int hour = tpTime.getCurrentHour();
        int minute = tpTime.getCurrentMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, MedicineReminderReceiver.class);
        intent.putExtra("medicine_name", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        tvStatus.setText("已设置提醒：每天 " + hour + ":" + String.format("%02d", minute) +
                        "\n药品：" + medicineName);
    }

    private void cancelReminder() {
        Intent intent = new Intent(this, MedicineReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            tvStatus.setText("提醒已取消");
        } else {
            tvStatus.setText("没有设置中的提醒");
        }
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