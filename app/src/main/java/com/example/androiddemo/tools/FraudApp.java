package com.example.androiddemo.tools;

import android.graphics.drawable.Drawable;

/**
 * 电诈APP数据类
 */
public class FraudApp {
    private String packageName;
    private String appName;
    private String riskType;
    private String description;
    private Drawable icon;

    public FraudApp(String packageName, String appName, String riskType, String description) {
        this.packageName = packageName;
        this.appName = appName;
        this.riskType = riskType;
        this.description = description;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRiskType() {
        return riskType;
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}