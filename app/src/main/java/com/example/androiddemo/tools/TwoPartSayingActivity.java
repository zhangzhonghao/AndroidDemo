package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class TwoPartSayingActivity extends AppCompatActivity {

    private ListView lvSaying;
    private List<String> sayings;
    private ArrayAdapter<String> adapter;
    private int currentIndex = 0;

    private final List<String[]> TWO_PART_SAYINGS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idiom_chain);

        lvSaying = findViewById(R.id.rv_history);

        sayings = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sayings);
        lvSaying.setAdapter(adapter);

        initSayings();
        showRandomSaying();
    }

    private void initSayings() {
        TWO_PART_SAYINGS.add(new String[]{"打破砂锅", "纹（问）到底"});
        TWO_PART_SAYINGS.add(new String[]{"小葱拌豆腐", "一清二白"});
        TWO_PART_SAYINGS.add(new String[]{"骑驴看唱本", "走着瞧"});
        TWO_PART_SAYINGS.add(new String[]{"肉包子打狗", "有去无回"});
        TWO_PART_SAYINGS.add(new String[]{"芝麻开花", "节节高"});
        TWO_PART_SAYINGS.add(new String[]{"老鼠钻风箱", "两头受气"});
        TWO_PART_SAYINGS.add(new String[]{"竹篮打水", "一场空"});
        TWO_PART_SAYINGS.add(new String[]{"十五个吊桶打水", "七上八下"});
        TWO_PART_SAYINGS.add(new String[]{"猪八戒照镜子", "里外不是人"});
        TWO_PART_SAYINGS.add(new String[]{"千里送鹅毛", "礼轻情意重"});
    }

    private void showRandomSaying() {
        if (TWO_PART_SAYINGS.isEmpty()) return;
        currentIndex = (int) (Math.random() * TWO_PART_SAYINGS.size());
        sayings.clear();
        String[] saying = TWO_PART_SAYINGS.get(currentIndex);
        sayings.add("上句：" + saying[0]);
        sayings.add("下句：" + saying[1]);
        adapter.notifyDataSetChanged();
    }

    public void onNextClick(View view) {
        showRandomSaying();
    }

    public void onShowAnswerClick(View view) {
        if (TWO_PART_SAYINGS.isEmpty()) return;
        String[] saying = TWO_PART_SAYINGS.get(currentIndex);
        sayings.clear();
        sayings.add("歇后语：" + saying[0] + "，" + saying[1]);
        adapter.notifyDataSetChanged();
    }
}
