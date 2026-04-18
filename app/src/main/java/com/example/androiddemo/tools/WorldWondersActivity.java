package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class WorldWondersActivity extends AppCompatActivity {

    private TextView tvWonderName;
    private TextView tvWonderLocation;
    private TextView tvWonderDescription;
    private Spinner spinner;

    private String[] wonderNames = {
            "中国长城", "秦始皇兵马俑", "故宫", "莫高窟", "黄山", "杭州西湖", "苏州园林",
            "埃及金字塔", "自由女神像", "埃菲尔铁塔", "悉尼歌剧院", "马丘比丘",
            "泰姬陵", "吴哥窟", "佩特拉古城", "科罗西姆斗兽场"
    };

    private String[] wonderLocations = {
            "中国", "中国西安", "中国北京", "中国敦煌", "中国安徽", "中国杭州", "中国苏州",
            "埃及吉萨", "美国纽约", "法国巴黎", "澳大利亚悉尼", "秘鲁库斯科",
            "印度阿格拉", "柬埔寨暹粒", "约旦佩特拉", "意大利罗马"
    };

    private String[] wonderDescriptions = {
            "中国古代军事防御工程，绵延数千公里。",
            "秦始皇陵的陪葬坑，被誉为\"世界第八大奇迹\"。",
            "明清两代的皇家宫殿，是中国古代宫廷建筑的精华。",
            "世界上规模最大、内容最丰富的佛教艺术宝库。",
            "中国著名的风景游览胜地，以奇松、怪石、云海、温泉著称。",
            "中国著名的风景名胜，有\"西湖十景\"等景点。",
            "苏州古典园林，以其精美的建筑和山水布局闻名。",
            "古代世界七大奇迹之一，埋葬法老的陵墓。",
            "法国赠送美国的礼物，象征自由与民主。",
            "巴黎标志性建筑，以设计师埃菲尔命名。",
            "澳大利亚标志性建筑，形似帆船。",
            "印加帝国遗址，被称为\"失落之城\"。",
            "印度标志性建筑，蒙兀儿皇帝为妃建造的陵墓。",
            "高棉帝国遗址，世界上最大的宗教建筑群。",
            "纳巴泰人建造的玫瑰红城市。",
            "古罗马时期最大的圆形角斗场。"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_wonders);

        tvWonderName = findViewById(R.id.tv_wonder_name);
        tvWonderLocation = findViewById(R.id.tv_wonder_location);
        tvWonderDescription = findViewById(R.id.tv_wonder_description);
        spinner = findViewById(R.id.spinner_wonders);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wonderNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayWonder(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        displayWonder(0);
    }

    private void displayWonder(int index) {
        tvWonderName.setText(wonderNames[index]);
        tvWonderLocation.setText("📍 " + wonderLocations[index]);
        tvWonderDescription.setText(wonderDescriptions[index]);
    }
}
