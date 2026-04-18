package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class WorldCapitalsActivity extends AppCompatActivity {

    private AutoCompleteTextView actvCountry;
    private TextView tvCapital;
    private Map<String, String> countryCapitalMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_capitals);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("世界各国首都查询");
        }

        actvCountry = findViewById(R.id.actv_country);
        tvCapital = findViewById(R.id.tv_capital);

        initCountryData();

        String[] countries = countryCapitalMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, countries);
        actvCountry.setAdapter(adapter);

        actvCountry.setOnItemClickListener((parent, view, position, id) -> {
            String country = parent.getItemAtPosition(position).toString();
            tvCapital.setText("首都：" + countryCapitalMap.get(country));
        });
    }

    private void initCountryData() {
        countryCapitalMap = new HashMap<>();
        countryCapitalMap.put("中国", "北京");
        countryCapitalMap.put("美国", "华盛顿");
        countryCapitalMap.put("英国", "伦敦");
        countryCapitalMap.put("法国", "巴黎");
        countryCapitalMap.put("德国", "柏林");
        countryCapitalMap.put("日本", "东京");
        countryCapitalMap.put("韩国", "首尔");
        countryCapitalMap.put("俄罗斯", "莫斯科");
        countryCapitalMap.put("加拿大", "渥太华");
        countryCapitalMap.put("澳大利亚", "堪培拉");
        countryCapitalMap.put("巴西", "巴西利亚");
        countryCapitalMap.put("印度", "新德里");
        countryCapitalMap.put("意大利", "罗马");
        countryCapitalMap.put("西班牙", "马德里");
        countryCapitalMap.put("墨西哥", "墨西哥城");
        countryCapitalMap.put("阿根廷", "布宜诺斯艾利斯");
        countryCapitalMap.put("南非", "比勒陀利亚");
        countryCapitalMap.put("埃及", "开罗");
        countryCapitalMap.put("沙特阿拉伯", "利雅得");
        countryCapitalMap.put("泰国", "曼谷");
        countryCapitalMap.put("越南", "河内");
        countryCapitalMap.put("新加坡", "新加坡");
        countryCapitalMap.put("菲律宾", "马尼拉");
        countryCapitalMap.put("印度尼西亚", "雅加达");
        countryCapitalMap.put("马来西亚", "吉隆坡");
        countryCapitalMap.put("土耳其", "安卡拉");
        countryCapitalMap.put("荷兰", "阿姆斯特丹");
        countryCapitalMap.put("瑞士", "伯尔尼");
        countryCapitalMap.put("瑞典", "斯德哥尔摩");
        countryCapitalMap.put("挪威", "奥斯陆");
        countryCapitalMap.put("丹麦", "哥本哈根");
        countryCapitalMap.put("芬兰", "赫尔辛基");
        countryCapitalMap.put("波兰", "华沙");
        countryCapitalMap.put("希腊", "雅典");
        countryCapitalMap.put("葡萄牙", "里斯本");
        countryCapitalMap.put("新西兰", "惠灵顿");
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}