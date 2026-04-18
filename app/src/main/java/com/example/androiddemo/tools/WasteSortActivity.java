package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class WasteSortActivity extends AppCompatActivity {
    private EditText etInput;
    private TextView tvResult;
    private Map<String, String> wasteDatabase = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waste_sort);
        etInput = findViewById(R.id.et_waste_input);
        tvResult = findViewById(R.id.tv_waste_result);
        findViewById(R.id.btn_search).setOnClickListener(v -> searchWaste());
        initDatabase();
    }

    private void initDatabase() {
        wasteDatabase.put("香蕉皮", "湿垃圾");
        wasteDatabase.put("苹果核", "湿垃圾");
        wasteDatabase.put("剩菜", "湿垃圾");
        wasteDatabase.put("塑料瓶", "可回收垃圾");
        wasteDatabase.put("纸箱", "可回收垃圾");
        wasteDatabase.put("易拉罐", "可回收垃圾");
        wasteDatabase.put("电池", "有害垃圾");
        wasteDatabase.put("灯管", "有害垃圾");
        wasteDatabase.put("过期药品", "有害垃圾");
        wasteDatabase.put("烟头", "干垃圾");
        wasteDatabase.put("一次性餐具", "干垃圾");
        wasteDatabase.put("陶瓷", "干垃圾");
        wasteDatabase.put("玻璃瓶", "可回收垃圾");
        wasteDatabase.put("衣物", "可回收垃圾");
        wasteDatabase.put("杀虫剂", "有害垃圾");
    }

    private void searchWaste() {
        String input = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            tvResult.setText("请输入垃圾名称");
            return;
        }
        String result = wasteDatabase.get(input);
        if (result != null) {
            tvResult.setText(input + " 属于：" + result);
        } else {
            tvResult.setText("未找到该垃圾的分类信息，请自行查询当地分类标准");
        }
    }
}