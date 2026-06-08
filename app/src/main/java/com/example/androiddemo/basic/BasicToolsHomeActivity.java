package com.example.androiddemo.basic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.FlashlightActivity;
import com.example.androiddemo.tools.VibrationTestActivity;
import com.example.androiddemo.tools.CalculatorActivity;
import com.example.androiddemo.tools.QRScannerActivity;
import com.example.androiddemo.tools.QRGeneratorActivity;
import com.example.androiddemo.tools.NgtaActivity;
import com.example.androiddemo.tools.UnitConverterActivity;
import com.example.androiddemo.tools.AppManagerActivity;
import com.example.androiddemo.tools.ImageCompressorActivity;
import com.example.androiddemo.tools.TunerActivity;
import com.example.androiddemo.tools.MetronomeActivity;
import com.example.androiddemo.tools.ColorPickerActivity;
import com.example.androiddemo.tools.ColorPickerScreenActivity;
import com.example.androiddemo.tools.TouchTestActivity;
import com.example.androiddemo.tools.DeviceInfoActivity;
import com.example.androiddemo.tools.SystemInfoActivity;
import com.example.androiddemo.tools.QuickShortcutActivity;
import com.example.androiddemo.tools.PasswordVaultActivity;
import com.example.androiddemo.tools.PasswordManagerActivity;
import com.example.androiddemo.tools.WallpaperActivity;
import com.example.androiddemo.tools.AppCloneActivity;
import com.example.androiddemo.tools.PowerScheduleActivity;
import com.example.androiddemo.tools.GpsConverterActivity;
import com.example.androiddemo.tools.ItemStorageActivity;
import com.example.androiddemo.tools.BinaryCalculatorActivity;
import com.example.androiddemo.tools.AdvancedBaseConverterActivity;
import com.example.androiddemo.tools.ColorConverterActivity;
import com.example.androiddemo.tools.WordCountActivity;
import com.example.androiddemo.tools.RandomGeneratorActivity;
import com.example.androiddemo.tools.RandomNumberGeneratorActivity;
import com.example.androiddemo.tools.RandomLotteryActivity;
import com.example.androiddemo.tools.DateCalculatorActivity;
import com.example.androiddemo.tools.FamilyTreeActivity;
import com.example.androiddemo.tools.RelativeDetailActivity;
import com.example.androiddemo.tools.LoveMatchActivity;
import com.example.androiddemo.tools.BirthdayCodeActivity;
import com.example.androiddemo.tools.Base64CodecActivity;
import com.example.androiddemo.tools.UrlCodecActivity;
import com.example.androiddemo.tools.HashCalculatorActivity;
import com.example.androiddemo.tools.LengthConverterActivity;
import com.example.androiddemo.tools.WeightConverterActivity;
import com.example.androiddemo.tools.AreaConverterActivity;
import com.example.androiddemo.tools.TemperatureConverterActivity;
import com.example.androiddemo.tools.LoanCalculatorActivity;
import com.example.androiddemo.tools.CompoundInterestActivity;
import com.example.androiddemo.tools.BonusCalculatorActivity;

/**
 * 基础工具首页
 * 包含：手电筒、计算器、QR扫描、颜色选择、设备信息等基础工具
 */
