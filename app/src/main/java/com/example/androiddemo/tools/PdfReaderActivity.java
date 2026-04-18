package com.example.androiddemo.tools;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.io.File;
import java.io.IOException;

public class PdfReaderActivity extends AppCompatActivity {

    private static final int MIN_PAGE = 0;

    private ImageView ivPdfPage;
    private TextView tvPageInfo;
    private TextView tvEmptyHint;
    private Button btnPrevPage;
    private Button btnNextPage;
    private Button btnOpenFile;

    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private int currentPageIndex = 0;
    private int pageCount = 0;

    private final ActivityResultLauncher<String> openPdfLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    openPdf(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);

        initViews();
        setupListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("PDF阅读器");
        }
    }

    private void initViews() {
        ivPdfPage = findViewById(R.id.iv_pdf_page);
        tvPageInfo = findViewById(R.id.tv_page_info);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        btnPrevPage = findViewById(R.id.btn_prev_page);
        btnNextPage = findViewById(R.id.btn_next_page);
        btnOpenFile = findViewById(R.id.btn_open_file);
    }

    private void setupListeners() {
        btnOpenFile.setOnClickListener(v -> openPdfPicker());

        btnPrevPage.setOnClickListener(v -> showPage(currentPageIndex - 1));

        btnNextPage.setOnClickListener(v -> showPage(currentPageIndex + 1));
    }

    private void openPdfPicker() {
        openPdfLauncher.launch("application/pdf");
    }

    private void openPdf(android.net.Uri uri) {
        try {
            closePdfRenderer();

            // 复制到临时文件
            File tempFile = new File(getCacheDir(), "temp_pdf_" + System.currentTimeMillis() + ".pdf");
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                if (inputStream != null) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }

            parcelFileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            pageCount = pdfRenderer.getPageCount();

            if (pageCount > 0) {
                currentPageIndex = 0;
                showPage(0);
                tvEmptyHint.setVisibility(View.GONE);
                ivPdfPage.setVisibility(View.VISIBLE);
                updatePageInfo();
                updateNavigationButtons();
            } else {
                Toast.makeText(this, "PDF文件为空", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "打开PDF失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPage(int pageIndex) {
        if (pdfRenderer == null || pageCount == 0) {
            return;
        }

        if (pageIndex < MIN_PAGE || pageIndex >= pageCount) {
            return;
        }

        currentPageIndex = pageIndex;

        PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

        // 计算缩放比例以适配屏幕宽度
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float scale = (float) screenWidth / page.getWidth();
        int scaledWidth = (int) (page.getWidth() * scale);
        int scaledHeight = (int) (page.getHeight() * scale);

        Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFFFFFFF); // 白色背景

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        ivPdfPage.setImageBitmap(bitmap);
        updatePageInfo();
        updateNavigationButtons();
    }

    private void updatePageInfo() {
        tvPageInfo.setText("第 " + (currentPageIndex + 1) + " / " + pageCount + " 页");
    }

    private void updateNavigationButtons() {
        btnPrevPage.setEnabled(currentPageIndex > MIN_PAGE);
        btnNextPage.setEnabled(currentPageIndex < pageCount - 1);
    }

    private void closePdfRenderer() {
        if (pdfRenderer != null) {
            pdfRenderer.close();
            pdfRenderer = null;
        }
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException ignored) {
            }
            parcelFileDescriptor = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePdfRenderer();
    }
}