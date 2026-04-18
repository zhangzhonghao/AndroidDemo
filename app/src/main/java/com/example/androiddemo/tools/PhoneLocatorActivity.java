package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class PhoneLocatorActivity extends AppCompatActivity {
    private EditText etPhone;
    private TextView tvResult;
    private Map<String, String[]> phoneDatabase = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_locator);
        etPhone = findViewById(R.id.et_phone_input);
        tvResult = findViewById(R.id.tv_phone_result);
        findViewById(R.id.btn_query).setOnClickListener(v -> queryPhone());
        initDatabase();
    }

    private void initDatabase() {
        phoneDatabase.put("138", new String[]{"中国移动", "GSM"});
        phoneDatabase.put("139", new String[]{"中国移动", "GSM"});
        phoneDatabase.put("147", new String[]{"中国移动", "TD-SCDMA"});
        phoneDatabase.put("150", new String[]{"中国移动", "GSM"});
        phoneDatabase.put("158", new String[]{"中国移动", "GSM"});
        phoneDatabase.put("159", new String[]{"中国移动", "GSM"});
        phoneDatabase.put("186", new String[]{"中国联通", "WCDMA"});
        phoneDatabase.put("185", new String[]{"中国联通", "WCDMA"});
        phoneDatabase.put("176", new String[]{"中国联通", "FDD-LTE"});
        phoneDatabase.put("187", new String[]{"中国移动", "TD-SCDMA"});
        phoneDatabase.put("188", new String[]{"中国移动", "TD-SCDMA"});
        phoneDatabase.put("189", new String[]{"中国电信", "CDMA2000"});
        phoneDatabase.put("181", new String[]{"中国电信", "CDMA2000"});
        phoneDatabase.put("177", new String[]{"中国联通", "FDD-LTE"});
        phoneDatabase.put("170", new String[]{"虚拟运营商", "GSM"});
    }

    private void queryPhone() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            tvResult.setText("请输入电话号码");
            return;
        }
        if (phone.length() < 7) {
            tvResult.setText("请输入完整的电话号码");
            return;
        }
        String prefix = phone.substring(0, 3);
        String[] info = phoneDatabase.get(prefix);
        if (info != null) {
            tvResult.setText("运营商：" + info[0] + "\n网络制式：" + info[1] + "\n归属地：仅支持部分号段查询");
        } else {
            tvResult.setText("未找到该号段信息，请自行查询");
        }
    }
}