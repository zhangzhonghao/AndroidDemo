package com.example.androiddemo.tools;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddemo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class GifMakerActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;

    private ImageView ivPreview;
    private ImageView ivGifPreview;
    private TextView tvImageCount;
    private TextView tvFpsValue;
    private TextView tvQualityValue;
    private TextView tvDurationValue;
    private TextView tvGifEstimate;
    private TextView tvProgressText;
    private SeekBar seekbarFps;
    private SeekBar seekbarQuality;
    private SeekBar seekbarDuration;
    private RadioGroup rgGifSize;
    private RadioButton rbSizeOriginal;
    private Button btnSelectImages;
    private Button btnCaptureImages;
    private Button btnPreviewGif;
    private Button btnCreateGif;
    private ProgressBar progressBar;
    private RecyclerView rvImages;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<Bitmap> selectedBitmaps = new ArrayList<>();
    private int currentPreviewIndex = 0;
    private Handler handler;
    private Runnable previewRunnable;

    private int selectedFps = 10;
    private float selectedScale = 1.0f;
    private int selectedQuality = 10;
    private int frameDurationMs = 100;

    private int originalWidth = 0;
    private int originalHeight = 0;

    private AtomicBoolean isCreating = new AtomicBoolean(false);

    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_maker);

        handler = new Handler(Looper.getMainLooper());
        initActivityResultLaunchers();
        initViews();
        setupListeners();
    }

    private void initActivityResultLaunchers() {
        pickMultipleMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(10),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImageUris.clear();
                        selectedImageUris.addAll(uris);
                        loadImages();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempImageUri != null) {
                        selectedImageUris.add(tempImageUri);
                        loadImages();
                    }
                });
    }

    private Uri tempImageUri;

    private void initViews() {
        ivPreview = findViewById(R.id.iv_preview);
        ivGifPreview = findViewById(R.id.iv_gif_preview);
        tvImageCount = findViewById(R.id.tv_image_count);
        tvFpsValue = findViewById(R.id.tv_fps_value);
        tvQualityValue = findViewById(R.id.tv_quality_value);
        tvDurationValue = findViewById(R.id.tv_duration_value);
        tvGifEstimate = findViewById(R.id.tv_gif_estimate);
        tvProgressText = findViewById(R.id.tv_progress_text);
        seekbarFps = findViewById(R.id.seekbar_fps);
        seekbarQuality = findViewById(R.id.seekbar_quality);
        seekbarDuration = findViewById(R.id.seekbar_duration);
        rgGifSize = findViewById(R.id.rg_gif_size);
        rbSizeOriginal = findViewById(R.id.rb_size_original);
        btnSelectImages = findViewById(R.id.btn_select_images);
        btnCaptureImages = findViewById(R.id.btn_capture_images);
        btnPreviewGif = findViewById(R.id.btn_preview_gif);
        btnCreateGif = findViewById(R.id.btn_create_gif);
        progressBar = findViewById(R.id.progress_bar);
        rvImages = findViewById(R.id.rv_images);

        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupListeners() {
        btnSelectImages.setOnClickListener(v -> {
            pickMultipleMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnCaptureImages.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                launchCamera();
            }
        });

        seekbarFps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedFps = Math.max(1, progress);
                tvFpsValue.setText(String.valueOf(selectedFps));
                updateGifEstimate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedQuality = progress;
                String[] qualityLabels = {"最低", "低", "中低", "中", "中高", "高", "中高", "高", "很高", "最高", "极高"};
                int index = Math.min(progress, qualityLabels.length - 1);
                tvQualityValue.setText(qualityLabels[index]);
                updateGifEstimate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frameDurationMs = Math.max(50, progress * 10);
                tvDurationValue.setText(frameDurationMs + "ms");
                updateGifEstimate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        rgGifSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_size_original) {
                selectedScale = 1.0f;
            } else if (checkedId == R.id.rb_size_75) {
                selectedScale = 0.75f;
            } else if (checkedId == R.id.rb_size_50) {
                selectedScale = 0.5f;
            } else if (checkedId == R.id.rb_size_25) {
                selectedScale = 0.25f;
            }
            updateGifEstimate();
        });

        btnPreviewGif.setOnClickListener(v -> previewGif());
        btnCreateGif.setOnClickListener(v -> createGif());
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    private void launchCamera() {
        Uri imageUri = createImageUri();
        if (imageUri != null) {
            tempImageUri = imageUri;
            takePictureLauncher.launch(imageUri);
        }
    }

    private Uri createImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void loadImages() {
        selectedBitmaps.clear();

        new Thread(() -> {
            for (Uri uri : selectedImageUris) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    selectedBitmaps.add(bitmap);

                    if (originalWidth == 0 && originalHeight == 0) {
                        originalWidth = bitmap.getWidth();
                        originalHeight = bitmap.getHeight();
                    }
                } catch (Exception e) {
                    // Skip failed images
                }
            }

            runOnUiThread(() -> {
                tvImageCount.setText("共 " + selectedBitmaps.size() + " 张图片");
                updatePreview();
                updateGifEstimate();
                updateImageList();

                if (!selectedBitmaps.isEmpty()) {
                    btnPreviewGif.setEnabled(true);
                    btnCreateGif.setEnabled(true);
                }
            });
        }).start();
    }

    private void updateImageList() {
        if (selectedBitmaps.isEmpty()) {
            rvImages.setVisibility(View.GONE);
            return;
        }

        rvImages.setVisibility(View.VISIBLE);
        ImageListAdapter adapter = new ImageListAdapter(this, selectedBitmaps);
        rvImages.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            currentPreviewIndex = position;
            updatePreview();
        });

        adapter.setOnItemRemoveListener(position -> {
            if (position >= 0 && position < selectedImageUris.size()) {
                selectedImageUris.remove(position);
                Bitmap removed = selectedBitmaps.remove(position);
                if (removed != null && !removed.isRecycled()) {
                    removed.recycle();
                }
                tvImageCount.setText("共 " + selectedBitmaps.size() + " 张图片");
                updatePreview();
                updateGifEstimate();

                if (selectedBitmaps.isEmpty()) {
                    btnPreviewGif.setEnabled(false);
                    btnCreateGif.setEnabled(false);
                }
            }
        });
    }

    private void updatePreview() {
        if (selectedBitmaps.isEmpty()) {
            ivPreview.setImageResource(android.R.color.darker_gray);
            return;
        }

        if (currentPreviewIndex >= selectedBitmaps.size()) {
            currentPreviewIndex = 0;
        }

        ivPreview.setImageBitmap(selectedBitmaps.get(currentPreviewIndex));
    }

    private void updateGifEstimate() {
        if (selectedBitmaps.isEmpty()) {
            tvGifEstimate.setText("预估大小: --  帧数: 0");
            return;
        }

        int frameCount = selectedBitmaps.size();
        int outputWidth = (int) (originalWidth * selectedScale);
        int outputHeight = (int) (originalHeight * selectedScale);

        // Rough estimate
        int bytesPerFrame = 5000 + (20 - selectedQuality) * 1000;
        long estimatedSize = frameCount * bytesPerFrame;

        String sizeStr;
        if (estimatedSize < 1024) {
            sizeStr = estimatedSize + " B";
        } else if (estimatedSize < 1024 * 1024) {
            sizeStr = String.format(Locale.getDefault(), "%.1f KB", estimatedSize / 1024.0);
        } else {
            sizeStr = String.format(Locale.getDefault(), "%.2f MB", estimatedSize / (1024.0 * 1024.0));
        }

        tvGifEstimate.setText(String.format(Locale.getDefault(),
                "预估大小: %s  帧数: %d帧\n输出尺寸: %d x %d",
                sizeStr, frameCount, outputWidth, outputHeight));
    }

    private void previewGif() {
        if (selectedBitmaps.isEmpty()) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPreviewGif.setEnabled(false);
        btnCreateGif.setEnabled(false);

        currentPreviewIndex = 0;
        startPreviewAnimation();
    }

    private void startPreviewAnimation() {
        if (previewRunnable != null) {
            handler.removeCallbacks(previewRunnable);
        }

        previewRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentPreviewIndex < selectedBitmaps.size()) {
                    ivGifPreview.setImageBitmap(selectedBitmaps.get(currentPreviewIndex));
                    currentPreviewIndex++;
                    handler.postDelayed(this, frameDurationMs);
                } else {
                    currentPreviewIndex = 0;
                    // Loop preview
                    handler.postDelayed(this, frameDurationMs);
                }
            }
        };

        handler.post(previewRunnable);
    }

    private void stopPreviewAnimation() {
        if (previewRunnable != null) {
            handler.removeCallbacks(previewRunnable);
            previewRunnable = null;
        }
    }

    private void createGif() {
        if (selectedBitmaps.isEmpty()) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkStoragePermission()) {
            return;
        }

        if (isCreating.get()) {
            Toast.makeText(this, "正在生成中...", Toast.LENGTH_SHORT).show();
            return;
        }

        isCreating.set(true);
        btnPreviewGif.setEnabled(false);
        btnCreateGif.setEnabled(false);
        stopPreviewAnimation();
        progressBar.setVisibility(View.VISIBLE);
        tvProgressText.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        new Thread(() -> {
            try {
                int outputWidth = (int) (originalWidth * selectedScale);
                int outputHeight = (int) (originalHeight * selectedScale);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GifEncoder encoder = new GifEncoder();
                encoder.start(baos);
                encoder.setDelay(frameDurationMs);
                encoder.setQuality(selectedQuality);

                for (int i = 0; i < selectedBitmaps.size(); i++) {
                    Bitmap original = selectedBitmaps.get(i);
                    Bitmap scaled = Bitmap.createScaledBitmap(original, outputWidth, outputHeight, true);
                    encoder.addFrame(scaled);

                    if (scaled != original) {
                        scaled.recycle();
                    }

                    final int progress = (int) ((i + 1) * 100.0 / selectedBitmaps.size());
                    final int finalI = i;
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        tvProgressText.setText(String.format(Locale.getDefault(),
                                "正在生成... %d/%d", finalI + 1, selectedBitmaps.size()));
                    });
                }

                encoder.finish();
                byte[] gifData = baos.toByteArray();

                // Save to gallery
                runOnUiThread(() -> tvProgressText.setText("正在保存到相册..."));

                String fileName = "GIF_MAKER_" + System.currentTimeMillis() + ".gif";
                saveGifToGallery(gifData, fileName);

                runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    tvProgressText.setText("生成完成！");
                    Toast.makeText(this, "GIF已保存到相册", Toast.LENGTH_LONG).show();
                    resetCreateState();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetCreateState();
                });
            }
        }).start();
    }

    private void saveGifToGallery(byte[] gifData, String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/gif");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GIF");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os != null) {
                    os.write(gifData);
                    os.close();
                }
            }
        } else {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File gifDir = new File(picturesDir, "GIF");
            if (!gifDir.exists()) {
                gifDir.mkdirs();
            }
            File gifFile = new File(gifDir, fileName);
            FileOutputStream fos = new FileOutputStream(gifFile);
            fos.write(gifData);
            fos.close();

            // Notify gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(gifFile));
            sendBroadcast(mediaScanIntent);
        }
    }

    private void resetCreateState() {
        isCreating.set(false);
        btnPreviewGif.setEnabled(true);
        btnCreateGif.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        tvProgressText.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createGif();
            } else {
                Toast.makeText(this, "存储权限被拒绝，无法保存GIF", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreviewAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreviewAnimation();
        for (Bitmap bitmap : selectedBitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        selectedBitmaps.clear();
    }

    // GIF Encoder
    private class GifEncoder {
        private int width;
        private int height;
        private int delay = 0;
        private int quality = 10;
        private boolean started = false;
        private ByteArrayOutputStream out;

        public void setDelay(int ms) {
            delay = Math.round(ms / 10.0f);
        }

        public void setQuality(int quality) {
            this.quality = Math.max(1, quality);
        }

        public void start(OutputStream os) {
            out = new ByteArrayOutputStream();
            started = true;
        }

        public boolean addFrame(Bitmap frame) {
            if (!started) return false;

            try {
                int frameWidth = frame.getWidth();
                int frameHeight = frame.getHeight();

                if (width <= 0 || height <= 0) {
                    width = frameWidth;
                    height = frameHeight;
                }

                // Simple GIF encoding
                int[] pixels = new int[width * height];
                Bitmap scaled = Bitmap.createScaledBitmap(frame, width, height, true);
                scaled.getPixels(pixels, 0, width, 0, 0, width, height);

                // Convert to grayscale palette
                byte[] indexedPixels = new byte[width * height];
                int colorCount = 256;
                java.util.Map<Integer, Integer> colorMap = new java.util.HashMap<>();
                List<Integer> palette = new ArrayList<>();

                for (int i = 0; i < pixels.length; i++) {
                    int color = pixels[i];
                    int r = (color >> 16) & 0xff;
                    int g = (color >> 8) & 0xff;
                    int b = color & 0xff;
                    int gray = (r * 30 + g * 59 + b * 11) / 100;

                    int factor = 256 / colorCount;
                    int indexed = (gray / factor) % colorCount;
                    indexedPixels[i] = (byte) indexed;

                    if (!colorMap.containsKey(indexed)) {
                        colorMap.put(indexed, palette.size());
                        palette.add((r << 16) | (g << 8) | b);
                    }
                }

                if (scaled != frame) {
                    scaled.recycle();
                }

                // Write Graphic Control Extension
                out.write(0x21);
                out.write(0xf9);
                out.write(4);
                out.write(0);
                out.write(delay & 0xff);
                out.write((delay >> 8) & 0xff);
                out.write(0);
                out.write(0);

                // Write Image Descriptor
                out.write(0x2c);
                out.write(0);
                out.write(0);
                out.write(0);
                out.write(0);
                out.write(width & 0xff);
                out.write((width >> 8) & 0xff);
                out.write(height & 0xff);
                out.write((height >> 8) & 0xff);
                out.write(0x87);

                // Write Local Color Table
                int paletteSize = Math.min(256, palette.size());
                for (int i = 0; i < 256; i++) {
                    if (i < paletteSize) {
                        int color = palette.get(i);
                        out.write((color >> 16) & 0xff);
                        out.write((color >> 8) & 0xff);
                        out.write(color & 0xff);
                    } else {
                        out.write(0);
                        out.write(0);
                        out.write(0);
                    }
                }

                // Write LZW minimum code size
                out.write(8);

                // LZW encode
                byte[] lzwData = lzwEncode(indexedPixels, 8);
                out.write(lzwData);

                out.write(0x00);

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public byte[] finish() {
            if (!started) return null;
            started = false;
            try {
                out.write(0x3b);
            } catch (Exception e) {
                // Ignore
            }
            return out.toByteArray();
        }

        private byte[] lzwEncode(byte[] data, int minCodeSize) {
            int clearCode = 1 << minCodeSize;
            int endCode = clearCode + 1;
            int codeSize = minCodeSize + 1;
            int nextCode = endCode + 1;

            java.util.Map<String, Integer> dictionary = new java.util.HashMap<>();

            for (int i = 0; i < clearCode; i++) {
                dictionary.put(String.valueOf((char) (i & 0xff)), i);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[255];
            int bufferIndex = 0;
            int codeBuffer = 0;
            int codeBits = 0;

            codeBuffer |= (clearCode << codeBits);
            codeBits += codeSize;

            for (byte b : data) {
                String key = String.valueOf((char) (b & 0xff));
                int code = dictionary.get(key);
                codeBuffer |= (code << codeBits);
                codeBits += codeSize;

                while (codeBits >= 8) {
                    buffer[bufferIndex++] = (byte) (codeBuffer & 0xff);
                    if (bufferIndex == 255) {
                        output.write(255);
                        output.write(buffer, 0, 255);
                        bufferIndex = 0;
                    }
                    codeBuffer >>= 8;
                    codeBits -= 8;
                }

                if (nextCode < 4096) {
                    dictionary.put(key, nextCode++);
                    if (nextCode > (1 << codeSize) && codeSize < 12) {
                        codeSize++;
                    }
                }
            }

            codeBuffer |= (endCode << codeBits);
            codeBits += codeSize;

            while (codeBits >= 8) {
                buffer[bufferIndex++] = (byte) (codeBuffer & 0xff);
                if (bufferIndex == 255) {
                    output.write(255);
                    output.write(buffer, 0, 255);
                    bufferIndex = 0;
                }
                codeBuffer >>= 8;
                codeBits -= 8;
            }

            if (codeBits > 0) {
                buffer[bufferIndex++] = (byte) (codeBuffer & 0xff);
            }

            if (bufferIndex > 0) {
                output.write(bufferIndex);
                output.write(buffer, 0, bufferIndex);
            }

            return output.toByteArray();
        }
    }

    // Image List Adapter
    private class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

        private List<Bitmap> bitmaps;
        private OnItemClickListener listener;
        private OnItemRemoveListener removeListener;

        interface OnItemClickListener {
            void onItemClick(int position);
        }

        interface OnItemRemoveListener {
            void onItemRemove(int position);
        }

        ImageListAdapter(GifMakerActivity context, List<Bitmap> bitmaps) {
            this.bitmaps = bitmaps;
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        void setOnItemRemoveListener(OnItemRemoveListener listener) {
            this.removeListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_thumbnail, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Bitmap bitmap = bitmaps.get(position);
            holder.ivThumb.setImageBitmap(bitmap);
            holder.tvIndex.setText(String.valueOf(position + 1));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(holder.getAdapterPosition());
                }
            });

            holder.btnRemove.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onItemRemove(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return bitmaps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvIndex;
            Button btnRemove;

            ViewHolder(View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.iv_thumb);
                tvIndex = itemView.findViewById(R.id.tv_index);
                btnRemove = itemView.findViewById(R.id.btn_remove);
            }
        }
    }
}