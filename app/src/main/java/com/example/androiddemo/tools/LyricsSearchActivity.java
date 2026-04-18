package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class LyricsSearchActivity extends AppCompatActivity {

    private EditText etKeyword;
    private ListView lvResult;
    private List<String> results;
    private ArrayAdapter<String> adapter;

    private final List<LyricsItem> LYRICS_DB = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics_search);

        etKeyword = findViewById(R.id.et_keyword);
        lvResult = findViewById(R.id.rv_results);

        results = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, results);
        lvResult.setAdapter(adapter);

        initLyrics();
    }

    private void initLyrics() {
        LYRICS_DB.add(new LyricsItem("晴天", "周杰伦", "故事的小黄花"));
        LYRICS_DB.add(new LyricsItem("稻香", "周杰伦", "还记得你说家是唯一的城堡"));
        LYRICS_DB.add(new LyricsItem("青花瓷", "周杰伦", "天青色等烟雨而我在等你"));
        LYRICS_DB.add(new LyricsItem("告白气球", "周杰伦", "塞纳河畔 左岸的咖啡"));
        LYRICS_DB.add(new LyricsItem("演员", "薛之谦", "该配合你演出的我演视而不见"));
        LYRICS_DB.add(new LyricsItem("绅士", "薛之谦", "我想摸你的头发"));
        LYRICS_DB.add(new LyricsItem("沙漠骆驼", "展展与罗永", "我要穿越这片沙漠"));
        LYRICS_DB.add(new LyricsItem("学猫叫", "小潘潘", "我们一起学猫叫"));
        LYRICS_DB.add(new LyricsItem("卡路里", "火箭少女", "燃烧我的卡路里"));
        LYRICS_DB.add(new LyricsItem("爱情转移", "陈奕迅", "徘徊过多少橱窗"));
    }

    public void search(View view) {
        String keyword = etKeyword.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(this, "请输入关键词", Toast.LENGTH_SHORT).show();
            return;
        }

        results.clear();
        for (LyricsItem item : LYRICS_DB) {
            if (item.song.contains(keyword) || item.artist.contains(keyword) || item.lyrics.contains(keyword)) {
                results.add(item.song + " - " + item.artist + "\n" + item.lyrics);
            }
        }

        if (results.isEmpty()) {
            results.add("未找到相关歌词");
        }
        adapter.notifyDataSetChanged();
    }

    private static class LyricsItem {
        String song;
        String artist;
        String lyrics;
        LyricsItem(String s, String a, String l) {
            song = s; artist = a; lyrics = l;
        }
    }
}
