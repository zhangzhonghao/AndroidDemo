package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class RandomQuoteActivity extends AppCompatActivity {
    private TextView tvQuote;
    private Button btnNext;
    private int currentIndex = 0;

    private final String[] quotes = {
        "生活不是等待风暴过去，而是学会在雨中起舞。",
        "成功的秘诀在于坚持不懈地追求自己的梦想。",
        "每一个不曾起舞的日子，都是对生命的辜负。",
        "人生没有白走的路，每一步都算数。",
        "当你觉得为时已晚的时候，恰恰是最早的时候。",
        "世界上只有一种真正的英雄主义，那就是认清生活的真相后依然热爱生活。",
        "不要等待机会，而要创造机会。",
        "成功的路上并不拥挤，因为坚持下来的人并不多。",
        "你若盛开，清风自来。",
        "与其抱怨生活，不如改变自己。",
        "人生最重要的不是所处的位置，而是所朝的方向。",
        "只要功夫深，铁杵也能磨成针。",
        "路漫漫其修远兮，吾将上下而求索。",
        "天生我材必有用，千金散尽还复来。",
        "长风破浪会有时，直挂云帆济沧海。"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_quote);

        tvQuote = findViewById(R.id.tv_quote);
        btnNext = findViewById(R.id.btn_next);

        tvQuote.setText(quotes[currentIndex]);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = (currentIndex + 1) % quotes.length;
                tvQuote.setText(quotes[currentIndex]);
            }
        });
    }
}