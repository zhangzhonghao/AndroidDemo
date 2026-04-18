package com.example.androiddemo.tools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryptActivity extends AppCompatActivity {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int BUFFER_SIZE = 8192;

    private EditText etKey;
    private TextView tvFileName;
    private TextView tvStatus;
    private ProgressBar progressBar;
    private Button btnEncrypt;
    private Button btnDecrypt;

    private Uri selectedFileUri;
    private String selectedFileName;
    private Handler mainHandler;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        selectedFileName = getFileName(selectedFileUri);
                        tvFileName.setText(selectedFileName);
                        tvStatus.setText("已选择: " + selectedFileName);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_encrypt);

        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
    }

    private void initViews() {
        etKey = findViewById(R.id.et_key);
        tvFileName = findViewById(R.id.tv_file_name);
        tvStatus = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.progress_bar);
        btnEncrypt = findViewById(R.id.btn_encrypt);
        btnDecrypt = findViewById(R.id.btn_decrypt);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("文件加密");
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_select_file).setOnClickListener(v -> openFilePicker());

        btnEncrypt.setOnClickListener(v -> {
            if (validateInput()) {
                encryptFile();
            }
        });

        btnDecrypt.setOnClickListener(v -> {
            if (validateInput()) {
                decryptFile();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }

    private String getFileName(Uri uri) {
        String result = "unknown";
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result.equals("unknown")) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private boolean validateInput() {
        String key = etKey.getText().toString();
        if (key.length() < 4) {
            Toast.makeText(this, "密钥至少需要4位", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedFileUri == null) {
            Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void encryptFile() {
        new Thread(() -> {
            try {
                updateStatus("正在加密...");
                showProgress(true);

                String key = etKey.getText().toString();
                File tempFile = copyUriToTempFile(selectedFileUri, selectedFileName);
                File encryptedFile = new File(tempFile.getParent(), selectedFileName + ".enc");

                SecretKey secretKey = deriveKey(key);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                byte[] iv = new byte[GCM_IV_LENGTH];
                new SecureRandom().nextBytes(iv);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

                try (FileInputStream fis = new FileInputStream(tempFile);
                     FileOutputStream fos = new FileOutputStream(encryptedFile);
                     CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                    // 写入IV
                    fos.write(iv);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    long totalBytes = tempFile.length();
                    long processedBytes = 0;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                        processedBytes += bytesRead;
                        updateProgress((int) (processedBytes * 100 / totalBytes));
                    }
                }

                tempFile.delete();

                mainHandler.post(() -> {
                    showProgress(false);
                    updateStatus("加密完成: " + encryptedFile.getName());
                    Toast.makeText(this, "加密成功", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showProgress(false);
                    updateStatus("加密失败: " + e.getMessage());
                    Toast.makeText(this, "加密失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void decryptFile() {
        new Thread(() -> {
            try {
                updateStatus("正在解密...");
                showProgress(true);

                String key = etKey.getText().toString();
                File tempFile = copyUriToTempFile(selectedFileUri, selectedFileName);

                String originalName = selectedFileName;
                if (originalName.endsWith(".enc")) {
                    originalName = originalName.substring(0, originalName.length() - 4);
                }
                File decryptedFile = new File(tempFile.getParent(), originalName + ".dec");

                SecretKey secretKey = deriveKey(key);

                try (FileInputStream fis = new FileInputStream(tempFile)) {
                    // 读取IV
                    byte[] iv = new byte[GCM_IV_LENGTH];
                    int ivRead = fis.read(iv);
                    if (ivRead != GCM_IV_LENGTH) {
                        throw new IOException("无效的加密文件");
                    }

                    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

                    try (FileOutputStream fos = new FileOutputStream(decryptedFile);
                         CipherInputStream cis = new CipherInputStream(fis, cipher)) {

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        long totalBytes = tempFile.length() - GCM_IV_LENGTH;
                        long processedBytes = 0;

                        while ((bytesRead = cis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            processedBytes += bytesRead;
                            updateProgress((int) (processedBytes * 100 / totalBytes));
                        }
                    }
                }

                tempFile.delete();

                mainHandler.post(() -> {
                    showProgress(false);
                    updateStatus("解密完成: " + decryptedFile.getName());
                    Toast.makeText(this, "解密成功", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showProgress(false);
                    updateStatus("解密失败: " + e.getMessage());
                    Toast.makeText(this, "解密失败，请检查密钥是否正确", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private File copyUriToTempFile(Uri uri, String fileName) throws IOException {
        File tempFile = new File(getCacheDir(), fileName);
        try (InputStream is = getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private SecretKey deriveKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private void updateStatus(String status) {
        mainHandler.post(() -> tvStatus.setText(status));
    }

    private void updateProgress(int progress) {
        mainHandler.post(() -> progressBar.setProgress(progress));
    }

    private void showProgress(boolean show) {
        mainHandler.post(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.setProgress(0);
            btnEncrypt.setEnabled(!show);
            btnDecrypt.setEnabled(!show);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}