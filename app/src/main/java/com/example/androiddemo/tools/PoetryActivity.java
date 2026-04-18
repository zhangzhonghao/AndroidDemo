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

public class PoetryActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvAuthor;
    private ListView lvContent;
    private List<String> lines;
    private ArrayAdapter<String> adapter;
    private int currentIndex = 0;

    private final List<Poem> POEMS = new ArrayList<>();

    private static class Poem {
        String title;
        String author;
        List<String> content;
        Poem(String t, String a, List<String> c) {
            title = t; author = a; content = c;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poetry);

        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        lvContent = findViewById(R.id.lv_content);

        lines = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lines);
        lvContent.setAdapter(adapter);

        initPoems();
        showRandomPoem();
    }

    private void initPoems() {
        POEMS.add(new Poem("静夜思", "李白", List.of(
            "床前明月光，",
            "疑是地上霜。",
            "举头望明月，",
            "低头思故乡。"
        )));
        POEMS.add(new Poem("春晓", "孟浩然", List.of(
            "春眠不觉晓，",
            "处处闻啼鸟。",
            "夜来风雨声，",
            "花落知多少。"
        )));
        POEMS.add(new Poem("登鹳雀楼", "王之涣", List.of(
            "白日依山尽，",
            "黄河入海流。",
            "欲穷千里目，",
            "更上一层楼。"
        )));
        POEMS.add(new Poem("悯农", "李绅", List.of(
            "锄禾日当午，",
            "汗滴禾下土。",
            "谁知盘中餐，",
            "粒粒皆辛苦。"
        )));
        POEMS.add(new Poem("咏鹅", "骆宾王", List.of(
            "鹅鹅鹅，",
            "曲项向天歌。",
            "白毛浮绿水，",
            "红掌拨清波。"
        )));
    }

    private void showRandomPoem() {
        if (POEMS.isEmpty()) return;
        currentIndex = (int) (Math.random() * POEMS.size());
        Poem poem = POEMS.get(currentIndex);
        tvTitle.setText(poem.title);
        tvAuthor.setText("—— " + poem.author);
        lines.clear();
        lines.addAll(poem.content);
        adapter.notifyDataSetChanged();
    }

    public void onNextClick(View view) {
        showRandomPoem();
    }
}