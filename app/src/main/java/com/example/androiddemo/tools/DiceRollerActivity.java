package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class DiceRollerActivity extends AppCompatActivity {
    private static final int DICE_COUNT = 2;
    private TextView[] diceViews = new TextView[DICE_COUNT];
    private TextView tvTotal;
    private int[] diceValues = new int[DICE_COUNT];
    private boolean isRolling = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice_roller);

        GridLayout diceContainer = findViewById(R.id.dice_container);
        tvTotal = findViewById(R.id.tv_total);

        for (int i = 0; i < DICE_COUNT; i++) {
            TextView tvDice = new TextView(this);
            tvDice.setText("?");
            tvDice.setTextSize(60);
            tvDice.setGravity(android.view.Gravity.CENTER);
            tvDice.setWidth(150);
            tvDice.setHeight(150);
            tvDice.setBackgroundColor(0xFFFFFFFF);
            tvDice.setTextColor(0xFF000000);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(0, 1f);
            params.columnSpec = GridLayout.spec(i, 1f);
            params.setMargins(8, 8, 8, 8);
            tvDice.setLayoutParams(params);

            diceViews[i] = tvDice;
            diceContainer.addView(tvDice);
        }

        Button btnRoll = findViewById(R.id.btn_roll);
        btnRoll.setOnClickListener(v -> rollDice());

        updateTotal();
    }

    private void rollDice() {
        if (isRolling) return;
        isRolling = true;

        Button btnRoll = findViewById(R.id.btn_roll);
        btnRoll.setEnabled(false);

        Random random = new Random();
        int rollDuration = 1500;
        int rollInterval = 100;
        int rollCount = rollDuration / rollInterval;

        handler.postDelayed(new Runnable() {
            int currentRoll = 0;

            @Override
            public void run() {
                if (currentRoll < rollCount) {
                    for (int i = 0; i < DICE_COUNT; i++) {
                        diceValues[i] = random.nextInt(6) + 1;
                        diceViews[i].setText(String.valueOf(diceValues[i]));
                    }
                    currentRoll++;
                    handler.postDelayed(this, rollInterval);
                } else {
                    isRolling = false;
                    btnRoll.setEnabled(true);
                    updateTotal();
                }
            }
        }, rollInterval);
    }

    private void updateTotal() {
        int total = 0;
        for (int value : diceValues) {
            total += value;
        }
        tvTotal.setText("总计: " + total);
    }
}