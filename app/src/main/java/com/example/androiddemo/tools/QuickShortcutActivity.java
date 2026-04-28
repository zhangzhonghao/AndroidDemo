package com.example.androiddemo.tools;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class QuickShortcutActivity extends AppCompatActivity {

    private LinearLayout shortcutsContainer;

    // 预设的快捷方式列表
    private final String[][] shortcuts = {
            {"系统设置", "android.settings.SETTINGS", "com.android.settings", "com.android.settings.Settings"},
            {"浏览器", "android.intent.action.VIEW", null, null},
            {"相机", "android.media.action.IMAGE_CAPTURE", null, null},
            {"地图", "android.intent.action.VIEW", null, null},
            {"音乐", "android.intent.action.MUSIC_PLAYER", null, null},
            {"日历", "android.intent.action.MAIN", "com.android.calendar", "com.android.calendar.LaunchActivity"}
    };

    private final int[] shortcutIcons = {
            R.drawable.ic_settings,
            R.drawable.ic_settings,
            R.drawable.ic_settings,
            R.drawable.ic_settings,
            R.drawable.ic_settings,
            R.drawable.ic_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_shortcut);
        shortcutsContainer = findViewById(R.id.shortcuts_container);
        setupShortcuts();
    }

    private void setupShortcuts() {
        for (int i = 0; i < shortcuts.length; i++) {
            final String[] shortcut = shortcuts[i];
            View shortcutView = createShortcutView(shortcut[0], shortcutIcons[i], i);
            shortcutsContainer.addView(shortcutView);
        }
    }

    private View createShortcutView(String name, int iconRes, final int index) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(32, 24, 32, 24);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setBackgroundResource(android.R.drawable.list_selector_background);
        container.setClickable(true);
        container.setFocusable(true);

        final int finalIndex = index;
        container.setOnClickListener(v -> createShortcutAndAddToHome(shortcuts[finalIndex]));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);
        iconParams.setMargins(0, 0, 32, 0);
        icon.setLayoutParams(iconParams);
        container.addView(icon);

        TextView text = new TextView(this);
        text.setText(name);
        text.setTextSize(18);
        text.setTextColor(getResources().getColor(android.R.color.black, null));
        container.addView(text);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 8, 0, 8);
        container.setLayoutParams(containerParams);

        return container;
    }

    private void createShortcutAndAddToHome(String[] shortcut) {
        String name = shortcut[0];
        String action = shortcut[1];
        String packageName = shortcut[2];
        String className = shortcut[3];

        // 找到对应的Activity
        Intent launchIntent = new Intent(action);
        if (packageName != null && className != null) {
            launchIntent.setComponent(new ComponentName(packageName, className));
        } else if (action.equals("android.intent.action.VIEW")) {
            // 浏览器打开百度
            launchIntent.setData(Uri.parse("https://www.baidu.com"));
        } else if (action.equals("android.media.action.IMAGE_CAPTURE")) {
            // 相机
            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(launchIntent, 0);
            if (!activities.isEmpty()) {
                launchIntent.setPackage(activities.get(0).activityInfo.packageName);
            }
        } else if (action.equals("android.intent.action.MUSIC_PLAYER")) {
            // 音乐播放器
            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(launchIntent, 0);
            if (!activities.isEmpty()) {
                launchIntent.setPackage(activities.get(0).activityInfo.packageName);
            }
        }

        // 创建快捷方式的Intent
        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_settings));
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);

        // 发送广播创建快捷方式
        sendBroadcast(shortcutIntent);

        Toast.makeText(this, "快捷方式「" + name + "」已创建到桌面", Toast.LENGTH_SHORT).show();
    }
}