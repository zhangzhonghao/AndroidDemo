package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class FoodCaloriesActivity extends AppCompatActivity {

    private EditText etFood;
    private TextView tvResult;
    private Map<String, String[]> foodData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_calories);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("食物卡路里");
        }

        etFood = findViewById(R.id.et_food);
        tvResult = findViewById(R.id.tv_result);

        initFoodData();

        etFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchFood(s.toString());
            }
        });
    }

    private void initFoodData() {
        foodData = new HashMap<>();
        // 每100克卡路里
        foodData.put("米饭", new String[]{"116", "碳水化合物"});
        foodData.put("面条", new String[]{"137", "碳水化合物"});
        foodData.put("馒头", new String[]{"223", "碳水化合物"});
        foodData.put("饺子", new String[]{"242", "碳水化合物·脂肪"});
        foodData.put("包子", new String[]{"227", "碳水化合物·脂肪"});
        foodData.put("鸡蛋", new String[]{"144", "蛋白质"});
        foodData.put("鸡胸肉", new String[]{"133", "蛋白质"});
        foodData.put("鱼肉", new String[]{"90", "蛋白质"});
        foodData.put("猪肉", new String[]{"143", "脂肪·蛋白质"});
        foodData.put("牛肉", new String[]{"125", "蛋白质"});
        foodData.put("苹果", new String[]{"52", "碳水化合物"});
        foodData.put("香蕉", new String[]{"93", "碳水化合物"});
        foodData.put("橙子", new String[]{"47", "碳水化合物"});
        foodData.put("西瓜", new String[]{"30", "碳水化合物"});
        foodData.put("葡萄", new String[]{"67", "碳水化合物"});
        foodData.put("白菜", new String[]{"17", "膳食纤维"});
        foodData.put("西红柿", new String[]{"19", "维生素"});
        foodData.put("黄瓜", new String[]{"15", "膳食纤维"});
        foodData.put("胡萝卜", new String[]{"35", "膳食纤维"});
        foodData.put("土豆", new String[]{"76", "碳水化合物"});
        foodData.put("红薯", new String[]{"99", "碳水化合物"});
        foodData.put("牛奶", new String[]{"54", "蛋白质"});
        foodData.put("酸奶", new String[]{"72", "蛋白质"});
        foodData.put("豆浆", new String[]{"33", "蛋白质"});
        foodData.put("豆腐", new String[]{"81", "蛋白质"});
        foodData.put("可乐", new String[]{"42", "糖分"});
        foodData.put("奶茶", new String[]{"67", "糖分·脂肪"});
        foodData.put("薯片", new String[]{"548", "脂肪"});
        foodData.put("巧克力", new String[]{"546", "脂肪·糖分"});
        foodData.put("坚果", new String[]{"600", "脂肪"});
    }

    private void searchFood(String keyword) {
        if (keyword.isEmpty()) {
            tvResult.setText("请输入食物名称查询卡路里\n\n常见食物热量（每100克）：\n米饭：116千卡\n面条：137千卡\n鸡蛋：144千卡\n鸡胸肉：133千卡");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (String food : foodData.keySet()) {
            if (food.contains(keyword)) {
                String[] info = foodData.get(food);
                result.append(food).append("：").append(info[0])
                      .append("千卡/100g").append(" (").append(info[1]).append(")\n");
            }
        }

        if (result.length() == 0) {
            tvResult.setText("未找到\"" + keyword + "\"相关信息\n请尝试其他食物名称");
        } else {
            tvResult.setText(result.toString());
        }
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