public class BasicToolsHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_tools_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_basic_flashlight) {
            intent = new Intent(this, FlashlightActivity.class);
        } else if (id == R.id.btn_basic_vibration_test) {
            intent = new Intent(this, VibrationTestActivity.class);
        } else if (id == R.id.btn_basic_calculator) {
            intent = new Intent(this, CalculatorActivity.class);
        } else if (id == R.id.btn_basic_qr_scanner) {
            intent = new Intent(this, QRScannerActivity.class);
        } else if (id == R.id.btn_basic_qr_generator) {
            intent = new Intent(this, QRGeneratorActivity.class);
        } else if (id == R.id.btn_basic_ngta) {
            intent = new Intent(this, NgtaActivity.class);
        } else if (id == R.id.btn_basic_unit_converter) {
            intent = new Intent(this, UnitConverterActivity.class);
        } else if (id == R.id.btn_basic_app_manager) {
            intent = new Intent(this, AppManagerActivity.class);
        } else if (id == R.id.btn_basic_image_compressor) {
            intent = new Intent(this, ImageCompressorActivity.class);
        } else if (id == R.id.btn_basic_tuner) {
            intent = new Intent(this, TunerActivity.class);
        } else if (id == R.id.btn_basic_metronome) {
            intent = new Intent(this, MetronomeActivity.class);
        } else if (id == R.id.btn_basic_color_picker) {
            intent = new Intent(this, ColorPickerActivity.class);
        } else if (id == R.id.btn_basic_color_picker_screen) {
            intent = new Intent(this, ColorPickerScreenActivity.class);
        } else if (id == R.id.btn_basic_touch_test) {
            intent = new Intent(this, TouchTestActivity.class);
        } else if (id == R.id.btn_basic_device_info) {
            intent = new Intent(this, DeviceInfoActivity.class);
        } else if (id == R.id.btn_basic_system_info) {
            intent = new Intent(this, SystemInfoActivity.class);
        } else if (id == R.id.btn_basic_quick_shortcut) {
            intent = new Intent(this, QuickShortcutActivity.class);
        } else if (id == R.id.btn_basic_password_vault) {
            intent = new Intent(this, PasswordVaultActivity.class);
        } else if (id == R.id.btn_basic_password_manager) {
            intent = new Intent(this, PasswordManagerActivity.class);
        } else if (id == R.id.btn_basic_wallpaper) {
            intent = new Intent(this, WallpaperActivity.class);
        } else if (id == R.id.btn_basic_app_clone) {
            intent = new Intent(this, AppCloneActivity.class);
        } else if (id == R.id.btn_basic_power_schedule) {
            intent = new Intent(this, PowerScheduleActivity.class);
        } else if (id == R.id.btn_basic_gps_converter) {
            intent = new Intent(this, GpsConverterActivity.class);
        } else if (id == R.id.btn_basic_item_storage) {
            intent = new Intent(this, ItemStorageActivity.class);
        } else if (id == R.id.btn_basic_binary_calculator) {
            intent = new Intent(this, BinaryCalculatorActivity.class);
        } else if (id == R.id.btn_basic_base_converter) {
            intent = new Intent(this, AdvancedBaseConverterActivity.class);
        } else if (id == R.id.btn_basic_color_converter) {
            intent = new Intent(this, ColorConverterActivity.class);
        } else if (id == R.id.btn_basic_word_count) {
            intent = new Intent(this, WordCountActivity.class);
        } else if (id == R.id.btn_basic_random_generator) {
            intent = new Intent(this, RandomGeneratorActivity.class);
        } else if (id == R.id.btn_basic_random_number) {
            intent = new Intent(this, RandomNumberGeneratorActivity.class);
        } else if (id == R.id.btn_basic_random_lottery) {
            intent = new Intent(this, RandomLotteryActivity.class);
        } else if (id == R.id.btn_basic_date_calculator) {
            intent = new Intent(this, DateCalculatorActivity.class);
        } else if (id == R.id.btn_basic_family_tree) {
            intent = new Intent(this, FamilyTreeActivity.class);
        } else if (id == R.id.btn_basic_relative_detail) {
            intent = new Intent(this, RelativeDetailActivity.class);
        } else if (id == R.id.btn_basic_love_match) {
            intent = new Intent(this, LoveMatchActivity.class);
        } else if (id == R.id.btn_basic_birthday_code) {
            intent = new Intent(this, BirthdayCodeActivity.class);
        } else if (id == R.id.btn_basic_base64_codec) {
            intent = new Intent(this, Base64CodecActivity.class);
        } else if (id == R.id.btn_basic_url_codec) {
            intent = new Intent(this, UrlCodecActivity.class);
        } else if (id == R.id.btn_basic_hash_calculator) {
            intent = new Intent(this, HashCalculatorActivity.class);
        } else if (id == R.id.btn_basic_length_converter) {
            intent = new Intent(this, LengthConverterActivity.class);
        } else if (id == R.id.btn_basic_weight_converter) {
            intent = new Intent(this, WeightConverterActivity.class);
        } else if (id == R.id.btn_basic_area_converter) {
            intent = new Intent(this, AreaConverterActivity.class);
        } else if (id == R.id.btn_basic_temperature_converter) {
            intent = new Intent(this, TemperatureConverterActivity.class);
        } else if (id == R.id.btn_basic_loan_calculator) {
            intent = new Intent(this, LoanCalculatorActivity.class);
        } else if (id == R.id.btn_basic_compound_interest) {
            intent = new Intent(this, CompoundInterestActivity.class);
        } else if (id == R.id.btn_basic_bonus_calculator) {
            intent = new Intent(this, BonusCalculatorActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
