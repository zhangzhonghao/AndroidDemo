package com.example.androiddemo.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class SecurityHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_home);
    }

    public void onSecurityItemClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.card_fraud_alert) {
            intent = new Intent(this, FraudAlertActivity.class);
        } else if (id == R.id.card_sensitive_words) {
            intent = new Intent(this, SensitiveWordsActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}