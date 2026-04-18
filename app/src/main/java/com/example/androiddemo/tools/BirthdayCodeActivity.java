package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BirthdayCodeActivity extends AppCompatActivity {

    private DatePicker dpBirthday;
    private TextView tvResult;
    private Map<Integer, String[]> birthdayCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_code);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("生日密码");
        }

        dpBirthday = findViewById(R.id.dp_birthday);
        tvResult = findViewById(R.id.tv_result);

        initBirthdayCodes();

        dpBirthday.init(2000, 0, 1, (view, year, monthOfYear, dayOfMonth) -> {
            calculateBirthdayCode(year, monthOfYear + 1, dayOfMonth);
        });

        calculateBirthdayCode(2000, 1, 1);
    }

    private void initBirthdayCodes() {
        birthdayCodes = new HashMap<>();
        birthdayCodes.put(1, new String[]{"独立、自主", "创造者、革新者", "领导者、冒险家"});
        birthdayCodes.put(2, new String[]{"合作、平衡", "外交家、和平者", "艺术家、梦想家"});
        birthdayCodes.put(3, new String[]{"表达、社交", "沟通者、表演者", "作家、艺术家"});
        birthdayCodes.put(4, new String[]{"稳定、勤劳", "实践者、组织者", "建造者、工程师"});
        birthdayCodes.put(5, new String[]{"自由、变化", "探索者、旅行家", "企业家、改革者"});
        birthdayCodes.put(6, new String[]{"责任、关怀", "教育者、照顾者", "艺术家、服务者"});
        birthdayCodes.put(7, new String[]{"分析、智慧", "思想家、学者", "科学家、哲学家"});
        birthdayCodes.put(8, new String[]{"成就、权威", "领导者、决策者", "企业家、成功者"});
        birthdayCodes.put(9, new String[]{"博爱、理想", "人道主义者、慈善家", "艺术家、梦想家"});
    }

    private void calculateBirthdayCode(int year, int month, int day) {
        // 计算生命密码（出生日期数字相加直到个位数）
        int sum = year + month + day;
        while (sum > 9 && sum != 11 && sum != 22 && sum != 33) {
            int temp = 0;
            while (sum > 0) {
                temp += sum % 10;
                sum /= 10;
            }
            sum = temp;
        }

        String[] traits = birthdayCodes.get(sum);
        if (traits == null) {
            traits = new String[]{"神秘", "独特", "不凡"};
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String birthStr = sdf.format(Calendar.getInstance().getTime());

        String result = "您的生日： " + year + "年" + month + "月" + day + "日\n\n" +
                        "生命密码：" + sum + "\n\n" +
                        "性格特点：\n" + traits[0] + "\n" + traits[1] + "\n" + traits[2];

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