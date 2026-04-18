package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class CountryInfoActivity extends AppCompatActivity {

    private TextView tvCountryName;
    private TextView tvCountryCapital;
    private TextView tvCountryPopulation;
    private TextView tvCountryLanguage;
    private TextView tvCountryCurrency;
    private TextView tvCountryArea;
    private Spinner spinner;

    private String[] countries = {"中国", "美国", "英国", "法国", "德国", "日本", "韩国",
            "俄罗斯", "加拿大", "澳大利亚", "巴西", "印度", "意大利", "西班牙", "墨西哥"};

    private String[] capitals = {"北京", "华盛顿", "伦敦", "巴黎", "柏林", "东京", "首尔",
            "莫斯科", "渥太华", "堪培拉", "巴西利亚", "新德里", "罗马", "马德里", "墨西哥城"};

    private String[] populations = {"14.4亿", "3.3亿", "6700万", "6700万", "8300万", "1.26亿",
            "5200万", "1.44亿", "3800万", "2600万", "2.15亿", "14.2亿", "6000万", "4700万", "1.3亿"};

    private String[] languages = {"中文", "英语", "英语", "法语", "德语", "日语", "韩语",
            "俄语", "英语/法语", "英语", "葡萄牙语", "印地语/英语", "意大利语", "西班牙语", "西班牙语"};

    private String[] currencies = {"人民币(CNY)", "美元(USD)", "英镑(GBP)", "欧元(EUR)", "欧元(EUR)",
            "日元(JPY)", "韩元(KRW)", "卢布(RUB)", "加元(CAD)", "澳元(AUD)",
            "雷亚尔(BRL)", "卢比(INR)", "欧元(EUR)", "欧元(EUR)", "比索(MXN)"};

    private String[] areas = {"960万 km²", "983万 km²", "24万 km²", "64万 km²", "36万 km²",
            "38万 km²", "10万 km²", "1710万 km²", "998万 km²", "769万 km²",
            "852万 km²", "329万 km²", "30万 km²", "51万 km²", "196万 km²"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_info);

        tvCountryName = findViewById(R.id.tv_country_name);
        tvCountryCapital = findViewById(R.id.tv_country_capital);
        tvCountryPopulation = findViewById(R.id.tv_country_population);
        tvCountryLanguage = findViewById(R.id.tv_country_language);
        tvCountryCurrency = findViewById(R.id.tv_country_currency);
        tvCountryArea = findViewById(R.id.tv_country_area);
        spinner = findViewById(R.id.spinner_country);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayCountry(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        displayCountry(0);
    }

    private void displayCountry(int index) {
        tvCountryName.setText(countries[index]);
        tvCountryCapital.setText("首都: " + capitals[index]);
        tvCountryPopulation.setText("人口: " + populations[index]);
        tvCountryLanguage.setText("语言: " + languages[index]);
        tvCountryCurrency.setText("货币: " + currencies[index]);
        tvCountryArea.setText("面积: " + areas[index]);
    }
}
