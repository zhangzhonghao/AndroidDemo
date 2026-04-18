package com.example.androiddemo.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MedicineReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine_name");
        // 这里可以发送通知，实际项目中需要添加通知权限
    }
}