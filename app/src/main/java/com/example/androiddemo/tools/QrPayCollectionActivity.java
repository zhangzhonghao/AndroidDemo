package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class QrPayCollectionActivity extends AppCompatActivity {
    private ImageView ivAlipay;
    private ImageView ivWechat;
    private ImageView ivUnionpay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_pay_collection);
        initViews();
    }

    private void initViews() {
        ivAlipay = findViewById(R.id.iv_alipay);
        ivWechat = findViewById(R.id.iv_wechat);
        ivUnionpay = findViewById(R.id.iv_unionpay);
    }
}