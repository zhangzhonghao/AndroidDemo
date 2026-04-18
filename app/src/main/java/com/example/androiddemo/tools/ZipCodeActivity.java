package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class ZipCodeActivity extends AppCompatActivity {
    private EditText etProvince, etCity, etDistrict;
    private TextView tvResult;
    private Map<String, String> zipCodeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zip_code);
        etProvince = findViewById(R.id.et_province);
        etCity = findViewById(R.id.et_city);
        etDistrict = findViewById(R.id.et_district);
        tvResult = findViewById(R.id.tv_result);
        initZipCodes();
    }

    private void initZipCodes() {
        zipCodeMap.put("北京市北京市东城区", "100010");
        zipCodeMap.put("北京市北京市朝阳区", "100020");
        zipCodeMap.put("上海市上海市黄浦区", "200001");
        zipCodeMap.put("上海市上海市浦东新区", "200120");
        zipCodeMap.put("广东省广州市天河区", "510630");
        zipCodeMap.put("广东省深圳市福田区", "518031");
        zipCodeMap.put("浙江省杭州市西湖区", "310013");
        zipCodeMap.put("江苏省南京市鼓楼区", "210009");
    }

    public void query(View view) {
        String province = etProvince.getText().toString();
        String city = etCity.getText().toString();
        String district = etDistrict.getText().toString();
        if (TextUtils.isEmpty(province) || TextUtils.isEmpty(city) || TextUtils.isEmpty(district)) {
            tvResult.setText("请输入完整的省市区信息");
            return;
        }
        String key = province + city + district;
        String code = zipCodeMap.get(key);
        if (code != null) {
            tvResult.setText("邮编: " + code);
        } else {
            tvResult.setText("未找到该地区的邮编信息\n\n常见邮编:\n北京东城: 100010\n上海黄浦: 200001\n广州天河: 510630\n深圳福田: 518031\n杭州西湖: 310013");
        }
    }
}