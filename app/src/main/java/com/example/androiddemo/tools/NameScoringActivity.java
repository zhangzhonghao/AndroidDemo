package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class NameScoringActivity extends AppCompatActivity {

    private EditText etName;
    private TextView tvResult;
    private Random random = new Random();

    private static final String[] GOOD_MEANINGS = {
        "心地善良，为人真诚", "聪明伶俐，反应敏捷", "性格开朗，人缘极好",
        "稳重可靠，值得信赖", "富有创造力，艺术天赋", "意志坚定，敢闯敢拼"
    };

    private static final String[] BAD_MEANINGS = {
        "性格内向，不善表达", "有时优柔寡断", "过于理想化"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_scoring);

        etName = findViewById(R.id.et_name);
        tvResult = findViewById(R.id.tv_result);
    }

    public void onScoreClick(View view) {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            tvResult.setText("请输入姓名");
            return;
        }

        int totalScore = 0;
        for (char c : name.toCharArray()) {
            totalScore += (int) c;
        }
        int baseScore = (totalScore % 40) + 60;
        int giftScore = random.nextInt(21) - 5;
        int finalScore = Math.min(100, Math.max(1, baseScore + giftScore));

        String grade;
        if (finalScore >= 90) grade = "S级";
        else if (finalScore >= 80) grade = "A级";
        else if (finalScore >= 70) grade = "B级";
        else if (finalScore >= 60) grade = "C级";
        else grade = "D级";

        StringBuilder sb = new StringBuilder();
        sb.append("姓名：").append(name).append("\n\n");
        sb.append("综合评分：").append(finalScore).append("/100\n");
        sb.append("等级：").append(grade).append("\n\n");
        sb.append("性格分析：\n");
        if (finalScore >= 80) {
            sb.append(GOOD_MEANINGS[random.nextInt(GOOD_MEANINGS.length)]);
        } else {
            sb.append(BAD_MEANINGS[random.nextInt(BAD_MEANINGS.length)]);
        }
        sb.append("\n\n");
        sb.append("五格分析：\n");
        sb.append("天格：").append((name.charAt(0) % 10 + 1) * 10 + random.nextInt(10)).append("\n");
        sb.append("人格：").append((name.charAt(0) % 10 + name.charAt(name.length() - 1) % 10) * 5 + random.nextInt(10)).append("\n");
        sb.append("地格：").append((name.length() > 1 ? name.charAt(1) % 10 : 0) * 8 + random.nextInt(10)).append("\n");
        sb.append("外格：").append(name.length() * 3 + random.nextInt(10)).append("\n");
        sb.append("总格：").append(totalScore % 80 + 10 + random.nextInt(10)).append("\n");

        tvResult.setText(sb.toString());
    }
}