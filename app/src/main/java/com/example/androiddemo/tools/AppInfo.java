package com.example.androiddemo.tools;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String appName;
    public String packageName;
    public String versionName;
    public int versionCode;
    public long installTime;
    public long apkSize;
    public Drawable icon;
    public boolean isSystemApp;

    public AppInfo(String appName, String packageName, String versionName, int versionCode,
                   long installTime, long apkSize, Drawable icon, boolean isSystemApp) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.installTime = installTime;
        this.apkSize = apkSize;
        this.icon = icon;
        this.isSystemApp = isSystemApp;
    }
}