package com.example.androiddemo.tools;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.io.OutputStream;

public class PdfCreatorActivity extends AppCompatActivity {
    private EditText etContent;
    private Button btnCreatePdf;
    private static final int REQUEST_WRITE_STORAGE = 1;

    private final ActivityResultLauncher<String> createDocumentLauncher =
        registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
            uri -> {
                if (uri != null) {
                    createPdf(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_creator);
        initViews();
    }

    private void initViews() {
        etContent = findViewById(R.id.et_content);
        btnCreatePdf = findViewById(R.id.btn_create_pdf);
        btnCreatePdf.setOnClickListener(v -> createDocumentLauncher.launch("document.pdf"));
    }

    private void createPdf(Uri uri) {
        String content = etContent.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);
        canvas.drawText(content, 50, 50, paint);

        document.finishPage(page);

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            document.writeTo(out);
            Toast.makeText(this, "PDF创建成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "PDF创建失败", Toast.LENGTH_SHORT).show();
        }
        document.close();
    }
}