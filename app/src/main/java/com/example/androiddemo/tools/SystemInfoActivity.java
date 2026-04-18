package com.example.androiddemo.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SystemInfoActivity extends AppCompatActivity {

    private TextView tvAndroidVersion, tvSdkVersion, tvBuildVersion, tvSecurityPatch;
    private TextView tvCpuAbi, tvCpuCores, tvCpuFreq, tvCpuMaxFreq;
    private TextView tvTotalMemory, tvAvailableMemory, tvUsedMemory;
    private ProgressBar progressMemory;
    private TextView tvInternalStorage, tvExternalStorage;
    private ProgressBar progressInternal, progressExternal;
    private TextView tvScreenResolution, tvScreenDensity, tvScreenSize, tvRefreshRate;
    private TextView tvDeviceModel, tvDeviceBrand, tvDeviceManufacturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);

        initViews();
        loadSystemInfo();
        loadCpuInfo();
        loadMemoryInfo();
        loadStorageInfo();
        loadScreenInfo();
        loadDeviceInfo();
    }

    private void initViews() {
        tvAndroidVersion = findViewById(R.id.tv_android_version);
        tvSdkVersion = findViewById(R.id.tv_sdk_version);
        tvBuildVersion = findViewById(R.id.tv_build_version);
        tvSecurityPatch = findViewById(R.id.tv_security_patch);

        tvCpuAbi = findViewById(R.id.tv_cpu_abi);
        tvCpuCores = findViewById(R.id.tv_cpu_cores);
        tvCpuFreq = findViewById(R.id.tv_cpu_freq);
        tvCpuMaxFreq = findViewById(R.id.tv_cpu_max_freq);

        tvTotalMemory = findViewById(R.id.tv_total_memory);
        tvAvailableMemory = findViewById(R.id.tv_available_memory);
        tvUsedMemory = findViewById(R.id.tv_used_memory);
        progressMemory = findViewById(R.id.progress_memory);

        tvInternalStorage = findViewById(R.id.tv_internal_storage);
        tvExternalStorage = findViewById(R.id.tv_external_storage);
        progressInternal = findViewById(R.id.progress_internal);
        progressExternal = findViewById(R.id.progress_external);

        tvScreenResolution = findViewById(R.id.tv_screen_resolution);
        tvScreenDensity = findViewById(R.id.tv_screen_density);
        tvScreenSize = findViewById(R.id.tv_screen_size);
        tvRefreshRate = findViewById(R.id.tv_refresh_rate);

        tvDeviceModel = findViewById(R.id.tv_device_model);
        tvDeviceBrand = findViewById(R.id.tv_device_brand);
        tvDeviceManufacturer = findViewById(R.id.tv_device_manufacturer);
    }

    private void loadSystemInfo() {
        tvAndroidVersion.setText("Android 版本：" + Build.VERSION.RELEASE);
        tvSdkVersion.setText("SDK 版本：" + Build.VERSION.SDK_INT);
        tvBuildVersion.setText("Build 版本：" + Build.VERSION.INCREMENTAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvSecurityPatch.setText("安全补丁：" + Build.VERSION.SECURITY_PATCH);
        } else {
            tvSecurityPatch.setText("安全补丁：不可用");
        }
    }

    private void loadCpuInfo() {
        tvCpuAbi.setText("CPU 架构：" + Build.SUPPORTED_ABIS[0]);

        int cores = Runtime.getRuntime().availableProcessors();
        tvCpuCores.setText("核心数：" + cores);

        String cpuFreq = getCpuFreq();
        tvCpuFreq.setText("当前频率：" + cpuFreq);

        String maxFreq = getMaxCpuFreq();
        tvCpuMaxFreq.setText("最大频率：" + maxFreq);
    }

    private String getCpuFreq() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r");
            String freq = reader.readLine().trim();
            reader.close();
            return formatFreq(Long.parseLong(freq));
        } catch (IOException e) {
            return "未知";
        }
    }

    private String getMaxCpuFreq() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            String freq = reader.readLine().trim();
            reader.close();
            return formatFreq(Long.parseLong(freq));
        } catch (IOException e) {
            return "未知";
        }
    }

    private String formatFreq(long freqKhz) {
        if (freqKhz >= 1000000) {
            return String.format("%.2f GHz", freqKhz / 1000000.0);
        } else {
            return String.format("%d MHz", freqKhz / 1000);
        }
    }

    private void loadMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMem = memoryInfo.totalMem;
        long availMem = memoryInfo.availMem;
        long usedMem = totalMem - availMem;

        tvTotalMemory.setText("总内存：" + formatStorage(totalMem));
        tvAvailableMemory.setText("可用内存：" + formatStorage(availMem));
        tvUsedMemory.setText("已用内存：" + formatStorage(usedMem));

        int usagePercent = (int) ((usedMem * 100) / totalMem);
        progressMemory.setProgress(usagePercent);
    }

    private void loadStorageInfo() {
        StatFs internalStatFs = new StatFs(Environment.getDataDirectory().getPath());
        long internalTotal = internalStatFs.getBlockCountLong() * internalStatFs.getBlockSizeLong();
        long internalAvail = internalStatFs.getAvailableBlocksLong() * internalStatFs.getBlockSizeLong();
        long internalUsed = internalTotal - internalAvail;

        tvInternalStorage.setText(String.format("内部存储：已用 %s / 总计 %s",
                formatStorage(internalUsed), formatStorage(internalTotal)));

        int internalPercent = (int) ((internalUsed * 100) / internalTotal);
        progressInternal.setProgress(internalPercent);

        String externalStorageInfo = "外部存储：";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StatFs externalStatFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long externalTotal = externalStatFs.getBlockCountLong() * externalStatFs.getBlockSizeLong();
            long externalAvail = externalStatFs.getAvailableBlocksLong() * externalStatFs.getBlockSizeLong();
            long externalUsed = externalTotal - externalAvail;

            externalStorageInfo = String.format("外部存储：已用 %s / 总计 %s",
                    formatStorage(externalUsed), formatStorage(externalTotal));

            int externalPercent = externalTotal > 0 ? (int) ((externalUsed * 100) / externalTotal) : 0;
            progressExternal.setProgress(externalPercent);
        } else {
            externalStorageInfo = "外部存储：未挂载";
            progressExternal.setProgress(0);
        }
        tvExternalStorage.setText(externalStorageInfo);
    }

    private String formatStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        long tb = gb * 1024;

        if (size >= tb) {
            return String.format("%.2f TB", size / (double) tb);
        } else if (size >= gb) {
            return String.format("%.2f GB", size / (double) gb);
        } else if (size >= mb) {
            return String.format("%.2f MB", size / (double) mb);
        } else if (size >= kb) {
            return String.format("%.2f KB", size / (double) kb);
        } else {
            return size + " B";
        }
    }

    private void loadScreenInfo() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        tvScreenResolution.setText("分辨率：" + width + " x " + height);

        tvScreenDensity.setText("屏幕密度：" + metrics.densityDpi + " dpi");

        double diagonal = Math.sqrt(width * width + height * height) / metrics.xdpi;
        tvScreenSize.setText(String.format("屏幕尺寸：%.2f 英寸", diagonal));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            float refreshRate = getWindowManager().getDefaultDisplay().getRefreshRate();
            tvRefreshRate.setText("刷新率：" + refreshRate + " Hz");
        } else {
            tvRefreshRate.setText("刷新率：不可用");
        }
    }

    private void loadDeviceInfo() {
        tvDeviceModel.setText("型号：" + Build.MODEL);
        tvDeviceBrand.setText("品牌：" + Build.BRAND);
        tvDeviceManufacturer.setText("制造商：" + Build.MANUFACTURER);
    }
}