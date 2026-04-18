package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class IpLookupActivity extends AppCompatActivity {
    private TextView tvLocalIp;
    private EditText etIpInput;
    private TextView tvIpResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_lookup);
        tvLocalIp = findViewById(R.id.tv_local_ip);
        etIpInput = findViewById(R.id.et_ip_input);
        tvIpResult = findViewById(R.id.tv_ip_result);
        findViewById(R.id.btn_query_ip).setOnClickListener(v -> queryIp());
        displayLocalIp();
    }

    private void displayLocalIp() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        if (sAddr.contains(":") || sAddr.startsWith("192.") || sAddr.startsWith("10.") || sAddr.startsWith("172.")) {
                            tvLocalIp.setText("本机IP地址: " + sAddr);
                            return;
                        }
                    }
                }
            }
            tvLocalIp.setText("无法获取IP地址");
        } catch (Exception e) {
            tvLocalIp.setText("获取IP地址失败");
        }
    }

    private void queryIp() {
        String ip = etIpInput.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            tvIpResult.setText("请输入IP地址");
            return;
        }
        // 简单的IP归属地查询（静态数据演示）
        if (ip.startsWith("192.168.")) {
            tvIpResult.setText("归属地：局域网内网IP\n运营商：本地网络");
        } else if (ip.startsWith("10.")) {
            tvIpResult.setText("归属地：局域网内网IP\n运营商：本地网络");
        } else {
            tvIpResult.setText("归属地：公网IP\n说明：精确归属地需联网查询\n提示：本功能仅显示本机IP");
        }
    }
}