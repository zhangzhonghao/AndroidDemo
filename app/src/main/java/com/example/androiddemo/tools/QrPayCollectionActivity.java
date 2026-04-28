package com.example.androiddemo.tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.androiddemo.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class QrPayCollectionActivity extends AppCompatActivity {
    private ImageView ivAlipay;
    private ImageView ivWechat;
    private ImageView ivUnionpay;

    // 示例收款码数据（实际使用时替换为真实收款码）
    private static final String ALIPAY_URL = "https://qr.alipay.com/fkx123456789";
    private static final String WECHAT_URL = "wxp://f2x123456789";
    private static final String UNIONPAY_URL = "https://wallet.unionpay.com/qr/123456789";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_pay_collection);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        ivAlipay = findViewById(R.id.iv_alipay);
        ivWechat = findViewById(R.id.iv_wechat);
        ivUnionpay = findViewById(R.id.iv_unionpay);
    }

    private void setupClickListeners() {
        ivAlipay.setOnClickListener(v -> showQrCodeDialog("支付宝", ALIPAY_URL, "支付宝收款码"));
        ivWechat.setOnClickListener(v -> showQrCodeDialog("微信", WECHAT_URL, "微信收款码"));
        ivUnionpay.setOnClickListener(v -> showQrCodeDialog("云闪付", UNIONPAY_URL, "云闪付收款码"));
    }

    private void showQrCodeDialog(String name, String content, String title) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setGravity(Gravity.CENTER);

        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Color.BLACK);
        titleView.setGravity(Gravity.CENTER);
        layout.addView(titleView);

        // 二维码图片
        ImageView qrView = new ImageView(this);
        int qrSize = 600;
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(qrSize, qrSize);
        qrParams.gravity = Gravity.CENTER;
        qrParams.setMargins(0, 40, 0, 20);
        qrView.setLayoutParams(qrParams);

        try {
            Bitmap qrBitmap = generateQrCode(content, qrSize);
            qrView.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            qrView.setImageResource(R.drawable.ic_image_placeholder);
        }
        layout.addView(qrView);

        // 说明文字
        TextView descView = new TextView(this);
        descView.setText("请使用" + name + "扫描上方二维码\n完成收款");
        descView.setTextSize(14);
        descView.setTextColor(Color.GRAY);
        descView.setGravity(Gravity.CENTER);
        layout.addView(descView);

        // 提示
        TextView tipView = new TextView(this);
        tipView.setText("\n提示：实际使用时请替换为真实的收款码");
        tipView.setTextSize(12);
        tipView.setTextColor(Color.LTGRAY);
        tipView.setGravity(Gravity.CENTER);
        layout.addView(tipView);

        dialog.setContentView(layout);

        // 设置对话框宽度
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);
        }

        dialog.show();
    }

    private Bitmap generateQrCode(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        int[] pixels = new int[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pixels[y * size + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
        return bitmap;
    }
}