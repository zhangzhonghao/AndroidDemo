package com.example.androiddemo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.MainActivity;
import com.example.androiddemo.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnBiometric, btnGoogle;
    private AuthManager authManager;
    private BiometricHelper biometricHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthManager.getInstance(this);
        biometricHelper = new BiometricHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBiometric = findViewById(R.id.btnBiometric);
        btnGoogle = findViewById(R.id.btnGoogle);

        if (authManager.isBiometricEnabled() && biometricHelper.isBiometricAvailable()) {
            btnBiometric.setVisibility(android.view.View.VISIBLE);
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnBiometric.setVisibility(android.view.View.GONE);
        btnBiometric.setOnClickListener(v -> attemptBiometricLogin());
        btnGoogle.setOnClickListener(v -> Toast.makeText(this, "Google Sign-In requires configuration", Toast.LENGTH_SHORT).show());

        findViewById(R.id.tvRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        authManager.login(email, password);
        navigateToMain();
    }

    private void attemptBiometricLogin() {
        biometricHelper.authenticate(this, new BiometricHelper.BiometricAuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> navigateToMain());
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Biometric authentication failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
