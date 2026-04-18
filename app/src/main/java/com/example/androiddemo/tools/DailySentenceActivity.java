package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class DailySentenceActivity extends AppCompatActivity {
    private TextView tvSentence;
    private TextView tvSource;
    private Button btnRefresh;
    private int currentIndex = 0;

    private final String[] sentences = {
        "「人生若只如初见，何事秋风悲画扇」—— 纳兰性德",
        "「世界以痛吻我，要我报之以歌」—— 泰戈尔",
        "「你若安好，便是晴天」",
        "「愿你出走半生，归来仍是少年」",
        "「不忘初心，方得始终」",
        "「凡是过往，皆为序章」—— 莎士比亚",
        "「温柔半两，从容一生」",
        "「心有猛虎，细嗅蔷薇」—— 萨松",
        "「浅水是喧哗的，深水是沉默的」—— 雪莱",
        "「纵有疾风起，人生不言弃」—— 保罗·瓦雷里",
        "「没有人是一座孤岛，在大海里独踞」—— 约翰·多恩",
        "「我荒废了时间，时间便把我荒废了」—— 莎士比亚",
        "「当你凝视深渊时，深渊也在凝视你」—— 尼采",
        "「人的一生是短的，但如果卑劣地过这短的一生，就太长了」—— 莎士比亚",
        "「生活最佳状态是冷冷清清地风风火火」—— 木心"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_sentence);

        tvSentence = findViewById(R.id.tv_sentence);
        tvSource = findViewById(R.id.tv_source);
        btnRefresh = findViewById(R.id.btn_refresh);

        showSentence();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = (currentIndex + 1) % sentences.length;
                showSentence();
            }
        });
    }

    private void showSentence() {
        String sentence = sentences[currentIndex];
        if (sentence.contains("——")) {
            String[] parts = sentence.split("——");
            tvSentence.setText(parts[0].trim());
            tvSource.setText("——" + parts[1].trim());
        } else {
            tvSentence.setText(sentence);
            tvSource.setText("");
        }
    }
}