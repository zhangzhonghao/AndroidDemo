package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class CharRiddleActivity extends AppCompatActivity {

    private TextView tvRiddle;
    private ListView lvHints;
    private List<String> hints;
    private ArrayAdapter<String> adapter;
    private int currentIndex = 0;

    private final List<CharRiddle> riddles = new ArrayList<>();

    private static class CharRiddle {
        String riddle;
        String answer;
        List<String> hints;
        CharRiddle(String r, String a, List<String> h) {
            riddle = r; answer = a; hints = h;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_char_riddle);

        tvRiddle = findViewById(R.id.tv_riddle_content);
        lvHints = findViewById(R.id.rv_results);

        hints = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hints);
        lvHints.setAdapter(adapter);

        initRiddles();
        showRandomRiddle();
    }

    private void initRiddles() {
        riddles.add(new CharRiddle("一字十八口",
            "杏", List.of("十八即木", "口可能是部首")));
        riddles.add(new CharRiddle("一加一不等于二",
            "王", List.of("思考运算符号")));
        riddles.add(new CharRiddle("人在草木中",
            "茶", List.of("人+艹+木")));
        riddles.add(new CharRiddle("七人八千",
            "仞", List.of("七人八千是夸张说法")));
        riddles.add(new CharRiddle("上有十八，下有十八",
            "椿", List.of("十八即木")));
    }

    private void showRandomRiddle() {
        if (riddles.isEmpty()) return;
        currentIndex = (int) (Math.random() * riddles.size());
        CharRiddle riddle = riddles.get(currentIndex);
        tvRiddle.setText(riddle.riddle);
        hints.clear();
        hints.add("答案：" + riddle.answer);
        hints.addAll(riddle.hints);
        adapter.notifyDataSetChanged();
    }

    public void onNextClick(View view) {
        showRandomRiddle();
    }

    public void onShowHintClick(View view) {
        if (riddles.isEmpty()) return;
        CharRiddle riddle = riddles.get(currentIndex);
        hints.clear();
        hints.add("答案：" + riddle.answer);
        for (int i = 0; i < riddle.hints.size(); i++) {
            if (i < 2) hints.add("提示" + (i+1) + "：" + riddle.hints.get(i));
        }
        adapter.notifyDataSetChanged();
    }
}
