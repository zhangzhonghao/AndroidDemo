package com.example.androiddemo.tools;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.androiddemo.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class QRPayActivity extends AppCompatActivity {

    private ChipGroup chipGroupType;
    private Chip chipAlipay;
    private Chip chipWechat;
    private Chip chipBank;

    private MaterialCardView cardAlipay;
    private MaterialCardView cardWechat;
    private MaterialCardView cardBank;

    private EditText etAlipayCode;
    private EditText etWechatCode;
    private EditText etBankName;
    private EditText etBankCard;
    private EditText etBankName2;

    private ImageView ivQrCode;
    private TextView tvPlaceholder;
    private Button btnSave;
    private Button btnShare;

    private Bitmap currentQrBitmap;

    // 二维码尺寸（像素）
    private static final int QR_SIZE = 512;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_pay);

        initViews();
        setupListeners();
    }

    private void initViews() {
        chipGroupType = findViewById(R.id.chip_group_type);
        chipAlipay = findViewById(R.id.chip_alipay);
        chipWechat = findViewById(R.id.chip_wechat);
        chipBank = findViewById(R.id.chip_bank);

        cardAlipay = findViewById(R.id.card_alipay);
        cardWechat = findViewById(R.id.card_wechat);
        cardBank = findViewById(R.id.card_bank);

        etAlipayCode = findViewById(R.id.et_alipay_code);
        etWechatCode = findViewById(R.id.et_wechat_code);
        etBankName = findViewById(R.id.et_bank_name);
        etBankCard = findViewById(R.id.et_bank_card);
        etBankName2 = findViewById(R.id.et_bank_name2);

        ivQrCode = findViewById(R.id.iv_qr_code);
        tvPlaceholder = findViewById(R.id.tv_placeholder);
        btnSave = findViewById(R.id.btn_save);
        btnShare = findViewById(R.id.btn_share);

        btnSave.setEnabled(false);
        btnShare.setEnabled(false);
    }

    private void setupListeners() {
        chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            updateCardVisibility(checkedId);
        });

        findViewById(R.id.btn_generate).setOnClickListener(v -> generateQRCode());
        btnSave.setOnClickListener(v -> saveQRCode());
        btnShare.setOnClickListener(v -> shareQRCode());
    }

    private void updateCardVisibility(int checkedId) {
        cardAlipay.setVisibility(checkedId == R.id.chip_alipay ? View.VISIBLE : View.GONE);
        cardWechat.setVisibility(checkedId == R.id.chip_wechat ? View.VISIBLE : View.GONE);
        cardBank.setVisibility(checkedId == R.id.chip_bank ? View.VISIBLE : View.GONE);
    }

    private void generateQRCode() {
        int checkedId = chipGroupType.getCheckedChipId();
        String content = "";

        if (checkedId == R.id.chip_alipay) {
            content = etAlipayCode.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入支付宝收款码链接", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (checkedId == R.id.chip_wechat) {
            content = etWechatCode.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入微信收款码链接", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (checkedId == R.id.chip_bank) {
            String name = etBankName.getText().toString().trim();
            String card = etBankCard.getText().toString().trim();
            String bank = etBankName2.getText().toString().trim();

            if (name.isEmpty() || card.isEmpty() || bank.isEmpty()) {
                Toast.makeText(this, "请填写完整的银行卡信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 银行卡信息格式化为 QR 码内容
            content = String.format(
                "姓名:%s\n卡号:%s\n开户行:%s",
                name, card, bank
            );
        }

        try {
            currentQrBitmap = createQRBitmap(content, QR_SIZE);
            ivQrCode.setImageBitmap(currentQrBitmap);
            tvPlaceholder.setVisibility(View.GONE);

            btnSave.setEnabled(true);
            btnShare.setEnabled(true);

            Toast.makeText(this, "收款码生成成功", Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            Toast.makeText(this, "生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap createQRBitmap(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        java.util.Map<EncodeHintType, Object> hints = new java.util.HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    private void saveQRCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "请先生成收款码", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "PAY_QR_" + System.currentTimeMillis() + ".png";

        try {
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCode");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                        Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File qrDir = new File(picturesDir, "QRCode");
                if (!qrDir.exists()) {
                    qrDir.mkdirs();
                }
                File file = new File(qrDir, fileName);
                outputStream = new FileOutputStream(file);
                currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Toast.makeText(this, "已保存: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQRCode() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "请先生成收款码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "qr_pay_share.png";
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享收款码"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}