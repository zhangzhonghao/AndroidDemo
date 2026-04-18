package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class GuessNumberActivity extends AppCompatActivity {
    private int targetNumber;
    private int attempts = 0;
    private int minRange = 1;
    private int maxRange = 100;
    private TextView tvHint;
    private TextView tvAttempts;
    private EditText etGuess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_number);

        tvHint = findViewById(R.id.tv_hint);
        tvAttempts = findViewById(R.id.tv_attempts);
        etGuess = findViewById(R.id.et_guess);

        findViewById(R.id.btn_confirm).setOnClickListener(v -> makeGuess());
        findViewById(R.id.btn_new_game).setOnClickListener(v -> newGame());

        newGame();
    }

    private void newGame() {
        Random random = new Random();
        targetNumber = random.nextInt(100) + 1;
        attempts = 0;
        minRange = 1;
        maxRange = 100;

        tvHint.setText("我已经想好了一个" + minRange + "-" + maxRange + "之间的数字");
        tvAttempts.setText("猜测次数: 0");
        etGuess.setText("");
    }

    private void makeGuess() {
        String input = etGuess.getText().toString().trim();
        if (input.isEmpty()) return;

        int guess;
        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            tvHint.setText("请输入有效数字");
            return;
        }

        attempts++;

        if (guess < targetNumber) {
            minRange = Math.max(minRange, guess + 1);
            tvHint.setText("太小了! 范围: " + minRange + "-" + maxRange);
        } else if (guess > targetNumber) {
            maxRange = Math.min(maxRange, guess - 1);
            tvHint.setText("太大了! 范围: " + minRange + "-" + maxRange);
        } else {
            tvHint.setText("恭喜! 正确答案是 " + targetNumber);
        }

        tvAttempts.setText("猜测次数: " + attempts);
        etGuess.setText("");
    }
}