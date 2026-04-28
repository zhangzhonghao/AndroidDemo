package com.example.androiddemo.auth;

import android.content.Context;
import java.util.UUID;

public class AuthManager {
    private static AuthManager instance;
    private final AuthPreferences preferences;

    private String currentUser;
    private String loginMethod;
    private boolean isLoggedIn;

    private AuthManager(Context context) {
        preferences = new AuthPreferences(context);
        isLoggedIn = preferences.isLoggedIn();
        currentUser = preferences.getUserEmail();
        loginMethod = preferences.getLoginMethod();
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    public void login(String email, String password) {
        String token = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        preferences.saveAuthToken(token);
        preferences.saveUserEmail(email);
        preferences.saveUserId(userId);
        preferences.saveLoginMethod("email");

        currentUser = email;
        loginMethod = "email";
        isLoggedIn = true;
    }

    public void loginWithGoogle(String email) {
        String token = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        preferences.saveAuthToken(token);
        preferences.saveUserEmail(email);
        preferences.saveUserId(userId);
        preferences.saveLoginMethod("google");

        currentUser = email;
        loginMethod = "google";
        isLoggedIn = true;
    }

    public void enableBiometric() {
        preferences.setBiometricEnabled(true);
    }

    public boolean isBiometricEnabled() {
        return preferences.isBiometricEnabled();
    }

    public void logout() {
        preferences.clearSession();
        currentUser = null;
        loginMethod = null;
        isLoggedIn = false;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public boolean isAuthenticated() {
        return isLoggedIn && preferences.isLoggedIn();
    }
}
