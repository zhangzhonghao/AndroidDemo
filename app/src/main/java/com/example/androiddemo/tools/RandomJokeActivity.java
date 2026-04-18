package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class RandomJokeActivity extends AppCompatActivity {
    private TextView tvJoke;
    private Button btnNext;
    private int currentIndex = 0;

    private final String[] jokes = {
        "为什么程序员总是分不清万圣节和圣诞节？因为 Oct 31 = Dec 25",
        "一个SQL查询走进一家酒吧，看到两张表，喝了一杯",
        "为什么程序员喜欢暗黑模式？因为 light 主题 bug 多",
        "程序员的两大谎言：1. 代码写完就测试 2. 这个需求很简单",
        "程序员的三大错觉：1. 这个 bug 很简单 2. 注释以后再写 3. 这次能上线",
        "为什么程序员不爱去海边？因为那里有太多浪（wave）",
        "一个程序员去算命，算命师说：你会在35岁之前变得有钱。程序员问：然后呢？算命师说：然后你就习惯了",
        "程序员进阶之路：Hello World -> 抄代码 -> 改代码 -> 写代码 -> 掉头发",
        "为什么程序员总是很困？因为他们在 try-catch 中捕获了太多 sleep",
        "程序员的四大浪漫：1. 送你一朵玫瑰（花）2. 送你一棵树（树）3. 送你一个递归 4. 送你一个死循环",
        "代码写得好，bug 就像捉迷藏；代码写得烂，bug 就像跟屁虫",
        "程序员最讨厌的事：1. 写文档 2. 别人不写文档 3. 写注释 4. 别人不写注释",
        "为什么程序员喜欢用 Linux？因为 Windows 总是让他蓝屏（blue screen）",
        "一个程序员的遗言：把我的代码备份三份。一份放在服务器，一份放在云端，还有一份...记得放在月球上",
        "程序员三大错觉：这个功能很简单，这个 bug 很好修，这次能准时上线"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_joke);

        tvJoke = findViewById(R.id.tv_joke);
        btnNext = findViewById(R.id.btn_next);

        tvJoke.setText(jokes[currentIndex]);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = (currentIndex + 1) % jokes.length;
                tvJoke.setText(jokes[currentIndex]);
            }
        });
    }
}