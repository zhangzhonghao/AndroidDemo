package com.example.androiddemo.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class WifiPasswordActivity extends AppCompatActivity {

    private TextView tvWifiState;
    private TextView tvSsid;
    private TextView tvBssid;
    private TextView tvSignal;
    private TextView tvPassword;
    private ImageButton btnCopy;
    private Button btnRefresh;

    private WifiManager wifiManager;
    private boolean hasRoot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_wifi_password);

        tvWifiState = findViewById(R.id.tv_wifi_state);
        tvSsid = findViewById(R.id.tv_ssid);
        tvBssid = findViewById(R.id.tv_bssid);
        tvSignal = findViewById(R.id.tv_signal);
        tvPassword = findViewById(R.id.tv_password);
        btnCopy = findViewById(R.id.btn_copy);
        btnRefresh = findViewById(R.id.btn_refresh);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 检测 ROOT 权限
        hasRoot = checkRootPermission();

        btnRefresh.setOnClickListener(v -> refreshWifiInfo());

        btnCopy.setOnClickListener(v -> copyPassword());

        refreshWifiInfo();
    }

    private boolean checkRootPermission() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su -c id");
            BufferedReader reader = new BufferedReader(new FileReader("/proc/" + process.waitFor() + "/status"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Uid:")) {
                    int uid = Integer.parseInt(line.split("\\s+")[1]);
                    // UID 0 是 root
                    return uid == 0;
                }
            }
        } catch (IOException | InterruptedException e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    private void refreshWifiInfo() {
        if (!wifiManager.isWifiEnabled()) {
            tvWifiState.setText("WiFi 已关闭");
            tvWifiState.setTextColor(getColor(R.color.error));
            tvSsid.setText("--");
            tvBssid.setText("--");
            tvSignal.setText("--");
            tvPassword.setText("请先开启 WiFi");
            tvPassword.setTextColor(getColor(R.color.error));
            btnCopy.setVisibility(View.GONE);
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getNetworkId() == -1) {
            tvWifiState.setText("未连接");
            tvWifiState.setTextColor(getColor(R.color.error));
            tvSsid.setText("--");
            tvBssid.setText("--");
            tvSignal.setText("--");
            tvPassword.setText("未连接到任何 WiFi");
            tvPassword.setTextColor(getColor(R.color.error));
            btnCopy.setVisibility(View.GONE);
            return;
        }

        tvWifiState.setText("已连接");
        tvWifiState.setTextColor(getColor(R.color.secondary));

        String ssid = wifiInfo.getSSID();
        // 去除引号
        if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        if ("<unknown ssid>".equals(ssid)) {
            ssid = "--";
        }
        tvSsid.setText(ssid);

        String bssid = wifiInfo.getBSSID();
        tvBssid.setText(bssid != null ? bssid : "--");

        int rssi = wifiInfo.getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        String[] signalLevels = {"极弱", "较弱", "中等", "较强", "很强"};
        String signalDesc = rssi + " dBm (" + signalLevels[level] + ")";
        tvSignal.setText(signalDesc);

        // 获取密码
        String password = getWifiPassword();
        if (password != null) {
            tvPassword.setText(password);
            tvPassword.setTextColor(getColor(R.color.on_surface));
            btnCopy.setVisibility(View.VISIBLE);
        } else {
            tvPassword.setText("需要 ROOT 权限");
            tvPassword.setTextColor(getColor(R.color.error));
            btnCopy.setVisibility(View.GONE);
        }
    }

    private String getWifiPassword() {
        if (!hasRoot) {
            // 尝试通过系统 API 获取
            try {
                // 通过反射获取保存的 WiFi 配置
                Method getConfiguredNetworks = WifiManager.class.getMethod("getConfiguredNetworks");
                @SuppressWarnings("unchecked")
                java.util.List<android.net.wifi.WifiConfiguration> configs =
                        (java.util.List<android.net.wifi.WifiConfiguration>) getConfiguredNetworks.invoke(wifiManager);

                WifiInfo currentInfo = wifiManager.getConnectionInfo();
                if (currentInfo != null) {
                    int currentNetId = currentInfo.getNetworkId();
                    if (configs != null) {
                        for (android.net.wifi.WifiConfiguration config : configs) {
                            if (config.networkId == currentNetId && config.preSharedKey != null) {
                                // 去除引号
                                String pwd = config.preSharedKey;
                                if (pwd.startsWith("\"") && pwd.endsWith("\"")) {
                                    pwd = pwd.substring(1, pwd.length() - 1);
                                }
                                return pwd;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // API 调用失败
            }
            return null;
        }

        // ROOT 方式读取配置文件
        try {
            Process process = Runtime.getRuntime().exec("su -c cat /data/misc/wifi/wpa_supplicant.conf");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentSsid = null;
            String currentPsk = null;

            WifiInfo currentInfo = wifiManager.getConnectionInfo();
            String currentSsidRaw = currentInfo != null ? currentInfo.getSSID() : null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("network=")) {
                    currentSsid = null;
                    currentPsk = null;
                } else if (line.startsWith("ssid=")) {
                    currentSsid = line.substring(5).replace("\"", "");
                } else if (line.startsWith("psk=")) {
                    currentPsk = line.substring(4).replace("\"", "");
                    // 检查是否是当前连接的 WiFi
                    if (currentSsidRaw != null && currentSsidRaw.contains(currentSsid)) {
                        return currentPsk;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            Toast.makeText(this, "读取 WiFi 配置失败", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void copyPassword() {
        String password = tvPassword.getText().toString();
        if (password == null || password.startsWith("需要") || password.startsWith("--")) {
            Toast.makeText(this, "无法复制密码", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("WiFi密码", password);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "密码已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
}