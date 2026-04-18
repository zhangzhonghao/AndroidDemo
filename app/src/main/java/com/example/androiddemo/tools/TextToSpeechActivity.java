package com.example.androiddemo.tools;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Locale;

public class TextToSpeechActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText etInput;
    private Button btnSpeak, btnStop;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        initViews();
        tts = new TextToSpeech(this, this);
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        btnSpeak = findViewById(R.id.btn_speak);
        btnStop = findViewById(R.id.btn_stop);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("文字转语音");
        }

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "中文语言包不可用，将使用默认语言", Toast.LENGTH_SHORT).show();
                tts.setLanguage(Locale.US);
            }
            isTtsReady = true;
            Toast.makeText(this, "TTS初始化完成", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "TTS初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void speak() {
        if (!isTtsReady) {
            Toast.makeText(this, "TTS正在初始化，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "请输入文字", Toast.LENGTH_SHORT).show();
            return;
        }

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_demo");
    }

    private void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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