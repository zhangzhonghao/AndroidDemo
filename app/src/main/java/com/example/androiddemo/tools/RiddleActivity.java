package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class RiddleActivity extends AppCompatActivity {

    private TextView tvRiddle;
    private ListView lvAnswer;
    private List<String> answers;
    private ArrayAdapter<String> adapter;
    private int currentIndex = 0;

    private final List<String[]> RIDDLES = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riddle);

        tvRiddle = findViewById(R.id.tv_riddle);
        lvAnswer = findViewById(R.id.rv_history);

        answers = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, answers);
        lvAnswer.setAdapter(adapter);

        initRiddles();
        showRandomRiddle();
    }

    private void initRiddles() {
        RIDDLES.add(new String[]{"上下一体（打一字）", "卡"});
        RIDDLES.add(new String[]{"人在草木中（打一字）", "茶"});
        RIDDLES.add(new String[]{"七人八千（打一字）", "仞"});
        RIDDLES.add(new String[]{"一月一日（打一字）", "胆"});
        RIDDLES.add(new String[]{"七十二小时（打一字）", "晶"});
        RIDDLES.add(new String[]{"九点（打一字）", "丸"});
        RIDDLES.add(new String[]{"一加一（打一字）", "王"});
        RIDDLES.add(new String[]{"一半儿（打一字）", "伴"});
        RIDDLES.add(new String[]{"反比（打一字）", "北"});
        RIDDLES.add(new String[]{"有目共睹（打一字）", "者"});
    }

    private void showRandomRiddle() {
        if (RIDDLES.isEmpty()) return;
        currentIndex = (int) (Math.random() * RIDDLES.size());
        String[] riddle = RIDDLES.get(currentIndex);
        tvRiddle.setText(riddle[0]);
        answers.clear();
        answers.add("点击查看答案");
        adapter.notifyDataSetChanged();
    }

    public void onRevealClick(View view) {
        if (RIDDLES.isEmpty()) return;
        String[] riddle = RIDDLES.get(currentIndex);
        answers.clear();
        answers.add("谜底：" + riddle[1]);
        adapter.notifyDataSetChanged();
    }

    public void onNextClick(View view) {
        showRandomRiddle();
    }
}
