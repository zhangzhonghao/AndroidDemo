package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class TetrisActivity extends AppCompatActivity {
    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int TILE_SIZE = 30;

    private int[][] board = new int[ROWS][COLS];
    private int[][] currentPiece;
    private int currentX, currentY;
    private int score = 0;
    private boolean isGameOver = false;
    private TetrisView tetrisView;
    private Handler handler = new Handler();
    private Runnable gameLoop;

    private final int[][][] SHAPES = {
        {{1,1,1,1}}, // I
        {{1,1},{1,1}}, // O
        {{0,1,0},{1,1,1}}, // T
        {{1,0,0},{1,1,1}}, // L
        {{0,0,1},{1,1,1}}, // J
        {{0,1,1},{1,1,0}}, // S
        {{1,1,0},{0,1,1}}  // Z
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        tetrisView = new TetrisView(this);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.addView(tetrisView);

        TextView tvScore = findViewById(R.id.tv_score);
        tvScore.setText("分数: 0");

        Button btnNew = findViewById(R.id.btn_new_game);
        btnNew.setOnClickListener(v -> startNewGame());

        Button btnLeft = findViewById(R.id.btn_left);
        Button btnRight = findViewById(R.id.btn_right);
        Button btnDown = findViewById(R.id.btn_down);
        Button btnRotate = findViewById(R.id.btn_rotate);

        btnLeft.setOnClickListener(v -> movePiece(-1));
        btnRight.setOnClickListener(v -> movePiece(1));
        btnDown.setOnClickListener(v -> dropPiece());
        btnRotate.setOnClickListener(v -> rotatePiece());

        startNewGame();
    }

    private void startNewGame() {
        board = new int[ROWS][COLS];
        score = 0;
        isGameOver = false;
        spawnPiece();
        startGameLoop();
    }

    private void spawnPiece() {
        int shapeIndex = (int)(Math.random() * SHAPES.length);
        currentPiece = SHAPES[shapeIndex];
        currentX = COLS / 2 - currentPiece[0].length / 2;
        currentY = 0;
        if (!canPlace(currentPiece, currentX, currentY)) {
            isGameOver = true;
        }
    }

    private boolean canPlace(int[][] piece, int x, int y) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= COLS || newY >= ROWS) return false;
                    if (newY >= 0 && board[newY][newX] != 0) return false;
                }
            }
        }
        return true;
    }

    private void placePiece() {
        for (int i = 0; i < currentPiece.length; i++) {
            for (int j = 0; j < currentPiece[i].length; j++) {
                if (currentPiece[i][j] != 0) {
                    int y = currentY + i;
                    int x = currentX + j;
                    if (y >= 0) board[y][x] = 1;
                }
            }
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        for (int i = ROWS - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int k = i; k > 0; k--) {
                    board[k] = board[k - 1].clone();
                }
                board[0] = new int[COLS];
                score += 10;
                i++;
            }
        }
    }

    private void movePiece(int dx) {
        if (canPlace(currentPiece, currentX + dx, currentY)) {
            currentX += dx;
            tetrisView.invalidate();
        }
    }

    private void dropPiece() {
        if (canPlace(currentPiece, currentX, currentY + 1)) {
            currentY++;
            tetrisView.invalidate();
        } else {
            placePiece();
            tetrisView.invalidate();
        }
    }

    private void rotatePiece() {
        int[][] rotated = new int[currentPiece[0].length][currentPiece.length];
        for (int i = 0; i < currentPiece.length; i++) {
            for (int j = 0; j < currentPiece[i].length; j++) {
                rotated[j][currentPiece.length - 1 - i] = currentPiece[i][j];
            }
        }
        if (canPlace(rotated, currentX, currentY)) {
            currentPiece = rotated;
            tetrisView.invalidate();
        }
    }

    private void startGameLoop() {
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    dropPiece();
                    TextView tvScore = findViewById(R.id.tv_score);
                    tvScore.setText("分数: " + score);
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(gameLoop);
    }

    class TetrisView extends View {
        Paint paint = new Paint();

        public TetrisView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(Color.GRAY);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (board[i][j] != 0) {
                        paint.setColor(Color.BLUE);
                        canvas.drawRect(j * TILE_SIZE, i * TILE_SIZE,
                            (j + 1) * TILE_SIZE, (i + 1) * TILE_SIZE, paint);
                    }
                }
            }

            if (currentPiece != null) {
                paint.setColor(Color.RED);
                for (int i = 0; i < currentPiece.length; i++) {
                    for (int j = 0; j < currentPiece[i].length; j++) {
                        if (currentPiece[i][j] != 0) {
                            int x = (currentX + j) * TILE_SIZE;
                            int y = (currentY + i) * TILE_SIZE;
                            canvas.drawRect(x, y, x + TILE_SIZE, y + TILE_SIZE, paint);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(gameLoop);
    }
}