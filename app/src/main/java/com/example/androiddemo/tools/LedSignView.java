package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LedSignView extends View {

    private Paint ledOnPaint;
    private Paint ledOffPaint;
    private Paint backgroundPaint;

    private int dotColor = Color.RED;
    private int dotSize = 8;
    private int dotSpacing = 4;
    private int cols = 60;
    private int rows = 10;

    private boolean isScrolling = false;
    private String displayText = "LED字幕";
    private int scrollOffset = 0;
    private int scrollSpeed = 50;

    private Handler handler;
    private Runnable scrollRunnable;

    private List<boolean[][]> textBitmap = new ArrayList<>();

    public LedSignView(Context context) {
        super(context);
        init();
    }

    public LedSignView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LedSignView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        handler = new Handler(Looper.getMainLooper());

        ledOnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ledOnPaint.setColor(dotColor);
        ledOnPaint.setStyle(Paint.Style.FILL);

        ledOffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ledOffPaint.setColor(Color.rgb(40, 40, 40));
        ledOffPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.rgb(20, 20, 20));
        backgroundPaint.setStyle(Paint.Style.FILL);

        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isScrolling) {
                    scrollOffset++;
                    if (scrollOffset >= textBitmap.size()) {
                        scrollOffset = 0;
                    }
                    invalidate();
                    handler.postDelayed(this, scrollSpeed);
                }
            }
        };

        generateTextBitmap();
    }

    public void setDotColor(int color) {
        this.dotColor = color;
        ledOnPaint.setColor(color);
        invalidate();
    }

    public void setScrollSpeed(int speed) {
        this.scrollSpeed = 101 - speed;
    }

    public void setText(String text) {
        this.displayText = text;
        generateTextBitmap();
        scrollOffset = 0;
        invalidate();
    }

    public void startScrolling() {
        isScrolling = true;
        handler.post(scrollRunnable);
    }

    public void stopScrolling() {
        isScrolling = false;
        handler.removeCallbacks(scrollRunnable);
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    private void generateTextBitmap() {
        textBitmap.clear();
        if (displayText == null || displayText.isEmpty()) {
            displayText = "LED";
        }

        int textLen = displayText.length();

        for (int frame = 0; frame < textLen * 8 + cols; frame++) {
            boolean[][] bitmap = new boolean[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    bitmap[i][j] = false;
                }
            }

            for (int charIdx = 0; charIdx < textLen; charIdx++) {
                char c = displayText.charAt(charIdx);
                boolean[][] charBitmap = getCharBitmap(c);

                int startCol = charIdx * 8 - frame + cols;
                if (startCol >= cols || startCol < -8) continue;

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < 8; j++) {
                        int col = startCol + j;
                        if (col >= 0 && col < cols && i < rows) {
                            if (charBitmap[i][j]) {
                                bitmap[i][col] = true;
                            }
                        }
                    }
                }
            }

            textBitmap.add(bitmap);
        }
    }

    private boolean[][] getCharBitmap(char c) {
        boolean[][] bitmap = new boolean[rows][8];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 8; j++) {
                bitmap[i][j] = false;
            }
        }

        switch (c) {
            case 'A':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                for (int i = 1; i < 9; i++) bitmap[i][7] = true;
                for (int j = 1; j < 7; j++) bitmap[5][j] = true;
                break;
            case 'B':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][6] = true;
                for (int i = 5; i < 9; i++) bitmap[i][6] = true;
                break;
            case 'C':
                for (int j = 1; j < 8; j++) bitmap[0][j] = true;
                for (int j = 1; j < 8; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                break;
            case 'D':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][7] = true;
                break;
            case 'E':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int j = 0; j < 8; j++) bitmap[4][j] = true;
                for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                break;
            case 'F':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int j = 0; j < 8; j++) bitmap[4][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                break;
            case 'G':
                for (int j = 1; j < 8; j++) bitmap[0][j] = true;
                for (int j = 1; j < 8; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                bitmap[5][4] = true;
                bitmap[5][5] = true;
                bitmap[5][6] = true;
                bitmap[6][6] = true;
                bitmap[7][5] = true;
                bitmap[7][4] = true;
                break;
            case 'H':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int i = 0; i < 10; i++) bitmap[i][7] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                break;
            case 'I':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][3] = true;
                for (int i = 1; i < 9; i++) bitmap[i][4] = true;
                break;
            case 'J':
                for (int j = 1; j < 8; j++) bitmap[0][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][5] = true;
                bitmap[9][0] = true;
                bitmap[9][1] = true;
                bitmap[9][2] = true;
                bitmap[9][3] = true;
                bitmap[9][4] = true;
                break;
            case 'K':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                bitmap[4][1] = true;
                bitmap[3][2] = true;
                bitmap[2][3] = true;
                bitmap[1][4] = true;
                bitmap[0][5] = true;
                bitmap[1][6] = true;
                bitmap[2][7] = true;
                bitmap[3][6] = true;
                bitmap[4][5] = true;
                bitmap[5][4] = true;
                bitmap[6][3] = true;
                bitmap[7][2] = true;
                bitmap[8][1] = true;
                break;
            case 'L':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int j = 1; j < 8; j++) bitmap[9][j] = true;
                break;
            case 'M':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int i = 0; i < 10; i++) bitmap[i][7] = true;
                bitmap[1][1] = true;
                bitmap[2][2] = true;
                bitmap[3][3] = true;
                bitmap[1][6] = true;
                bitmap[2][5] = true;
                bitmap[3][4] = true;
                break;
            case 'N':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int i = 0; i < 10; i++) bitmap[i][7] = true;
                for (int i = 0; i < 10; i++) bitmap[i][i] = true;
                break;
            case 'O':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                for (int i = 1; i < 9; i++) bitmap[i][7] = true;
                break;
            case 'P':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][6] = true;
                break;
            case 'Q':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                for (int i = 1; i < 9; i++) bitmap[i][7] = true;
                bitmap[7][5] = true;
                bitmap[8][6] = true;
                bitmap[9][7] = true;
                break;
            case 'R':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][6] = true;
                bitmap[5][5] = true;
                bitmap[6][4] = true;
                bitmap[7][3] = true;
                bitmap[8][2] = true;
                bitmap[9][1] = true;
                break;
            case 'S':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][0] = true;
                for (int i = 6; i < 9; i++) bitmap[i][7] = true;
                break;
            case 'T':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int i = 1; i < 10; i++) bitmap[i][3] = true;
                for (int i = 1; i < 10; i++) bitmap[i][4] = true;
                break;
            case 'U':
                for (int i = 0; i < 9; i++) bitmap[i][0] = true;
                for (int i = 0; i < 9; i++) bitmap[i][7] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                break;
            case 'V':
                for (int i = 0; i < 8; i++) bitmap[i][0] = true;
                for (int i = 0; i < 8; i++) bitmap[i][7] = true;
                bitmap[8][1] = true;
                bitmap[8][6] = true;
                bitmap[9][2] = true;
                bitmap[9][3] = true;
                bitmap[9][4] = true;
                bitmap[9][5] = true;
                break;
            case 'W':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int i = 0; i < 10; i++) bitmap[i][7] = true;
                bitmap[8][1] = true;
                bitmap[8][6] = true;
                bitmap[7][2] = true;
                bitmap[7][5] = true;
                bitmap[6][3] = true;
                bitmap[6][4] = true;
                break;
            case 'X':
                for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                for (int i = 0; i < 10; i++) bitmap[i][7] = true;
                for (int i = 0; i < 10; i++) bitmap[i][i] = true;
                for (int i = 0; i < 10; i++) bitmap[9 - i][i] = true;
                break;
            case 'Y':
                for (int i = 0; i < 5; i++) bitmap[i][i] = true;
                for (int i = 0; i < 5; i++) bitmap[i][7 - i] = true;
                for (int i = 5; i < 10; i++) bitmap[i][3] = true;
                for (int i = 5; i < 10; i++) bitmap[i][4] = true;
                break;
            case 'Z':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][9 - i] = true;
                break;
            case '0':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                for (int i = 1; i < 9; i++) bitmap[i][7] = true;
                bitmap[1][6] = true;
                bitmap[2][5] = true;
                bitmap[3][4] = true;
                bitmap[6][4] = true;
                bitmap[7][5] = true;
                bitmap[8][6] = true;
                break;
            case '1':
                for (int i = 1; i < 9; i++) bitmap[i][3] = true;
                for (int i = 1; i < 9; i++) bitmap[i][4] = true;
                bitmap[0][3] = true;
                bitmap[0][4] = true;
                bitmap[9][3] = true;
                break;
            case '2':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][7] = true;
                for (int i = 6; i < 9; i++) bitmap[i][0] = true;
                bitmap[5][6] = true;
                bitmap[6][5] = true;
                bitmap[7][4] = true;
                bitmap[8][3] = true;
                break;
            case '3':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][7] = true;
                for (int i = 6; i < 9; i++) bitmap[i][7] = true;
                break;
            case '4':
                for (int i = 0; i < 5; i++) bitmap[i][0] = true;
                for (int i = 0; i < 5; i++) bitmap[i][7] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int i = 4; i < 10; i++) bitmap[i][5] = true;
                for (int i = 4; i < 10; i++) bitmap[i][6] = true;
                break;
            case '5':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int j = 0; j < 8; j++) bitmap[4][j] = true;
                for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                for (int i = 5; i < 9; i++) bitmap[i][7] = true;
                for (int i = 1; i < 4; i++) bitmap[i][0] = true;
                break;
            case '6':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                for (int i = 5; i < 9; i++) bitmap[i][7] = true;
                break;
            case '7':
                for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][5] = true;
                for (int i = 1; i < 9; i++) bitmap[i][6] = true;
                for (int i = 1; i < 5; i++) bitmap[i][4] = true;
                break;
            case '8':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][0] = true;
                for (int i = 1; i < 4; i++) bitmap[i][7] = true;
                for (int i = 6; i < 9; i++) bitmap[i][0] = true;
                for (int i = 6; i < 9; i++) bitmap[i][7] = true;
                break;
            case '9':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 4; i++) bitmap[i][0] = true;
                for (int i = 1; i < 4; i++) bitmap[i][7] = true;
                for (int i = 1; i < 9; i++) bitmap[i][7] = true;
                break;
            case ' ':
                break;
            case '!':
                bitmap[0][2] = true;
                bitmap[1][2] = true;
                bitmap[2][2] = true;
                bitmap[3][2] = true;
                bitmap[4][2] = true;
                bitmap[8][2] = true;
                break;
            case '.':
                bitmap[9][1] = true;
                break;
            case ',':
                bitmap[8][1] = true;
                bitmap[9][0] = true;
                break;
            case '-':
                for (int j = 2; j < 6; j++) bitmap[4][j] = true;
                break;
            case '+':
                for (int j = 2; j < 6; j++) bitmap[4][j] = true;
                for (int i = 2; i < 7; i++) bitmap[i][4] = true;
                break;
            case '*':
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                for (int i = 1; i < 7; i++) bitmap[i][4] = true;
                bitmap[1][1] = true;
                bitmap[2][2] = true;
                bitmap[6][1] = true;
                bitmap[5][2] = true;
                bitmap[1][6] = true;
                bitmap[2][5] = true;
                bitmap[6][6] = true;
                bitmap[5][5] = true;
                break;
            case '/':
                for (int i = 0; i < 10; i++) bitmap[i][9 - i] = true;
                break;
            case ':':
                bitmap[3][3] = true;
                bitmap[3][4] = true;
                bitmap[6][3] = true;
                bitmap[6][4] = true;
                break;
            case '~':
                bitmap[3][2] = true;
                bitmap[3][3] = true;
                bitmap[4][4] = true;
                bitmap[4][5] = true;
                bitmap[5][6] = true;
                bitmap[5][7] = true;
                break;
            case '^':
                bitmap[1][3] = true;
                bitmap[1][4] = true;
                bitmap[2][2] = true;
                bitmap[2][5] = true;
                bitmap[3][1] = true;
                bitmap[3][6] = true;
                break;
            case '#':
                for (int j = 1; j < 7; j++) bitmap[2][j] = true;
                for (int j = 1; j < 7; j++) bitmap[7][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][2] = true;
                for (int i = 1; i < 9; i++) bitmap[i][5] = true;
                break;
            case '@':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[9][j] = true;
                for (int i = 1; i < 9; i++) bitmap[i][0] = true;
                for (int i = 1; i < 5; i++) bitmap[i][7] = true;
                bitmap[6][6] = true;
                bitmap[7][5] = true;
                bitmap[7][4] = true;
                bitmap[7][3] = true;
                bitmap[6][3] = true;
                bitmap[5][3] = true;
                break;
            case '%':
                bitmap[0][0] = true;
                bitmap[1][1] = true;
                bitmap[2][2] = true;
                bitmap[3][3] = true;
                bitmap[4][4] = true;
                bitmap[5][5] = true;
                bitmap[6][6] = true;
                bitmap[7][7] = true;
                bitmap[9][7] = true;
                break;
            case '&':
                for (int j = 2; j < 7; j++) bitmap[0][j] = true;
                bitmap[1][1] = true;
                bitmap[2][0] = true;
                bitmap[2][7] = true;
                bitmap[3][1] = true;
                bitmap[4][2] = true;
                bitmap[5][3] = true;
                bitmap[6][4] = true;
                bitmap[7][5] = true;
                bitmap[8][6] = true;
                bitmap[9][5] = true;
                break;
            case '=':
                for (int j = 0; j < 8; j++) bitmap[2][j] = true;
                for (int j = 0; j < 8; j++) bitmap[7][j] = true;
                break;
            case '(':
                for (int i = 0; i < 10; i++) bitmap[i][2] = true;
                for (int i = 0; i < 10; i++) bitmap[i][5] = true;
                bitmap[0][3] = true;
                bitmap[0][4] = true;
                bitmap[9][3] = true;
                bitmap[9][4] = true;
                break;
            case ')':
                for (int i = 0; i < 10; i++) bitmap[i][2] = true;
                for (int i = 0; i < 10; i++) bitmap[i][5] = true;
                bitmap[0][3] = true;
                bitmap[0][4] = true;
                bitmap[9][3] = true;
                bitmap[9][4] = true;
                break;
            case '<':
                for (int j = 2; j < 6; j++) bitmap[9 - j][j] = true;
                for (int j = 2; j < 6; j++) bitmap[j][j] = true;
                break;
            case '>':
                for (int j = 2; j < 6; j++) bitmap[j][j] = true;
                for (int j = 2; j < 6; j++) bitmap[9 - j][j] = true;
                break;
            case '?':
                for (int j = 1; j < 7; j++) bitmap[0][j] = true;
                for (int j = 1; j < 7; j++) bitmap[4][j] = true;
                bitmap[1][7] = true;
                bitmap[2][7] = true;
                bitmap[5][2] = true;
                bitmap[6][2] = true;
                break;
            case '[':
                for (int i = 0; i < 10; i++) bitmap[i][1] = true;
                for (int i = 0; i < 10; i++) bitmap[i][5] = true;
                bitmap[0][2] = true;
                bitmap[0][3] = true;
                bitmap[0][4] = true;
                bitmap[9][2] = true;
                bitmap[9][3] = true;
                bitmap[9][4] = true;
                break;
            case ']':
                for (int i = 0; i < 10; i++) bitmap[i][1] = true;
                for (int i = 0; i < 10; i++) bitmap[i][5] = true;
                bitmap[0][2] = true;
                bitmap[0][3] = true;
                bitmap[0][4] = true;
                bitmap[9][2] = true;
                bitmap[9][3] = true;
                bitmap[9][4] = true;
                break;
            case '_':
                for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                break;
            case '\\':
                for (int i = 0; i < 10; i++) bitmap[i][i] = true;
                break;
            case '|':
                for (int i = 0; i < 10; i++) bitmap[i][3] = true;
                for (int i = 0; i < 10; i++) bitmap[i][4] = true;
                break;
            case '\u2764':
                bitmap[2][2] = true;
                bitmap[2][3] = true;
                bitmap[2][5] = true;
                bitmap[2][6] = true;
                bitmap[3][1] = true;
                bitmap[3][4] = true;
                bitmap[3][7] = true;
                bitmap[4][0] = true;
                bitmap[4][3] = true;
                bitmap[4][4] = true;
                bitmap[4][8] = true;
                bitmap[5][1] = true;
                bitmap[5][2] = true;
                bitmap[5][5] = true;
                bitmap[5][6] = true;
                bitmap[6][2] = true;
                bitmap[6][3] = true;
                bitmap[6][5] = true;
                bitmap[6][6] = true;
                bitmap[7][3] = true;
                bitmap[7][4] = true;
                bitmap[8][4] = true;
                break;
            default:
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    for (int j = 0; j < 8; j++) bitmap[0][j] = true;
                    for (int j = 0; j < 8; j++) bitmap[9][j] = true;
                    for (int i = 0; i < 10; i++) bitmap[i][0] = true;
                    for (int i = 0; i < 10; i++) bitmap[i][7] = true;
                    for (int j = 2; j < 6; j++) bitmap[4][j] = true;
                }
                break;
        }

        return bitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int availableWidth = w - getPaddingLeft() - getPaddingRight();
        int availableHeight = h - getPaddingTop() - getPaddingBottom();

        int totalDotWidth = dotSize + dotSpacing;
        int totalDotHeight = dotSize + dotSpacing;

        cols = availableWidth / totalDotWidth;
        rows = availableHeight / totalDotHeight;

        if (cols < 10) cols = 10;
        if (rows < 5) rows = 5;

        generateTextBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        boolean[][] bitmap;
        if (textBitmap.isEmpty()) {
            bitmap = new boolean[rows][cols];
        } else {
            bitmap = textBitmap.get(scrollOffset % textBitmap.size());
        }

        float startX = (getWidth() - cols * (dotSize + dotSpacing)) / 2f;
        float startY = (getHeight() - rows * (dotSize + dotSpacing)) / 2f;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float left = startX + j * (dotSize + dotSpacing);
                float top = startY + i * (dotSize + dotSpacing);

                RectF rect = new RectF(left, top, left + dotSize, top + dotSize);

                if (i < bitmap.length && j < bitmap[i].length && bitmap[i][j]) {
                    ledOnPaint.setColor(dotColor);
                    canvas.drawRoundRect(rect, dotSize / 2f, dotSize / 2f, ledOnPaint);
                } else {
                    canvas.drawRoundRect(rect, dotSize / 2f, dotSize / 2f, ledOffPaint);
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopScrolling();
    }
}