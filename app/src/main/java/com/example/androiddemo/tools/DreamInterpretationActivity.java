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
import java.util.Random;

public class DreamInterpretationActivity extends AppCompatActivity {

    private EditText etDream;
    private TextView tvResult;
    private Random random = new Random();

    private static final Map<String, String[]> DREAM_MAP = new HashMap<>();

    static {
        DREAM_MAP.put("蛇", new String[]{"梦见蛇通常表示潜意识中的恐惧或性暗示，也可能预示财富增长", "梦见蛇缠身可能表示有人对你不利，需要小心身边的人"});
        DREAM_MAP.put("水", new String[]{"梦见清水是好兆头，预示生活顺利", "梦见洪水可能表示情绪波动较大，需要控制情绪"});
        DREAM_MAP.put("钱", new String[]{"梦见捡钱表示财运上升，投资需谨慎", "梦见丢钱表示可能破财，要小心保管财物"});
        DREAM_MAP.put("房子", new String[]{"梦见新房表示对未来的期待", "梦见旧房子可能表示怀旧情绪"});
        DREAM_MAP.put("死亡", new String[]{"梦见死亡通常表示新生，是好兆头", "可能表示某种结束或新的开始"});
        DREAM_MAP.put("结婚", new String[]{"梦见自己结婚表示期望改变", "梦见别人结婚可能表示对感情的期待"});
        DREAM_MAP.put("考试", new String[]{"梦见考试表示压力或责任", "梦见考试顺利表示能力被认可"});
        DREAM_MAP.put("飞行", new String[]{"梦见飞行表示对自由的向往", "可能表示野心或抱负"});
        DREAM_MAP.put("下雨", new String[]{"梦见细雨表示财运平稳", "梦见暴雨可能表示挫折或困难"});
        DREAM_MAP.put("阳光", new String[]{"梦见阳光明媚表示心情愉快", "可能表示希望和机遇"});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dream_interpretation);

        etDream = findViewById(R.id.et_dream);
        tvResult = findViewById(R.id.tv_result);
    }

    public void onInterpretClick(View view) {
        String dream = etDream.getText().toString().trim();
        if (TextUtils.isEmpty(dream)) {
            tvResult.setText("请输入您梦到的内容");
            return;
        }

        String[] interpretations = findInterpretation(dream);
        StringBuilder sb = new StringBuilder();
        sb.append("梦见\"").append(dream).append("\"的解读：\n\n");
        for (String interp : interpretations) {
            sb.append("• ").append(interp).append("\n\n");
        }
        tvResult.setText(sb.toString());
    }

    private String[] findInterpretation(String dream) {
        for (Map.Entry<String, String[]> entry : DREAM_MAP.entrySet()) {
            if (dream.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new String[]{"梦境的含义因人而异，建议您回想梦中细节，结合自身情况理解", "一般梦境解读仅供参考，不可迷信"};
    }
}