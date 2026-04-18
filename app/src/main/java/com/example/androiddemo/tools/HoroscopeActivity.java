package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HoroscopeActivity extends AppCompatActivity {

    private Spinner spinnerZodiac;
    private RadioGroup rgDayType;
    private TextView tvResult;
    private Map<String, String[]> horoscopeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horoscope);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("每日星座运势");
        }

        spinnerZodiac = findViewById(R.id.spinner_zodiac);
        rgDayType = findViewById(R.id.rg_day_type);
        tvResult = findViewById(R.id.tv_result);

        initHoroscopeData();

        String[] zodiacs = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
                           "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, zodiacs);
        spinnerZodiac.setAdapter(adapter);

        rgDayType.setOnCheckedChangeListener((group, checkedId) -> showHoroscope());

        showHoroscope();
    }

    private void initHoroscopeData() {
        horoscopeData = new HashMap<>();
        horoscopeData.put("白羊座", new String[]{"今天运势极佳，行动力满满", "贵人运旺盛，把握机会", "财运上扬，意外收获"});
        horoscopeData.put("金牛座", new String[]{"今天运势平稳，宜稳扎稳打", "爱情运不错，单身者有桃花", "财务状况良好，控制支出"});
        horoscopeData.put("双子座", new String[]{"今天思维活跃，适合沟通", "学习运佳，进步明显", "人际关系和谐"});
        horoscopeData.put("巨蟹座", new String[]{"今天情绪稳定，内心平静", "家庭运佳，宜陪伴家人", "健康运尚可，注意休息"});
        horoscopeData.put("狮子座", new String[]{"今天自信满满，魅力四射", "事业运上升，获得认可", "桃花运旺盛"});
        horoscopeData.put("处女座", new String[]{"今天适合细致工作", "学业运佳，效率提高", "注意健康管理"});
        horoscopeData.put("天秤座", new String[]{"今天社交运佳，人脉拓展", "感情运稳定，甜蜜相处", "艺术运佳，提升审美"});
        horoscopeData.put("天蝎座", new String[]{"今天洞察力强，适合深入研究", "事业运上升，业绩提升", "偏财运佳"});
        horoscopeData.put("射手座", new String[]{"今天自由奔放，行动自由", "旅行运佳，出行顺利", "学习运佳开阔视野"});
        horoscopeData.put("摩羯座", new String[]{"今天脚踏实，稳步前进", "事业运稳定，计划推进", "财运平稳，注意储蓄"});
        horoscopeData.put("水瓶座", new String[]{"今天创意十足，灵感迸发", "人际关系有新发展", "健康运尚可"});
        horoscopeData.put("双鱼座", new String[]{"今天直觉敏锐，适合冥想", "艺术运佳，创作顺利", "感情运甜蜜"});
    }

    private void showHoroscope() {
        String zodiac = spinnerZodiac.getSelectedItem().toString();
        String[] fortunes = horoscopeData.get(zodiac);

        int dayType = rgDayType.getCheckedRadioButtonId() == R.id.rb_today ? 0 :
                      rgDayType.getCheckedRadioButtonId() == R.id.rb_tomorrow ? 1 : 2;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String dateStr = sdf.format(Calendar.getInstance().getTime());

        String result = dateStr + " " + zodiac + "运势\n\n" +
                       "综合运势：" + fortunes[dayType] + "\n\n" +
                       "今日建议：保持积极心态，把握机遇";

        tvResult.setText(result);
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