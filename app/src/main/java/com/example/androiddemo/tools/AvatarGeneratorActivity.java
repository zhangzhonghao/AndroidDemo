package com.example.androiddemo.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class AvatarGeneratorActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private Button btnGenerate;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_generator);

        ivAvatar = findViewById(R.id.iv_avatar);
        btnGenerate = findViewById(R.id.btn_generate);

        generateAvatar();

        btnGenerate.setOnClickListener(v -> generateAvatar());
    }

    private void generateAvatar() {
        int size = 400;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 随机背景颜色
        int bgColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        Paint bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        canvas.drawCircle(size / 2, size / 2, size / 2, bgPaint);

        // 绘制简单表情
        Paint facePaint = new Paint();
        facePaint.setColor(Color.WHITE);
        facePaint.setStyle(Paint.Style.FILL);

        Paint eyePaint = new Paint();
        eyePaint.setColor(Color.BLACK);
        eyePaint.setStyle(Paint.Style.FILL);

        // 左眼
        canvas.drawCircle(size / 3, size / 3, 30, facePaint);
        canvas.drawCircle(size / 3, size / 3, 15, eyePaint);

        // 右眼
        canvas.drawCircle(2 * size / 3, size / 3, 30, facePaint);
        canvas.drawCircle(2 * size / 3, size / 3, 15, eyePaint);

        // 嘴巴
        Paint mouthPaint = new Paint();
        mouthPaint.setColor(Color.BLACK);
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(10);
        mouthPaint.setAntiAlias(true);

        // 随机表情
        int expression = random.nextInt(3);
        if (expression == 0) {
            // 微笑
            canvas.drawArc(size / 4, size / 2, 3 * size / 4, 2 * size / 3, 0, 180, false, mouthPaint);
        } else if (expression == 1) {
            // 大笑
            Paint fillPaint = new Paint();
            fillPaint.setColor(Color.WHITE);
            fillPaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(size / 4, size / 2, 3 * size / 4, 2 * size / 3, 0, 180, false, fillPaint);
            canvas.drawArc(size / 4, size / 2, 3 * size / 4, 2 * size / 3, 0, 180, false, mouthPaint);
        } else {
            // 调皮
            canvas.drawLine(size / 4, 2 * size / 3, size / 2, size / 2, mouthPaint);
            canvas.drawLine(size / 2, size / 2, 3 * size / 4, 2 * size / 3, mouthPaint);
        }

        ivAvatar.setImageBitmap(bitmap);
    }
}