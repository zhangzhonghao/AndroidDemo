package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ToxicSoupActivity extends AppCompatActivity {
    private TextView tvToxic;
    private Button btnNext;
    private int currentIndex = 0;

    private final String[] toxicQuotes = {
        "比你优秀的人还在努力，那你努力还有什么用？",
        "又一天过去了，今天你过得怎么样？是不是离梦想又远了一点？",
        "咸鱼翻身还是咸鱼，别挣扎了。",
        "有人说你丑，你只是审美有问题，别难过。",
        "不要假装很努力，因为结果不会陪你演戏。",
        "别人比你有钱不可怕，可怕的是比你有钱的人还比你努力。",
        "条条大路通罗马，而有的人就出生在罗马。",
        "丑小鸭能变成白天鹅，不是因为它努力，是因为它爸妈是白天鹅。",
        "别沮丧了，虽然你还不知道自己想要什么，但至少知道自己不想要什么。",
        "上帝是公平的，给了你丑的外表，也会给你低的智商，免得你不协调。",
        "只要坚持下去，你就一定会失败。",
        "失败是成功之母，可惜成功往往是失败之父。",
        "有些人努力了一辈子，就是为了成为一个普通人。",
        "年轻人嘛，现在没钱算什么，以后没钱的日子还多着呢。",
        "只要功夫深，铁杵磨成针，但针还是针，杵还是杵。"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toxic_soup);

        tvToxic = findViewById(R.id.tv_toxic);
        btnNext = findViewById(R.id.btn_next);

        tvToxic.setText(toxicQuotes[currentIndex]);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = (currentIndex + 1) % toxicQuotes.length;
                tvToxic.setText(toxicQuotes[currentIndex]);
            }
        });
    }
}