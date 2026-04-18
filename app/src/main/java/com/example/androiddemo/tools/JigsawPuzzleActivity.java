package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class JigsawPuzzleActivity extends AppCompatActivity {
    private static final int GRID_SIZE = 3;
    private List<Integer> tiles = new ArrayList<>();
    private GridLayout gridLayout;
    private TextView tvMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jigsaw_puzzle);

        gridLayout = findViewById(R.id.grid_layout);
        tvMoves = findViewById(R.id.tv_moves);

        findViewById(R.id.btn_new_game).setOnClickListener(v -> initGame());

        initGame();
    }

    private void initGame() {
        tiles.clear();
        for (int i = 1; i < GRID_SIZE * GRID_SIZE; i++) {
            tiles.add(i);
        }
        tiles.add(0);

        do {
            Collections.shuffle(tiles, new Random());
        } while (!isSolvable());

        updateUI();
    }

    private boolean isSolvable() {
        int inversions = 0;
        for (int i = 0; i < tiles.size() - 1; i++) {
            for (int j = i + 1; j < tiles.size(); j++) {
                if (tiles.get(i) > tiles.get(j) && tiles.get(i) != 0 && tiles.get(j) != 0) {
                    inversions++;
                }
            }
        }
        return inversions % 2 == 0;
    }

    private void updateUI() {
        gridLayout.removeAllViews();
        int cols = GRID_SIZE;
        int rows = GRID_SIZE;

        gridLayout.setColumnCount(cols);
        gridLayout.setRowCount(rows);

        for (int i = 0; i < tiles.size(); i++) {
            final int position = i;
            View tileView = createTileView(tiles.get(i), i);
            tileView.setOnClickListener(v -> onTileClick(position));
            gridLayout.addView(tileView);
        }

        int moves = getMoveCount();
        tvMoves.setText("移动次数: " + moves);
    }

    private View createTileView(int number, int position) {
        android.widget.Button button = new android.widget.Button(this);
        button.setText(number == 0 ? "" : String.valueOf(number));
        button.setTextSize(24);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.rowSpec = GridLayout.spec(position / GRID_SIZE, 1f);
        params.columnSpec = GridLayout.spec(position % GRID_SIZE, 1f);
        params.setMargins(2, 2, 2, 2);
        button.setLayoutParams(params);

        if (number == 0) {
            button.setBackgroundColor(0xFFCCCCCC);
        } else {
            button.setBackgroundColor(0xFF2196F3);
            button.setTextColor(0xFFFFFFFF);
        }

        return button;
    }

    private void onTileClick(int position) {
        int emptyPosition = tiles.indexOf(0);
        if (isAdjacent(position, emptyPosition)) {
            Collections.swap(tiles, position, emptyPosition);
            updateUI();
            checkWin();
        }
    }

    private boolean isAdjacent(int pos1, int pos2) {
        int row1 = pos1 / GRID_SIZE;
        int col1 = pos1 % GRID_SIZE;
        int row2 = pos2 / GRID_SIZE;
        int col2 = pos2 % GRID_SIZE;

        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
               (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    private int getMoveCount() {
        int moves = 0;
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) != 0 && tiles.get(i) != i + 1) {
                moves++;
            }
        }
        return moves / 2;
    }

    private void checkWin() {
        boolean win = true;
        for (int i = 0; i < tiles.size() - 1; i++) {
            if (tiles.get(i) != i + 1) {
                win = false;
                break;
            }
        }
        if (win && tiles.get(tiles.size() - 1) == 0) {
            Toast.makeText(this, "恭喜通关! 移动次数: " + getMoveCount(), Toast.LENGTH_LONG).show();
        }
    }
}