package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoupletActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvResult;
    private Random random = new Random();

    private final List<Couplet> COUPLETS = new ArrayList<>();

    private static class Couplet {
        String first;
        String second;
        Couplet(String f, String s) { first = f; second = s; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couplet);

        etInput = findViewById(R.id.et_input);
        tvResult = findViewById(R.id.tv_result);

        initCouplets();
    }

    private void initCouplets() {
        COUPLETS.add(new Couplet("春回大地", "福满人间"));
        COUPLETS.add(new Couplet("万事如意", "百事可乐"));
        COUPLETS.add(new Couplet("喜气盈门", "吉祥满堂"));
        COUPLETS.add(new Couplet("心想事成", "万事如意"));
        COUPLETS.add(new Couplet("福星高照", "吉庆有余"));
        COUPLETS.add(new Couplet("恭喜发财", "红包拿来"));
        COUPLETS.add(new Couplet("年年有余", "岁岁平安"));
        COUPLETS.add(new Couplet("国泰民安", "五谷丰登"));
    }

    public void onGenerateClick(View view) {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            tvResult.setText("请输入上联");
            return;
        }

        Couplet matched = findBestMatch(input);
        StringBuilder sb = new StringBuilder();
        sb.append("上联：").append(input).append("\n\n");
        sb.append("下联：").append(matched.second).append("\n\n");
        sb.append("横批：").append(getRandomHorizontal()).append("\n\n");
        sb.append("--- 经典春联参考 ---\n");
        for (Couplet c : COUPLETS) {
            sb.append("上联：").append(c.first).append("\n");
            sb.append("下联：").append(c.second).append("\n\n");
        }
        tvResult.setText(sb.toString());
    }

    private Couplet findBestMatch(String input) {
        return COUPLETS.get(random.nextInt(COUPLETS.size()));
    }

    private String getRandomHorizontal() {
        String[] horizontals = {"新春快乐", "恭喜发财", "福满人间", "万事如意", "迎春接福"};
        return horizontals[random.nextInt(horizontals.length)];
    }

    public void onClassicClick(View view) {
        StringBuilder sb = new StringBuilder("--- 经典春联大全 ---\n\n");
        for (Couplet c : COUPLETS) {
            sb.append("上联：").append(c.first).append("\n");
            sb.append("下联：").append(c.second).append("\n\n");
        }
        tvResult.setText(sb.toString());
    }
}