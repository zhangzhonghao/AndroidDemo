package com.example.androiddemo.auth;

import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.concurrent.Executor;

public class BiometricHelper {
    private final Context context;
    private final BiometricManager biometricManager;

    public BiometricHelper(Context context) {
        this.context = context;
        this.biometricManager = BiometricManager.from(context);
    }

    public boolean isBiometricAvailable() {
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public void authenticate(FragmentActivity activity, BiometricAuthCallback callback) {
        Executor executor = ContextCompat.getMainExecutor(context);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Use your fingerprint to login")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        callback.onError(errString.toString());
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        callback.onFailure();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    public interface BiometricAuthCallback {
        void onSuccess();
        void onFailure();
        void onError(String message);
    }
}
