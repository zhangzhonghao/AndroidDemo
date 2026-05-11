package com.example.androiddemo.tools;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class QuickShortcutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_shortcut);
        setupShortcuts();
    }

    private void setupShortcuts() {
        LinearLayout container = findViewById(R.id.shortcuts_container);
        String[] names = {"系统设置", "浏览器", "地图", "相机", "音乐", "日历"};
        int[] icons = {R.drawable.ic_settings, R.drawable.ic_settings, R.drawable.ic_settings, R.drawable.ic_settings, R.drawable.ic_settings, R.drawable.ic_settings};

        for (int i = 0; i < names.length; i++) {
            int index = i;
            Button btn = new Button(this);
            btn.setText(names[i]);
            btn.setOnClickListener(v -> createShortcut(names[index]));
            container.addView(btn);
        }
    }

    private void createShortcut(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.baidu.com"));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent shortcutIntent = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction()) ? getIntent() : new Intent();
        shortcutIntent.setComponent(new ComponentName(this, QuickShortcutActivity.class));
        shortcutIntent.putExtra("shortcut", name);

        Toast.makeText(this, "快捷方式已创建：" + name, Toast.LENGTH_SHORT).show();
        finish();
    }
}