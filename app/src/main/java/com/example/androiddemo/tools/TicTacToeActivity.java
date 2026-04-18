package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class TicTacToeActivity extends AppCompatActivity {
    private static final int SIZE = 3;
    private int[] board = new int[SIZE * SIZE];
    private int currentPlayer = 1;
    private boolean gameOver = false;
    private GridLayout gridLayout;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        gridLayout = findViewById(R.id.grid_layout);
        tvStatus = findViewById(R.id.tv_status);

        findViewById(R.id.btn_new_game).setOnClickListener(v -> initGame());

        initGame();
    }

    private void initGame() {
        board = new int[SIZE * SIZE];
        currentPlayer = 1;
        gameOver = false;
        tvStatus.setText("玩家X的回合");

        gridLayout.removeAllViews();
        gridLayout.setColumnCount(SIZE);
        gridLayout.setRowCount(SIZE);

        for (int i = 0; i < SIZE * SIZE; i++) {
            Button btn = new Button(this);
            btn.setTag(i);
            btn.setTextSize(40);
            btn.setOnClickListener(v -> onCellClick((int) v.getTag()));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(i / SIZE, 1f);
            params.columnSpec = GridLayout.spec(i % SIZE, 1f);
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);
            gridLayout.addView(btn);
        }
    }

    private void onCellClick(int position) {
        if (gameOver || board[position] != 0) return;

        board[position] = currentPlayer;
        Button btn = (Button) gridLayout.getChildAt(position);
        btn.setText(currentPlayer == 1 ? "X" : "O");
        btn.setTextColor(currentPlayer == 1 ? 0xFFE91E63 : 0xFF2196F3);

        if (checkWin()) {
            gameOver = true;
            String winner = currentPlayer == 1 ? "X" : "O";
            Toast.makeText(this, "玩家" + winner + "获胜!", Toast.LENGTH_LONG).show();
            tvStatus.setText("玩家" + winner + "获胜!");
        } else if (checkDraw()) {
            gameOver = true;
            Toast.makeText(this, "平局!", Toast.LENGTH_LONG).show();
            tvStatus.setText("平局!");
        } else {
            currentPlayer = currentPlayer == 1 ? 2 : 1;
            tvStatus.setText("玩家" + (currentPlayer == 1 ? "X" : "O") + "的回合");
        }
    }

    private boolean checkWin() {
        int[][] lines = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
        };

        for (int[] line : lines) {
            if (board[line[0]] != 0 &&
                board[line[0]] == board[line[1]] &&
                board[line[1]] == board[line[2]]) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDraw() {
        for (int cell : board) {
            if (cell == 0) return false;
        }
        return true;
    }
}