package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MemoryCardActivity extends AppCompatActivity {
    private static final int PAIRS = 8;
    private List<Integer> cards = new ArrayList<>();
    private List<Button> cardButtons = new ArrayList<>();
    private int firstCard = -1;
    private int secondCard = -1;
    private int matchedPairs = 0;
    private int moves = 0;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_card);

        GridLayout gridLayout = findViewById(R.id.grid_layout);
        gridLayout.setColumnCount(4);
        gridLayout.setRowCount(4);

        findViewById(R.id.btn_new_game).setOnClickListener(v -> initGame());

        initGame();
    }

    private void initGame() {
        cards.clear();
        cardButtons.clear();
        matchedPairs = 0;
        moves = 0;
        firstCard = -1;
        secondCard = -1;
        isProcessing = false;

        for (int i = 0; i < PAIRS; i++) {
            cards.add(i);
            cards.add(i);
        }
        Collections.shuffle(cards, new Random());

        GridLayout gridLayout = findViewById(R.id.grid_layout);
        gridLayout.removeAllViews();

        for (int i = 0; i < cards.size(); i++) {
            Button btn = new Button(this);
            btn.setText("?");
            btn.setTextSize(20);
            btn.setTag(i);
            btn.setOnClickListener(v -> onCardClick((Button) v));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(i / 4, 1f);
            params.columnSpec = GridLayout.spec(i % 4, 1f);
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);
            cardButtons.add(btn);
            gridLayout.addView(btn);
        }

        updateUI();
    }

    private void onCardClick(Button btn) {
        if (isProcessing) return;

        int position = (int) btn.getTag();
        if (position == firstCard || cards.get(position) < 0) return;

        btn.setText(String.valueOf(cards.get(position)));

        if (firstCard == -1) {
            firstCard = position;
        } else {
            secondCard = position;
            moves++;
            isProcessing = true;

            btn.postDelayed(() -> checkMatch(), 800);
        }
        updateUI();
    }

    private void checkMatch() {
        if (cards.get(firstCard).equals(cards.get(secondCard))) {
            cards.set(firstCard, -1);
            cards.set(secondCard, -1);
            matchedPairs++;

            if (matchedPairs == PAIRS) {
                Toast.makeText(this, "恭喜通关! 移动次数: " + moves, Toast.LENGTH_LONG).show();
            }
        } else {
            cardButtons.get(firstCard).setText("?");
            cardButtons.get(secondCard).setText("?");
        }

        firstCard = -1;
        secondCard = -1;
        isProcessing = false;
        updateUI();
    }

    private void updateUI() {
        TextView tvMoves = findViewById(R.id.tv_moves);
        tvMoves.setText("移动次数: " + moves);
    }
}