package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game2048Activity extends AppCompatActivity {
    private static final int SIZE = 4;
    private int[][] grid = new int[SIZE][SIZE];
    private GridLayout gridLayout;
    private TextView tvScore;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_2048);

        gridLayout = findViewById(R.id.grid_layout);
        tvScore = findViewById(R.id.tv_score);

        initGame();
        setupTouchListener();
    }

    private void initGame() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = 0;
            }
        }
        score = 0;
        addRandomTile();
        addRandomTile();
        updateUI();
    }

    private void addRandomTile() {
        List<int[]> empty = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) empty.add(new int[]{i, j});
            }
        }
        if (!empty.isEmpty()) {
            int[] pos = empty.get(new Random().nextInt(empty.size()));
            grid[pos[0]][pos[1]] = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private void updateUI() {
        gridLayout.removeAllViews();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                TextView tv = new TextView(this);
                tv.setText(grid[i][j] == 0 ? "" : String.valueOf(grid[i][j]));
                tv.setTextSize(24);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.game_tile_background);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.setMargins(4, 4, 4, 4);
                tv.setLayoutParams(params);
                gridLayout.addView(tv);
            }
        }
        tvScore.setText("分数: " + score);
    }

    private void setupTouchListener() {
        findViewById(R.id.btn_new_game).setOnClickListener(v -> initGame());
        findViewById(R.id.btn_up).setOnClickListener(v -> moveUp());
        findViewById(R.id.btn_down).setOnClickListener(v -> moveDown());
        findViewById(R.id.btn_left).setOnClickListener(v -> moveLeft());
        findViewById(R.id.btn_right).setOnClickListener(v -> moveRight());
    }

    private void moveLeft() {
        for (int i = 0; i < SIZE; i++) {
            int[] row = grid[i];
            int[] newRow = new int[SIZE];
            int pos = 0;
            for (int j = 0; j < SIZE; j++) {
                if (row[j] != 0) {
                    if (pos > 0 && newRow[pos - 1] == row[j]) {
                        newRow[pos - 1] *= 2;
                        score += newRow[pos - 1];
                    } else {
                        newRow[pos++] = row[j];
                    }
                }
            }
            grid[i] = newRow;
        }
        addRandomTile();
        updateUI();
    }

    private void moveRight() {
        for (int i = 0; i < SIZE; i++) {
            int[] row = new int[SIZE];
            for (int j = 0; j < SIZE; j++) row[j] = grid[i][SIZE - 1 - j];
            int[] newRow = new int[SIZE];
            int pos = 0;
            for (int j = 0; j < SIZE; j++) {
                if (row[j] != 0) {
                    if (pos > 0 && newRow[pos - 1] == row[j]) {
                        newRow[pos - 1] *= 2;
                        score += newRow[pos - 1];
                    } else {
                        newRow[pos++] = row[j];
                    }
                }
            }
            for (int j = 0; j < SIZE; j++) grid[i][SIZE - 1 - j] = newRow[j];
        }
        addRandomTile();
        updateUI();
    }

    private void moveUp() {
        int[][] newGrid = new int[SIZE][SIZE];
        for (int j = 0; j < SIZE; j++) {
            int[] col = new int[SIZE];
            for (int i = 0; i < SIZE; i++) col[i] = grid[i][j];
            int[] newCol = new int[SIZE];
            int pos = 0;
            for (int i = 0; i < SIZE; i++) {
                if (col[i] != 0) {
                    if (pos > 0 && newCol[pos - 1] == col[i]) {
                        newCol[pos - 1] *= 2;
                        score += newCol[pos - 1];
                    } else {
                        newCol[pos++] = col[i];
                    }
                }
            }
            for (int i = 0; i < SIZE; i++) newGrid[i][j] = newCol[i];
        }
        grid = newGrid;
        addRandomTile();
        updateUI();
    }

    private void moveDown() {
        int[][] newGrid = new int[SIZE][SIZE];
        for (int j = 0; j < SIZE; j++) {
            int[] col = new int[SIZE];
            for (int i = 0; i < SIZE; i++) col[i] = grid[SIZE - 1 - i][j];
            int[] newCol = new int[SIZE];
            int pos = 0;
            for (int i = 0; i < SIZE; i++) {
                if (col[i] != 0) {
                    if (pos > 0 && newCol[pos - 1] == col[i]) {
                        newCol[pos - 1] *= 2;
                        score += newCol[pos - 1];
                    } else {
                        newCol[pos++] = col[i];
                    }
                }
            }
            for (int i = 0; i < SIZE; i++) newGrid[SIZE - 1 - i][j] = newCol[i];
        }
        grid = newGrid;
        addRandomTile();
        updateUI();
    }
}