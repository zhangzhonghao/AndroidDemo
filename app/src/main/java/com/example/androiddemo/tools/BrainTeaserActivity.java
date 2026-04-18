package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BrainTeaserActivity extends AppCompatActivity {

    private TextView tvQuestion;
    private TextView tvAnswer;
    private Button btnShowAnswer;
    private Button btnNext;

    private int currentIndex = 0;
    private final List<BrainTeaser> brainTeasers = new ArrayList<>();
    private final Random random = new Random();

    private static class BrainTeaser {
        String question;
        String answer;
        BrainTeaser(String q, String a) { question = q; answer = a; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brain_teaser);

        tvQuestion = findViewById(R.id.tv_question);
        tvAnswer = findViewById(R.id.tv_answer);
        btnShowAnswer = findViewById(R.id.btn_show_answer);
        btnNext = findViewById(R.id.btn_next);

        initBrainTeasers();
        showRandomTeaser();
    }

    private void initBrainTeasers() {
        brainTeasers.add(new BrainTeaser("什么东西越洗越脏？", "水"));
        brainTeasers.add(new BrainTeaser("什么门永远关不上？", "球门"));
        brainTeasers.add(new BrainTeaser("什么牛不能吃？", "蜗牛"));
        brainTeasers.add(new BrainTeaser("什么鱼不能吃？", "木鱼"));
        brainTeasers.add(new BrainTeaser("什么布剪不断？", "瀑布"));
        brainTeasers.add(new BrainTeaser("什么柴火焰高？", "星星之火"));
        brainTeasers.add(new BrainTeaser("什么窗开着关不上？", "脑洞"));
        brainTeasers.add(new BrainTeaser("什么鸟不会飞？", "企鹅"));
        brainTeasers.add(new BrainTeaser("什么蛋打不烂？", "脸蛋"));
        brainTeasers.add(new BrainTeaser("什么瓜不能吃？", "傻瓜"));
    }

    private void showRandomTeaser() {
        currentIndex = random.nextInt(brainTeasers.size());
        BrainTeaser teaser = brainTeasers.get(currentIndex);
        tvQuestion.setText(teaser.question);
        tvAnswer.setText(teaser.answer);
        tvAnswer.setVisibility(View.GONE);
        btnShowAnswer.setVisibility(View.VISIBLE);
    }

    public void onShowAnswerClick(View view) {
        tvAnswer.setVisibility(View.VISIBLE);
        btnShowAnswer.setVisibility(View.GONE);
    }

    public void onNextClick(View view) {
        showRandomTeaser();
    }
}