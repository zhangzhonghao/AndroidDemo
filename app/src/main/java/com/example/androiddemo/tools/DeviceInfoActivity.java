package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class DeviceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        displayDeviceInfo();
    }

    private void displayDeviceInfo() {
        ScrollView scrollView = findViewById(R.id.scroll_device_info);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 32, 32, 32);

        addSection(container, "设备信息");
        addRow(container, "制造商", Build.MANUFACTURER);
        addRow(container, "品牌", Build.BRAND);
        addRow(container, "型号", Build.MODEL);
        addRow(container, "设备名", Build.DEVICE);
        addRow(container, "产品名", Build.PRODUCT);

        addSection(container, "硬件信息");
        addRow(container, "CPU型号", Build.HARDWARE);
        addRow(container, "CPU架构", Build.SUPPORTED_ABIS[0]);
        addRow(container, "处理器", Build.BOARD);

        addSection(container, "系统信息");
        addRow(container, "Android版本", Build.VERSION.RELEASE);
        addRow(container, "SDK版本", String.valueOf(Build.VERSION.SDK_INT));
        addRow(container, "内核版本", System.getProperty("os.version"));
        addRow(container, "构建ID", Build.ID);

        addSection(container, "屏幕信息");
        addRow(container, "屏幕密度", getResources().getDisplayMetrics().densityDpi + " dpi");
        addRow(container, "屏幕分辨率", getResources().getDisplayMetrics().widthPixels + " x " + getResources().getDisplayMetrics().heightPixels);

        addSection(container, "存储信息");
        addRow(container, "可用内存", formatFileSize(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()));
        addRow(container, "总内存", formatFileSize(Runtime.getRuntime().maxMemory()));

        scrollView.removeAllViews();
        scrollView.addView(container);
    }

    private void addSection(LinearLayout container, String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(18);
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        tv.setPadding(0, 32, 0, 16);
        container.addView(tv);
    }

    private void addRow(LinearLayout container, String label, String value) {
        TextView tv = new TextView(this);
        tv.setText(label + ": " + value);
        tv.setTextSize(14);
        tv.setPadding(0, 8, 0, 4);
        container.addView(tv);
    }

    private String formatFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) return String.format("%.2f GB", size / (double) gb);
        if (size >= mb) return String.format("%.2f MB", size / (double) mb);
        if (size >= kb) return String.format("%.2f KB", size / (double) kb);
        return size + " B";
    }
}