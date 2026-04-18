package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HtmlEntityActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;

    // HTML实体映射
    private static final Map<Character, String> HTML_ENTITIES = new HashMap<>();
    private static final Map<String, Character> HTML_CODES = new HashMap<>();

    static {
        // 基本实体
        putEntity('"', "&quot;");
        putEntity('\'', "&apos;");
        putEntity('&', "&amp;");
        putEntity('<', "&lt;");
        putEntity('>', "&gt;");
        // 更多实体
        putEntity(' ', "&nbsp;");
        putEntity('¡', "&iexcl;");
        putEntity('¢', "&cent;");
        putEntity('£', "&pound;");
        putEntity('¤', "&curren;");
        putEntity('¥', "&yen;");
        putEntity('¦', "&brvbar;");
        putEntity('§', "&sect;");
        putEntity('¨', "&uml;");
        putEntity('©', "&copy;");
        putEntity('ª', "&ordf;");
        putEntity('«', "&laquo;");
        putEntity('¬', "&not;");
        putEntity('®', "&reg;");
        putEntity('¯', "&macr;");
        putEntity('°', "&deg;");
        putEntity('±', "&plusmn;");
        putEntity('²', "&sup2;");
        putEntity('³', "&sup3;");
        putEntity('´', "&acute;");
        putEntity('µ', "&micro;");
        putEntity('¶', "&para;");
        putEntity('·', "&middot;");
        putEntity('¸', "&cedil;");
        putEntity('¹', "&sup1;");
        putEntity('º', "&ordm;");
        putEntity('»', "&raquo;");
        putEntity('¼', "&frac14;");
        putEntity('½', "&frac12;");
        putEntity('¾', "&frac34;");
        putEntity('¿', "&iquest;");
        putEntity('À', "&Agrave;");
        putEntity('Á', "&Aacute;");
        putEntity('Â', "&Acirc;");
        putEntity('Ã', "&Atilde;");
        putEntity('Ä', "&Auml;");
        putEntity('Å', "&Aring;");
        putEntity('Æ', "&AElig;");
        putEntity('Ç', "&Ccedil;");
        putEntity('È', "&Egrave;");
        putEntity('É', "&Eacute;");
        putEntity('Ê', "&Ecirc;");
        putEntity('Ë', "&Euml;");
        putEntity('Ì', "&Igrave;");
        putEntity('Í', "&Iacute;");
        putEntity('Î', "&Icirc;");
        putEntity('Ï', "&Iuml;");
        putEntity('Ð', "&ETH;");
        putEntity('Ñ', "&Ntilde;");
        putEntity('Ò', "&Ograve;");
        putEntity('Ó', "&Oacute;");
        putEntity('Ô', "&Ocirc;");
        putEntity('Õ', "&Otilde;");
        putEntity('Ö', "&Ouml;");
        putEntity('×', "&times;");
        putEntity('Ø', "&Oslash;");
        putEntity('Ù', "&Ugrave;");
        putEntity('Ú', "&Uacute;");
        putEntity('Û', "&Ucirc;");
        putEntity('Ü', "&Uuml;");
        putEntity('Ý', "&Yacute;");
        putEntity('Þ', "&THORN;");
        putEntity('ß', "&szlig;");
        putEntity('à', "&agrave;");
        putEntity('á', "&aacute;");
        putEntity('â', "&acirc;");
        putEntity('ã', "&atilde;");
        putEntity('ä', "&auml;");
        putEntity('å', "&aring;");
        putEntity('æ', "&aelig;");
        putEntity('ç', "&ccedil;");
        putEntity('è', "&egrave;");
        putEntity('é', "&eacute;");
        putEntity('ê', "&ecirc;");
        putEntity('ë', "&euml;");
        putEntity('ì', "&igrave;");
        putEntity('í', "&iacute;");
        putEntity('î', "&icirc;");
        putEntity('ï', "&iuml;");
        putEntity('ð', "&eth;");
        putEntity('ñ', "&ntilde;");
        putEntity('ò', "&ograve;");
        putEntity('ó', "&oacute;");
        putEntity('ô', "&ocirc;");
        putEntity('õ', "&otilde;");
        putEntity('ö', "&ouml;");
        putEntity('÷', "&divide;");
        putEntity('ø', "&oslash;");
        putEntity('ù', "&ugrave;");
        putEntity('ú', "&uacute;");
        putEntity('û', "&ucirc;");
        putEntity('ü', "&uuml;");
        putEntity('ý', "&yacute;");
        putEntity('þ', "&thorn;");
        putEntity('ÿ', "&yuml;");
    }

    private static void putEntity(char c, String entity) {
        HTML_ENTITIES.put(c, entity);
        HTML_CODES.put(entity, c);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_entity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("HTML实体编码");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        Button btnEncode = findViewById(R.id.btn_encode);
        Button btnDecode = findViewById(R.id.btn_decode);
        Button btnCopy = findViewById(R.id.btn_copy);

        btnEncode.setOnClickListener(v -> encode());
        btnDecode.setOnClickListener(v -> decode());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("HtmlEntity", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void encode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (HTML_ENTITIES.containsKey(c)) {
                result.append(HTML_ENTITIES.get(c));
            } else if (c > 127) {
                result.append("&#").append((int) c).append(";");
            } else {
                result.append(c);
            }
        }
        tvOutput.setText(result.toString());
    }

    private void decode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入HTML实体");
            return;
        }

        String result = input;

        // 解码数字实体
        StringBuilder decoded = new StringBuilder();
        int i = 0;
        while (i < result.length()) {
            if (result.charAt(i) == '&' && i + 1 < result.length() && result.charAt(i + 1) == '#') {
                int j = result.indexOf(";", i);
                if (j > i + 2) {
                    String numStr = result.substring(i + 2, j);
                    try {
                        int code = Integer.parseInt(numStr);
                        decoded.append((char) code);
                        i = j + 1;
                        continue;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
            decoded.append(result.charAt(i));
            i++;
        }
        result = decoded.toString();

        // 解码命名实体
        for (Map.Entry<String, Character> entry : HTML_CODES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue().toString());
        }

        tvOutput.setText(result);
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