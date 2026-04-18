package com.example.androiddemo.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;

public class PowerScheduleActivity extends AppCompatActivity {
    private TimePicker tpPowerOn, tpPowerOff;
    private Button btnSetPowerOn, btnSetPowerOff, btnCancelPowerOn, btnCancelPowerOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_schedule);
        tpPowerOn = findViewById(R.id.tp_power_on);
        tpPowerOff = findViewById(R.id.tp_power_off);
        btnSetPowerOn = findViewById(R.id.btn_set_power_on);
        btnSetPowerOff = findViewById(R.id.btn_set_power_off);
        btnCancelPowerOn = findViewById(R.id.btn_cancel_power_on);
        btnCancelPowerOff = findViewById(R.id.btn_cancel_power_off);

        tpPowerOn.setIs24HourView(true);
        tpPowerOff.setIs24HourView(true);

        btnSetPowerOn.setOnClickListener(v -> setPowerOn());
        btnSetPowerOff.setOnClickListener(v -> setPowerOff());
        btnCancelPowerOn.setOnClickListener(v -> cancelPowerOn());
        btnCancelPowerOff.setOnClickListener(v -> cancelPowerOff());
    }

    private void setPowerOn() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "需要悬浮窗权限", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, tpPowerOn.getHour());
        c.set(Calendar.MINUTE, tpPowerOn.getMinute());
        c.set(Calendar.SECOND, 0);
        Intent intent = new Intent("android.intent.action.QUICKBOOT_POWERON");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) cancelAlarm(pi);
        pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        Toast.makeText(this, "开机闹钟已设置", Toast.LENGTH_SHORT).show();
    }

    private void setPowerOff() {
        Toast.makeText(this, "定时关机需要系统权限，应用市场下载专业版", Toast.LENGTH_SHORT).show();
    }

    private void cancelPowerOn() {
        Intent intent = new Intent("android.intent.action.QUICKBOOT_POWERON");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) cancelAlarm(pi);
        Toast.makeText(this, "开机闹钟已取消", Toast.LENGTH_SHORT).show();
    }

    private void cancelPowerOff() {
        Toast.makeText(this, "定时关机已取消", Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm(PendingIntent pi) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        pi.cancel();
    }
}