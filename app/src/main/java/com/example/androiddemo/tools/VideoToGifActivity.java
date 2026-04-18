package com.example.androiddemo.tools;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoToGifActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;

    private ImageView ivVideoPreview;
    private ImageView ivGifPreview;
    private ImageView ivPlayIcon;
    private TextView tvVideoInfo;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvFpsValue;
    private TextView tvQualityValue;
    private TextView tvGifEstimate;
    private TextView tvProgressText;
    private SeekBar seekbarProgress;
    private SeekBar seekbarStartTime;
    private SeekBar seekbarEndTime;
    private SeekBar seekbarFps;
    private SeekBar seekbarQuality;
    private RadioGroup rgGifSize;
    private RadioButton rbSizeOriginal;
    private Button btnSelectVideo;
    private Button btnRecordVideo;
    private Button btnPlayPause;
    private Button btnPreviewGif;
    private Button btnConvert;
    private ProgressBar progressBar;

    private Uri videoUri;
    private String videoPath;
    private long videoDurationMs;
    private int videoWidth;
    private int videoHeight;
    private int rotation;

    private int selectedFps = 10;
    private float selectedScale = 1.0f;
    private int selectedQuality = 10;

    private boolean isPlaying = false;
    private Handler handler;
    private Runnable updateRunnable;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<Uri> takeVideoLauncher;

    private AtomicBoolean isConverting = new AtomicBoolean(false);
    private List<Bitmap> gifFrames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_to_gif);

        handler = new Handler(Looper.getMainLooper());
        initActivityResultLaunchers();
        initViews();
        setupListeners();
    }

    private void initActivityResultLaunchers() {
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        videoUri = uri;
                        loadVideo(uri);
                    }
                });

        takeVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.CaptureVideo(),
                success -> {
                    if (success && videoUri != null) {
                        loadVideo(videoUri);
                    }
                });
    }

    private void initViews() {
        ivVideoPreview = findViewById(R.id.iv_video_preview);
        ivGifPreview = findViewById(R.id.iv_gif_preview);
        ivPlayIcon = findViewById(R.id.iv_play_icon);
        tvVideoInfo = findViewById(R.id.tv_video_info);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvFpsValue = findViewById(R.id.tv_fps_value);
        tvQualityValue = findViewById(R.id.tv_quality_value);
        tvGifEstimate = findViewById(R.id.tv_gif_estimate);
        tvProgressText = findViewById(R.id.tv_progress_text);
        seekbarProgress = findViewById(R.id.seekbar_progress);
        seekbarStartTime = findViewById(R.id.seekbar_start_time);
        seekbarEndTime = findViewById(R.id.seekbar_end_time);
        seekbarFps = findViewById(R.id.seekbar_fps);
        seekbarQuality = findViewById(R.id.seekbar_quality);
        rgGifSize = findViewById(R.id.rg_gif_size);
        rbSizeOriginal = findViewById(R.id.rb_size_original);
        btnSelectVideo = findViewById(R.id.btn_select_video);
        btnRecordVideo = findViewById(R.id.btn_record_video);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnPreviewGif = findViewById(R.id.btn_preview_gif);
        btnConvert = findViewById(R.id.btn_convert);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnSelectVideo.setOnClickListener(v -> {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                    .build());
        });

        btnRecordVideo.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                launchCamera();
            }
        });

        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        seekbarStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int maxProgress = seekbarStartTime.getMax();
                long timeMs = (long) (progress * videoDurationMs / maxProgress);
                tvStartTime.setText(formatTime(timeMs));

                if (seekbarEndTime.getProgress() <= progress) {
                    seekbarEndTime.setProgress(progress);
                    tvEndTime.setText(formatTime(timeMs));
                }
                updateGifEstimate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarEndTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int maxProgress = seekbarEndTime.getMax();
                long timeMs = (long) (progress * videoDurationMs / maxProgress);
                tvEndTime.setText(formatTime(timeMs));

                if (seekbarStartTime.getProgress() >= progress) {
                    seekbarStartTime.setProgress(progress);
                    tvStartTime.setText(formatTime(timeMs));
                }
                updateGifEstimate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
        btnConvert.setOnClickListener(v -> convertToGif());
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
        Uri videoUri = createVideoUri();
        if (videoUri != null) {
            this.videoUri = videoUri;
            takeVideoLauncher.launch(videoUri);
        }
    }

    private Uri createVideoUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "VID_" + timeStamp + ".mp4";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
        }
        return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void loadVideo(Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoDurationMs = durationStr != null ? Long.parseLong(durationStr) : 0;

            String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            videoWidth = widthStr != null ? Integer.parseInt(widthStr) : 0;
            videoHeight = heightStr != null ? Integer.parseInt(heightStr) : 0;

            String rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            rotation = rotationStr != null ? Integer.parseInt(rotationStr) : 0;

            // Get video path for MediaCodec
            videoPath = getPathFromUri(uri);

            // Show first frame as preview
            Bitmap firstFrame = retriever.getFrameAtTime(0);
            if (firstFrame != null) {
                ivVideoPreview.setImageBitmap(firstFrame);
            }

            retriever.release();

            // Update UI
            tvVideoInfo.setText(String.format(Locale.getDefault(),
                    "时长: %s  分辨率: %d x %d",
                    formatTime(videoDurationMs), videoWidth, videoHeight));
            tvTotalTime.setText(formatTime(videoDurationMs));
            tvEndTime.setText(formatTime(videoDurationMs));

            seekbarProgress.setMax((int) videoDurationMs);
            seekbarStartTime.setMax((int) videoDurationMs);
            seekbarEndTime.setMax((int) videoDurationMs);
            seekbarEndTime.setProgress((int) videoDurationMs);

            seekbarProgress.setEnabled(true);
            seekbarStartTime.setEnabled(true);
            seekbarEndTime.setEnabled(true);
            btnPlayPause.setEnabled(true);
            btnPreviewGif.setEnabled(true);
            btnConvert.setEnabled(true);

            ivPlayIcon.setVisibility(View.VISIBLE);
            updateGifEstimate();

            // Setup preview update
            setupPreviewUpdate(uri);

        } catch (Exception e) {
            Toast.makeText(this, "加载视频失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        try {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                if (index >= 0) {
                    path = cursor.getString(index);
                }
                cursor.close();
            }
        } catch (Exception e) {
            // Fallback
        }
        if (path == null) {
            path = uri.getPath();
        }
        return path;
    }

    private void setupPreviewUpdate(Uri uri) {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }

        updateRunnable = new Runnable() {
            private long currentPosition = 0;

            @Override
            public void run() {
                if (isPlaying && videoUri != null) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    try {
                        retriever.setDataSource(VideoToGifActivity.this, uri);
                        long timeUs = currentPosition * 1000;
                        Bitmap frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST);
                        if (frame != null) {
                            ivVideoPreview.setImageBitmap(frame);
                        }

                        seekbarProgress.setProgress((int) currentPosition);
                        tvCurrentTime.setText(formatTime(currentPosition));

                        currentPosition += 100;
                        if (currentPosition >= videoDurationMs) {
                            currentPosition = 0;
                        }
                    } catch (Exception e) {
                        // Ignore
                    } finally {
                        try { retriever.release(); } catch (Exception ignored) {}
                    }
                    handler.postDelayed(this, 100);
                }
            }
        };
    }

    private void togglePlayPause() {
        isPlaying = !isPlaying;
        btnPlayPause.setText(isPlaying ? "⏸" : "▶");

        if (isPlaying) {
            ivPlayIcon.setVisibility(View.GONE);
            handler.post(updateRunnable);
        } else {
            handler.removeCallbacks(updateRunnable);
            ivPlayIcon.setVisibility(View.VISIBLE);
        }
    }

    private String formatTime(long timeMs) {
        long seconds = (timeMs / 1000) % 60;
        long minutes = (timeMs / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void updateGifEstimate() {
        if (videoDurationMs <= 0) {
            tvGifEstimate.setText("预估大小: --  帧数: --");
            return;
        }

        long startMs = (long) (seekbarStartTime.getProgress());
        long endMs = (long) (seekbarEndTime.getProgress());
        long durationMs = endMs - startMs;

        int frameCount = (int) (durationMs * selectedFps / 1000);
        int outputWidth = (int) (videoWidth * selectedScale);
        int outputHeight = (int) (videoHeight * selectedScale);

        // Rough estimate: each frame ~10-50KB depending on quality
        int bytesPerFrame = 10000 + (20 - selectedQuality) * 2000;
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
        if (videoUri == null) {
            Toast.makeText(this, "请先选择视频", Toast.LENGTH_SHORT).show();
            return;
        }

        long startMs = seekbarStartTime.getProgress();
        long endMs = seekbarEndTime.getProgress();

        if (endMs <= startMs) {
            Toast.makeText(this, "结束时间必须大于起始时间", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPreviewGif.setEnabled(false);
        btnConvert.setEnabled(false);

        new Thread(() -> {
            try {
                gifFrames = extractFrames(videoUri, startMs, endMs, selectedFps);

                if (gifFrames.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "无法提取帧", Toast.LENGTH_SHORT).show();
                        btnPreviewGif.setEnabled(true);
                        btnConvert.setEnabled(true);
                    });
                    return;
                }

                // Show first frame as preview
                Bitmap previewFrame = gifFrames.get(0);
                runOnUiThread(() -> {
                    ivGifPreview.setImageBitmap(previewFrame);
                    btnPreviewGif.setEnabled(true);
                    btnConvert.setEnabled(true);
                    Toast.makeText(this, "预览生成完成", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "预览失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPreviewGif.setEnabled(true);
                    btnConvert.setEnabled(true);
                });
            }
        }).start();
    }

    private void convertToGif() {
        if (videoUri == null) {
            Toast.makeText(this, "请先选择视频", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkStoragePermission()) {
            return;
        }

        if (isConverting.get()) {
            Toast.makeText(this, "正在转换中...", Toast.LENGTH_SHORT).show();
            return;
        }

        long startMs = seekbarStartTime.getProgress();
        long endMs = seekbarEndTime.getProgress();

        if (endMs <= startMs) {
            Toast.makeText(this, "结束时间必须大于起始时间", Toast.LENGTH_SHORT).show();
            return;
        }

        isConverting.set(true);
        btnPreviewGif.setEnabled(false);
        btnConvert.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvProgressText.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        new Thread(() -> {
            try {
                // Extract frames
                runOnUiThread(() -> tvProgressText.setText("正在提取视频帧..."));

                gifFrames = extractFrames(videoUri, startMs, endMs, selectedFps);

                if (gifFrames.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "无法提取帧", Toast.LENGTH_SHORT).show();
                        resetConvertState();
                    });
                    return;
                }

                // Encode GIF
                runOnUiThread(() -> tvProgressText.setText("正在编码GIF..."));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                encoder.start(baos);
                encoder.setDelay(1000 / selectedFps);
                encoder.setQuality(selectedQuality);

                int outputWidth = (int) (videoWidth * selectedScale);
                int outputHeight = (int) (videoHeight * selectedScale);

                int frameIndex = 0;
                for (Bitmap frame : gifFrames) {
                    Bitmap scaledFrame = Bitmap.createScaledBitmap(frame, outputWidth, outputHeight, true);
                    encoder.addFrame(scaledFrame);

                    final int progress = (int) ((frameIndex + 1) * 100.0 / gifFrames.size());
                    final int finalFrameIndex = frameIndex;
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        tvProgressText.setText(String.format(Locale.getDefault(),
                                "正在编码... %d/%d 帧", finalFrameIndex + 1, gifFrames.size()));
                    });

                    if (scaledFrame != frame) {
                        scaledFrame.recycle();
                    }
                    frameIndex++;
                }

                encoder.finish();
                byte[] gifData = baos.toByteArray();

                // Save to gallery
                runOnUiThread(() -> tvProgressText.setText("正在保存到相册..."));

                String fileName = "VID_TO_GIF_" + System.currentTimeMillis() + ".gif";
                saveGifToGallery(gifData, fileName);

                runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    tvProgressText.setText("转换完成！");
                    Toast.makeText(this, "GIF已保存到相册", Toast.LENGTH_LONG).show();
                    resetConvertState();
                });

                // Clean up frames
                for (Bitmap frame : gifFrames) {
                    frame.recycle();
                }
                gifFrames.clear();

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "转换失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetConvertState();
                });
            }
        }).start();
    }

    private List<Bitmap> extractFrames(Uri uri, long startMs, long endMs, int fps) {
        List<Bitmap> frames = new ArrayList<>();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);

            long intervalUs = 1000000 / fps;
            long currentUs = startMs * 1000;

            while (currentUs <= endMs * 1000) {
                Bitmap frame = retriever.getFrameAtTime(currentUs, MediaMetadataRetriever.OPTION_CLOSEST);
                if (frame != null) {
                    frames.add(frame);
                }
                currentUs += intervalUs;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { retriever.release(); } catch (Exception ignored) {}
        }

        return frames;
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

    private void resetConvertState() {
        isConverting.set(false);
        btnPreviewGif.setEnabled(true);
        btnConvert.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        tvProgressText.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                convertToGif();
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
        if (isPlaying) {
            isPlaying = false;
            handler.removeCallbacks(updateRunnable);
            btnPlayPause.setText("▶");
            ivPlayIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        if (gifFrames != null) {
            for (Bitmap frame : gifFrames) {
                if (frame != null && !frame.isRecycled()) {
                    frame.recycle();
                }
            }
            gifFrames.clear();
        }
    }

    // Animated GIF Encoder
    private class AnimatedGifEncoder {
        private int width;
        private int height;
        private Color transparent = null;
        private int transIndex;
        private int repeat = -1;
        private int delay = 0;
        private int quality = 10;
        private boolean started = false;
        private ByteArrayOutputStream out;
        private ImageGifEncoder encoder;
        private Bitmap pendingFrame;

        public void setDelay(int ms) {
            delay = Math.round(ms / 10.0f);
        }

        public void setQuality(int quality) {
            this.quality = Math.max(1, quality);
        }

        public void start(OutputStream os) {
            out = new ByteArrayOutputStream();
            started = true;
            encoder = new ImageGifEncoder();
        }

        public boolean addFrame(Bitmap frame) {
            if (!started) return false;

            try {
                if (pendingFrame != null) {
                    pendingFrame.recycle();
                }
                pendingFrame = frame.copy(Bitmap.Config.RGB_565, true);

                if (width <= 0 || height <= 0) {
                    width = frame.getWidth();
                    height = frame.getHeight();
                }

                encoder.encode(out, pendingFrame, width, height, quality, delay);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public byte[] finish() {
            if (!started) return null;
            started = false;
            try {
                out.write(0x3b); // GIF trailer
            } catch (Exception e) {
                // Ignore
            }
            return out.toByteArray();
        }
    }

    // Simple GIF Encoder using LZW compression
    private class ImageGifEncoder {
        public void encode(OutputStream os, Bitmap bitmap, int width, int height, int quality, int delay) throws IOException {
            // Get pixels
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            // Convert to indexed color
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

                // Reduce color palette for smaller file size
                int factor = 256 / colorCount;
                int indexed = (gray / factor) % colorCount;

                indexedPixels[i] = (byte) indexed;

                int key = indexed;
                if (!colorMap.containsKey(key)) {
                    colorMap.put(key, palette.size());
                    palette.add((r << 16) | (g << 8) | b);
                }
            }

            // Write GIF header
            writeHeader(os, width, height, palette);

            // Write Netscape extension for looping
            writeNetscapeExtension(os);

            // Write Graphic Control Extension
            writeGraphicControlExtension(os, delay);

            // Write Image Descriptor
            writeImageDescriptor(os, width, height);

            // Write Local Color Table
            writeColorTable(os, palette);

            // Write LZW minimum code size
            os.write(8); // LZW minimum code size

            // Encode and write image data using LZW
            byte[] lzwData = lzwEncode(indexedPixels, 8);
            writeLZWData(os, lzwData);

            os.write(0x00); // Block terminator
        }

        private void writeHeader(OutputStream os, int width, int height, List<Integer> palette) throws IOException {
            os.write("GIF89a".getBytes());
            // Logical Screen Descriptor
            os.write(width & 0xff);
            os.write((width >> 8) & 0xff);
            os.write(height & 0xff);
            os.write((height >> 8) & 0xff);
            os.write(0x70); // Global Color Table Flag, Color Resolution, Sort Flag, Size of Global Color Table
            os.write(0); // Background Color Index
            os.write(0); // Pixel Aspect Ratio

            // Global Color Table (256 colors max)
            int colorCount = Math.min(256, palette.size());
            for (int i = 0; i < 256; i++) {
                if (i < colorCount) {
                    int color = palette.get(i);
                    os.write((color >> 16) & 0xff);
                    os.write((color >> 8) & 0xff);
                    os.write(color & 0xff);
                } else {
                    os.write(0);
                    os.write(0);
                    os.write(0);
                }
            }
        }

        private void writeNetscapeExtension(OutputStream os) throws IOException {
            os.write(0x21); // Extension Introducer
            os.write(0xff); // Application Extension Label
            os.write(11); // Block Size
            os.write("NETSCAPE2.0".getBytes());
            os.write(3); // Sub-block Size
            os.write(1); // Sub-block ID
            os.write(0); // Loop Count (0 = infinite)
            os.write(0); // Block Terminator
        }

        private void writeGraphicControlExtension(OutputStream os, int delay) throws IOException {
            os.write(0x21); // Extension Introducer
            os.write(0xf9); // Graphic Control Label
            os.write(4); // Block Size
            os.write(0); // Packed Fields
            os.write(delay & 0xff);
            os.write((delay >> 8) & 0xff);
            os.write(0); // Transparent Color Index
            os.write(0); // Block Terminator
        }

        private void writeImageDescriptor(OutputStream os, int width, int height) throws IOException {
            os.write(0x2c); // Image Separator
            os.write(0); // Image Left Position
            os.write(0);
            os.write(0); // Image Top Position
            os.write(0);
            os.write(width & 0xff);
            os.write((width >> 8) & 0xff);
            os.write(height & 0xff);
            os.write((height >> 8) & 0xff);
            os.write(0); // Packed Fields (no local color table)
        }

        private void writeColorTable(OutputStream os, List<Integer> palette) throws IOException {
            // Already written in header
        }

        private byte[] lzwEncode(byte[] data, int minCodeSize) {
            int clearCode = 1 << minCodeSize;
            int endCode = clearCode + 1;
            int codeSize = minCodeSize + 1;
            int nextCode = endCode + 1;

            java.util.Map<String, Integer> dictionary = new java.util.HashMap<>();

            // Initialize dictionary
            for (int i = 0; i < clearCode; i++) {
                dictionary.put(String.valueOf((char) (i & 0xff)), i);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[255];
            int bufferIndex = 0;
            int codeBuffer = 0;
            int codeBits = 0;

            // Write clear code
            codeBuffer |= (clearCode << codeBits);
            codeBits += codeSize;

            int mask = (1 << 16) - 1;

            for (byte b : data) {
                String key = String.valueOf((char) (b & 0xff));
                String nextKey = null;

                for (String dictKey : dictionary.keySet()) {
                    if (dictKey.length() > 0 && dictKey.charAt(dictKey.length() - 1) == (char) (b & 0xff)) {
                        nextKey = dictKey;
                        break;
                    }
                }

                if (nextKey == null) {
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
                        dictionary.put(key + (char) (b & 0xff), nextCode++);
                        if (nextCode > (1 << codeSize) && codeSize < 12) {
                            codeSize++;
                        }
                    }
                }
            }

            // Write end code
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

        private void writeLZWData(OutputStream os, byte[] data) throws IOException {
            os.write(data);
        }
    }
}