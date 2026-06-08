package com.example.androiddemo.tools;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.R;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NgtaActivity extends AppCompatActivity {

    private static final int MODE_READ = 0;
    private static final int MODE_WRITE = 1;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private int mode = MODE_READ;
    private RadioGroup modeGroup;
    private Spinner typeSpinner;
    private LinearLayout formContainer;
    private TextView statusText;
    private TextView resultText;
    private Button writeButton;
    private NdefMessage pendingWriteMessage;
    private final List<EditText> inputs = new ArrayList<>();

    private final RecordTemplate[] templates = new RecordTemplate[]{
            new RecordTemplate("文本", "添加文本记录", RecordType.TEXT, new Field("文本内容", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)),
            new RecordTemplate("URL / URI", "添加 URL 记录", RecordType.URI, new Field("URL 或 URI", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("个性化 URL / URI", "添加带标签的 URI 记录", RecordType.CUSTOM_URI, new Field("显示名称", InputType.TYPE_CLASS_TEXT), new Field("URL 或 URI", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("Unit.Link", "Share everything with one link", RecordType.URI, new Field("Unit.Link 地址", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("搜索", "将链接添加到搜索", RecordType.SEARCH, new Field("搜索关键词", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("社交网络", "添加社交网络链接", RecordType.URI, new Field("社交主页 URL", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("视频", "添加视频链接", RecordType.URI, new Field("视频 URL", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("文件", "将链接添加到文件", RecordType.URI, new Field("文件 URL", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("应用程序", "添加应用程序记录", RecordType.URI, new Field("应用链接或包名", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("邮件", "添加邮件记录", RecordType.EMAIL, new Field("收件人", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS), new Field("主题", InputType.TYPE_CLASS_TEXT), new Field("正文", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)),
            new RecordTemplate("联系", "添加联系人", RecordType.CONTACT, new Field("姓名", InputType.TYPE_CLASS_TEXT), new Field("电话", InputType.TYPE_CLASS_PHONE), new Field("邮箱", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)),
            new RecordTemplate("电话号码", "添加电话号码", RecordType.PHONE, new Field("电话号码", InputType.TYPE_CLASS_PHONE)),
            new RecordTemplate("短信", "添加短信", RecordType.SMS, new Field("手机号", InputType.TYPE_CLASS_PHONE), new Field("短信内容", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)),
            new RecordTemplate("位置", "添加位置", RecordType.GEO, new Field("纬度", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED), new Field("经度", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED)),
            new RecordTemplate("自定义位置", "打开一个自定义的位置", RecordType.URI, new Field("地图位置 URL", InputType.TYPE_TEXT_VARIATION_URI)),
            new RecordTemplate("地址", "添加地址", RecordType.ADDRESS, new Field("地址", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("目标地址", "在地图上启动导航到一个位置", RecordType.NAVIGATION, new Field("目的地地址", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("近邻搜索", "搜索位置附近的兴趣点", RecordType.NEARBY, new Field("关键词", InputType.TYPE_CLASS_TEXT), new Field("位置或坐标", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("街景", "在坐标处打开街景视图", RecordType.STREET_VIEW, new Field("纬度", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED), new Field("经度", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED)),
            new RecordTemplate("紧急", "在紧急情况下的信息", RecordType.TEXT, new Field("紧急信息", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)),
            new RecordTemplate("比特币", "加入比特币地址", RecordType.BITCOIN, new Field("比特币地址", InputType.TYPE_CLASS_TEXT), new Field("金额（可选）", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)),
            new RecordTemplate("蓝牙", "添加蓝牙连接", RecordType.TEXT, new Field("蓝牙名称或 MAC", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("Wi-Fi 网络", "配置 WIFI 网络", RecordType.WIFI, new Field("SSID", InputType.TYPE_CLASS_TEXT), new Field("密码", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD), new Field("加密方式（WPA/WEP/nopass）", InputType.TYPE_CLASS_TEXT)),
            new RecordTemplate("数据", "添加个性话的记录", RecordType.MIME, new Field("MIME 类型", InputType.TYPE_CLASS_TEXT), new Field("数据内容", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE))
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        setupNfcIntent();
        buildUi();
        updateNfcStatus();
        handleNfcIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNfcIntent(intent);
    }

    private void setupNfcIntent() {
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
    }

    private void enableForegroundDispatch() {
        if (nfcAdapter == null) {
            return;
        }
        IntentFilter[] filters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };
        String[][] techLists = new String[][]{
                new String[]{Ndef.class.getName()},
                new String[]{NdefFormatable.class.getName()}
        };
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists);
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        root.setPadding(padding, padding, padding, padding);
        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText("NGTA 卡片");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(resolvePrimaryColor());
        title.setPadding(0, dp(8), 0, dp(12));
        root.addView(title, matchWrap());

        statusText = new TextView(this);
        statusText.setTextSize(14);
        statusText.setPadding(0, 0, 0, dp(12));
        root.addView(statusText, matchWrap());

        modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton read = new RadioButton(this);
        read.setId(View.generateViewId());
        read.setText("读取");
        RadioButton write = new RadioButton(this);
        write.setId(View.generateViewId());
        write.setText("写入");
        modeGroup.addView(read);
        modeGroup.addView(write);
        modeGroup.check(read.getId());
        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mode = checkedId == write.getId() ? MODE_WRITE : MODE_READ;
            updateModeUi();
        });
        root.addView(modeGroup, matchWrap());

        typeSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getTemplateNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderTemplate(templates[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(typeSpinner, matchWrap());

        formContainer = new LinearLayout(this);
        formContainer.setOrientation(LinearLayout.VERTICAL);
        formContainer.setPadding(0, dp(8), 0, 0);
        root.addView(formContainer, matchWrap());

        writeButton = new Button(this);
        writeButton.setText("生成写入内容，贴近卡片写入");
        writeButton.setOnClickListener(v -> prepareWrite());
        root.addView(writeButton, matchWrap());

        resultText = new TextView(this);
        resultText.setTextSize(15);
        resultText.setText("读取模式：把 NGTA/NFC 卡片贴近手机。");
        resultText.setPadding(0, dp(16), 0, dp(24));
        root.addView(resultText, matchWrap());

        setContentView(scrollView);
        updateModeUi();
    }

    private void updateModeUi() {
        boolean writing = mode == MODE_WRITE;
        typeSpinner.setVisibility(writing ? View.VISIBLE : View.GONE);
        formContainer.setVisibility(writing ? View.VISIBLE : View.GONE);
        writeButton.setVisibility(writing ? View.VISIBLE : View.GONE);
        pendingWriteMessage = null;
        resultText.setText(writing ? "先填写模板并点击生成写入内容，然后把卡片贴近手机。" : "读取模式：把 NGTA/NFC 卡片贴近手机。");
    }

    private void updateNfcStatus() {
        if (nfcAdapter == null) {
            statusText.setText("当前设备不支持 NFC，无法读写 NGTA 卡片。");
        } else if (!nfcAdapter.isEnabled()) {
            statusText.setText("NFC 未开启，请先在系统设置中打开 NFC。");
        } else {
            statusText.setText("NFC 已就绪。读取或写入时将卡片贴近手机感应区。");
        }
    }

    private void renderTemplate(RecordTemplate template) {
        if (formContainer == null) {
            return;
        }
        formContainer.removeAllViews();
        inputs.clear();

        TextView subtitle = new TextView(this);
        subtitle.setText(template.description);
        subtitle.setTextSize(14);
        subtitle.setPadding(0, 0, 0, dp(8));
        formContainer.addView(subtitle, matchWrap());

        for (Field field : template.fields) {
            EditText editText = new EditText(this);
            editText.setHint(field.hint);
            editText.setSingleLine((field.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
            editText.setMinLines((field.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0 ? 3 : 1);
            editText.setInputType(field.inputType);
            formContainer.addView(editText, matchWrap());
            inputs.add(editText);
        }
    }

    private void prepareWrite() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            updateNfcStatus();
            Toast.makeText(this, "NFC 不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        RecordTemplate template = templates[typeSpinner.getSelectedItemPosition()];
        String[] values = readInputValues();
        if (!validateRequired(values)) {
            Toast.makeText(this, "请填写模板中的必填内容", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingWriteMessage = buildMessage(template.type, values);
        resultText.setText("已生成「" + template.name + "」写入内容：\n\n" + describeMessage(pendingWriteMessage) + "\n\n现在贴近 NGTA/NFC 卡片即可写入。");
    }

    private void handleNfcIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                && !NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                && !NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            return;
        }
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            Toast.makeText(this, "未识别到卡片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mode == MODE_WRITE && pendingWriteMessage != null) {
            writeTag(tag, pendingWriteMessage);
        } else {
            readTag(intent, tag);
        }
    }

    private void readTag(Intent intent, Tag tag) {
        StringBuilder builder = new StringBuilder();
        builder.append("卡片 ID：").append(bytesToHex(tag.getId())).append("\n\n");

        NdefMessage[] messages = getNdefMessages(intent, tag);
        if (messages.length == 0) {
            builder.append("未读取到 NDEF 内容。");
        } else {
            for (int i = 0; i < messages.length; i++) {
                builder.append("NDEF 消息 ").append(i + 1).append("：\n");
                builder.append(describeMessage(messages[i])).append("\n");
            }
        }
        resultText.setText(builder.toString().trim());
    }

    private void writeTag(Tag tag, NdefMessage message) {
        try {
            int size = message.toByteArray().length;
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    resultText.setText("写入失败：这张卡片不可写。");
                    return;
                }
                if (ndef.getMaxSize() < size) {
                    resultText.setText("写入失败：内容大小 " + size + " B，超过卡片容量 " + ndef.getMaxSize() + " B。");
                    return;
                }
                ndef.writeNdefMessage(message);
                resultText.setText("写入成功：\n\n" + describeMessage(message));
                pendingWriteMessage = null;
                return;
            }

            NdefFormatable formatable = NdefFormatable.get(tag);
            if (formatable != null) {
                formatable.connect();
                formatable.format(message);
                resultText.setText("格式化并写入成功：\n\n" + describeMessage(message));
                pendingWriteMessage = null;
                return;
            }
            resultText.setText("写入失败：卡片不支持 NDEF 写入。");
        } catch (IOException | FormatException e) {
            resultText.setText("写入失败：" + e.getMessage());
        } finally {
            closeQuietly(Ndef.get(tag));
            closeQuietly(NdefFormatable.get(tag));
        }
    }

    private NdefMessage buildMessage(RecordType type, String[] values) {
        switch (type) {
            case TEXT:
                return message(textRecord(values[0]));
            case URI:
                return message(uriRecord(normalizeUri(values[0])));
            case CUSTOM_URI:
                return message(textRecord(values[0]), uriRecord(normalizeUri(values[1])));
            case SEARCH:
                return message(uriRecord("https://www.google.com/search?q=" + Uri.encode(values[0])));
            case EMAIL:
                return message(uriRecord("mailto:" + values[0] + "?subject=" + Uri.encode(valueAt(values, 1)) + "&body=" + Uri.encode(valueAt(values, 2))));
            case CONTACT:
                return message(mimeRecord("text/vcard", buildVcard(values)));
            case PHONE:
                return message(uriRecord("tel:" + values[0]));
            case SMS:
                return message(uriRecord("sms:" + values[0] + "?body=" + Uri.encode(valueAt(values, 1))));
            case GEO:
                return message(uriRecord("geo:" + values[0] + "," + values[1]));
            case ADDRESS:
                return message(uriRecord("geo:0,0?q=" + Uri.encode(values[0])));
            case NAVIGATION:
                return message(uriRecord("google.navigation:q=" + Uri.encode(values[0])));
            case NEARBY:
                return message(uriRecord("geo:0,0?q=" + Uri.encode(values[0] + " " + values[1])));
            case STREET_VIEW:
                return message(uriRecord("google.streetview:cbll=" + values[0] + "," + values[1]));
            case BITCOIN:
                String amount = valueAt(values, 1);
                return message(uriRecord("bitcoin:" + values[0] + (TextUtils.isEmpty(amount) ? "" : "?amount=" + amount)));
            case WIFI:
                String auth = TextUtils.isEmpty(valueAt(values, 2)) ? "WPA" : valueAt(values, 2).toUpperCase(Locale.US);
                return message(textRecord("WIFI:T:" + auth + ";S:" + values[0] + ";P:" + valueAt(values, 1) + ";;"));
            case MIME:
                return message(mimeRecord(values[0], valueAt(values, 1)));
            default:
                return message(textRecord(values[0]));
        }
    }

    private NdefMessage[] getNdefMessages(Intent intent, Tag tag) {
        Object[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null && rawMessages.length > 0) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < rawMessages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            return messages;
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            return new NdefMessage[0];
        }
        try {
            ndef.connect();
            NdefMessage cached = ndef.getNdefMessage();
            return cached == null ? new NdefMessage[0] : new NdefMessage[]{cached};
        } catch (IOException | FormatException e) {
            return new NdefMessage[0];
        } finally {
            closeQuietly(ndef);
        }
    }

    private String describeMessage(NdefMessage message) {
        StringBuilder builder = new StringBuilder();
        NdefRecord[] records = message.getRecords();
        for (int i = 0; i < records.length; i++) {
            NdefRecord record = records[i];
            builder.append("记录 ").append(i + 1).append("：");
            builder.append(describeRecord(record)).append("\n");
        }
        return builder.toString().trim();
    }

    private String describeRecord(NdefRecord record) {
        short tnf = record.getTnf();
        byte[] type = record.getType();
        if (tnf == NdefRecord.TNF_WELL_KNOWN && matches(type, NdefRecord.RTD_TEXT)) {
            return "\n类型：文本\n内容：" + parseTextRecord(record);
        }
        if (tnf == NdefRecord.TNF_WELL_KNOWN && matches(type, NdefRecord.RTD_URI)) {
            return "\n类型：URI\n内容：" + record.toUri();
        }
        if (tnf == NdefRecord.TNF_MIME_MEDIA) {
            return "\n类型：MIME " + new String(type, StandardCharsets.US_ASCII) + "\n内容：" + new String(record.getPayload(), StandardCharsets.UTF_8);
        }
        return "\n类型：TNF " + tnf + " / " + new String(type, StandardCharsets.US_ASCII) + "\n原始数据：" + bytesToHex(record.getPayload());
    }

    private NdefRecord textRecord(String text) {
        byte[] language = Locale.getDefault().getLanguage().getBytes(StandardCharsets.US_ASCII);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[1 + language.length + textBytes.length];
        payload[0] = (byte) language.length;
        System.arraycopy(language, 0, payload, 1, language.length);
        System.arraycopy(textBytes, 0, payload, 1 + language.length, textBytes.length);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    private NdefRecord uriRecord(String uri) {
        return NdefRecord.createUri(uri);
    }

    private NdefRecord mimeRecord(String mimeType, String value) {
        return NdefRecord.createMime(mimeType, value.getBytes(StandardCharsets.UTF_8));
    }

    private NdefMessage message(NdefRecord... records) {
        return new NdefMessage(records);
    }

    private String parseTextRecord(NdefRecord record) {
        byte[] payload = record.getPayload();
        if (payload.length == 0) {
            return "";
        }
        boolean utf16 = (payload[0] & 0x80) != 0;
        int languageLength = payload[0] & 0x3F;
        Charset charset = utf16 ? StandardCharsets.UTF_16 : StandardCharsets.UTF_8;
        return new String(payload, 1 + languageLength, payload.length - 1 - languageLength, charset);
    }

    private String normalizeUri(String value) {
        if (value.startsWith("http://") || value.startsWith("https://") || value.contains(":")) {
            return value;
        }
        return "https://" + value;
    }

    private String buildVcard(String[] values) {
        return "BEGIN:VCARD\nVERSION:3.0\nFN:" + values[0] + "\nTEL:" + valueAt(values, 1) + "\nEMAIL:" + valueAt(values, 2) + "\nEND:VCARD";
    }

    private String[] readInputValues() {
        String[] values = new String[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            values[i] = inputs.get(i).getText().toString().trim();
        }
        return values;
    }

    private boolean validateRequired(String[] values) {
        if (values.length == 0 || TextUtils.isEmpty(values[0])) {
            return false;
        }
        return values.length < 2 || !TextUtils.isEmpty(values[1]) || templates[typeSpinner.getSelectedItemPosition()].type != RecordType.CUSTOM_URI;
    }

    private String[] getTemplateNames() {
        String[] names = new String[templates.length];
        for (int i = 0; i < templates.length; i++) {
            names[i] = templates[i].name;
        }
        return names;
    }

    private String valueAt(String[] values, int index) {
        return index < values.length ? values[index] : "";
    }

    private boolean matches(byte[] left, byte[] right) {
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) {
                return false;
            }
        }
        return true;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    private void closeQuietly(Ndef ndef) {
        if (ndef == null) {
            return;
        }
        try {
            ndef.close();
        } catch (IOException ignored) {
        }
    }

    private void closeQuietly(NdefFormatable formatable) {
        if (formatable == null) {
            return;
        }
        try {
            formatable.close();
        } catch (IOException ignored) {
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int resolvePrimaryColor() {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    private enum RecordType {
        TEXT, URI, CUSTOM_URI, SEARCH, EMAIL, CONTACT, PHONE, SMS, GEO, ADDRESS, NAVIGATION, NEARBY, STREET_VIEW, BITCOIN, WIFI, MIME
    }

    private static class Field {
        final String hint;
        final int inputType;

        Field(String hint, int inputType) {
            this.hint = hint;
            this.inputType = inputType;
        }
    }

    private static class RecordTemplate {
        final String name;
        final String description;
        final RecordType type;
        final Field[] fields;

        RecordTemplate(String name, String description, RecordType type, Field... fields) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.fields = fields;
        }
    }
}
