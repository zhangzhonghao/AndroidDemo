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

public class NumberSliderActivity extends AppCompatActivity {
    private static final int SIZE = 4;
    private List<Integer> numbers = new ArrayList<>();
    private GridLayout gridLayout;
    private TextView tvMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_slider);

        gridLayout = findViewById(R.id.grid_layout);
        tvMoves = findViewById(R.id.tv_moves);

        findViewById(R.id.btn_new_game).setOnClickListener(v -> initGame());

        initGame();
    }

    private void initGame() {
        numbers.clear();
        for (int i = 1; i < SIZE * SIZE; i++) {
            numbers.add(i);
        }
        numbers.add(0);

        do {
            Collections.shuffle(numbers, new Random());
        } while (!isSolvable(numbers));

        updateUI();
    }

    private boolean isSolvable(List<Integer> list) {
        int inversions = 0;
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i) > list.get(j) && list.get(i) != 0 && list.get(j) != 0) {
                    inversions++;
                }
            }
        }
        return inversions % 2 == 0;
    }

    private void updateUI() {
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(SIZE);
        gridLayout.setRowCount(SIZE);

        for (int i = 0; i < numbers.size(); i++) {
            final int position = i;
            Button btn = createTile(numbers.get(i), i);
            btn.setOnClickListener(v -> onTileClick(position));
            gridLayout.addView(btn);
        }

        tvMoves.setText("移动次数: " + getMoveCount());
    }

    private Button createTile(int number, int position) {
        Button btn = new Button(this);
        btn.setText(number == 0 ? "" : String.valueOf(number));
        btn.setTextSize(20);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.rowSpec = GridLayout.spec(position / SIZE, 1f);
        params.columnSpec = GridLayout.spec(position % SIZE, 1f);
        params.setMargins(2, 2, 2, 2);
        btn.setLayoutParams(params);

        if (number == 0) {
            btn.setBackgroundColor(0xFFCCCCCC);
        } else {
            btn.setBackgroundColor(0xFF4CAF50);
            btn.setTextColor(0xFFFFFFFF);
        }

        return btn;
    }

    private void onTileClick(int position) {
        int emptyPosition = numbers.indexOf(0);
        if (isAdjacent(position, emptyPosition)) {
            Collections.swap(numbers, position, emptyPosition);
            updateUI();
            checkWin();
        }
    }

    private boolean isAdjacent(int pos1, int pos2) {
        int row1 = pos1 / SIZE;
        int col1 = pos1 % SIZE;
        int row2 = pos2 / SIZE;
        int col2 = pos2 % SIZE;

        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
               (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    private int getMoveCount() {
        int moves = 0;
        for (int i = 0; i < numbers.size() - 1; i++) {
            if (numbers.get(i) != 0 && numbers.get(i) != i + 1) {
                moves++;
            }
        }
        return moves / 2;
    }

    private void checkWin() {
        boolean win = true;
        for (int i = 0; i < numbers.size() - 1; i++) {
            if (numbers.get(i) != i + 1) {
                win = false;
                break;
            }
        }
        if (win && numbers.get(numbers.size() - 1) == 0) {
            Toast.makeText(this, "恭喜通关! 移动次数: " + getMoveCount(), Toast.LENGTH_LONG).show();
        }
    }
}