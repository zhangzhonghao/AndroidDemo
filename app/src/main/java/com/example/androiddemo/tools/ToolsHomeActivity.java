package com.example.androiddemo.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ToolsHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools_home);
    }

    public void onToolClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_flashlight) {
            intent = new Intent(this, FlashlightActivity.class);
        } else if (id == R.id.btn_vibration_test) {
            intent = new Intent(this, VibrationTestActivity.class);
        } else if (id == R.id.btn_calculator) {
            intent = new Intent(this, CalculatorActivity.class);
        } else if (id == R.id.btn_qr_scanner) {
            intent = new Intent(this, QRScannerActivity.class);
        } else if (id == R.id.btn_unit_converter) {
            intent = new Intent(this, UnitConverterActivity.class);
        } else if (id == R.id.btn_compass) {
            intent = new Intent(this, CompassActivity.class);
        } else if (id == R.id.btn_watermark_camera) {
            intent = new Intent(this, WatermarkCameraActivity.class);
        } else if (id == R.id.btn_level_meter) {
            intent = new Intent(this, LevelMeterActivity.class);
        } else if (id == R.id.btn_stopwatch) {
            intent = new Intent(this, StopwatchActivity.class);
        } else if (id == R.id.btn_noise_meter) {
            intent = new Intent(this, NoiseMeterActivity.class);
        } else if (id == R.id.btn_volume_meter) {
            intent = new Intent(this, VolumeMeterActivity.class);
        } else if (id == R.id.btn_ruler) {
            intent = new Intent(this, RulerActivity.class);
        } else if (id == R.id.btn_calendar) {
            intent = new Intent(this, CalendarActivity.class);
        } else if (id == R.id.btn_weather) {
            intent = new Intent(this, WeatherActivity.class);
        } else if (id == R.id.btn_translator) {
            intent = new Intent(this, TranslatorActivity.class);
        } else if (id == R.id.btn_qr_generator) {
            intent = new Intent(this, QRGeneratorActivity.class);
        } else if (id == R.id.btn_document_scanner) {
            intent = new Intent(this, DocumentScannerActivity.class);
        } else if (id == R.id.btn_app_manager) {
            intent = new Intent(this, AppManagerActivity.class);
        } else if (id == R.id.btn_image_compressor) {
            intent = new Intent(this, ImageCompressorActivity.class);
        } else if (id == R.id.btn_video_to_gif) {
            intent = new Intent(this, VideoToGifActivity.class);
        } else if (id == R.id.btn_tuner) {
            intent = new Intent(this, TunerActivity.class);
        } else if (id == R.id.btn_metronome) {
            intent = new Intent(this, MetronomeActivity.class);
        } else if (id == R.id.btn_pomodoro) {
            intent = new Intent(this, PomodoroTimerActivity.class);
        } else if (id == R.id.btn_random_generator) {
            intent = new Intent(this, RandomGeneratorActivity.class);
        } else if (id == R.id.btn_random_number_generator) {
            intent = new Intent(this, RandomNumberGeneratorActivity.class);
        } else if (id == R.id.btn_bmi_calculator) {
            intent = new Intent(this, BmiCalculatorActivity.class);
        } else if (id == R.id.btn_relative_calculator) {
            intent = new Intent(this, RelativeCalculatorActivity.class);
        } else if (id == R.id.btn_speed_test) {
            intent = new Intent(this, SpeedTestActivity.class);
        } else if (id == R.id.btn_date_calculator) {
            intent = new Intent(this, DateCalculatorActivity.class);
        } else if (id == R.id.btn_lunar_calendar) {
            intent = new Intent(this, LunarCalendarActivity.class);
        } else if (id == R.id.btn_draw_board) {
            intent = new Intent(this, DrawBoardActivity.class);
        } else if (id == R.id.btn_pdf_reader) {
            intent = new Intent(this, PdfReaderActivity.class);
        } else if (id == R.id.btn_system_info) {
            intent = new Intent(this, SystemInfoActivity.class);
        } else if (id == R.id.btn_today_in_history) {
            intent = new Intent(this, TodayInHistoryActivity.class);
        } else if (id == R.id.btn_color_picker) {
            intent = new Intent(this, ColorPickerActivity.class);
        } else if (id == R.id.btn_color_picker_screen) {
            intent = new Intent(this, ColorPickerScreenActivity.class);
        } else if (id == R.id.btn_touch_test) {
            intent = new Intent(this, TouchTestActivity.class);
        } else if (id == R.id.btn_waste_sort) {
            intent = new Intent(this, WasteSortActivity.class);
        } else if (id == R.id.btn_exchange_rate) {
            intent = new Intent(this, ExchangeRateActivity.class);
        } else if (id == R.id.btn_phone_locator) {
            intent = new Intent(this, PhoneLocatorActivity.class);
        } else if (id == R.id.btn_ip_lookup) {
            intent = new Intent(this, IpLookupActivity.class);
        } else if (id == R.id.btn_history_today) {
            intent = new Intent(this, HistoryTodayActivity.class);
        } else if (id == R.id.btn_random_lottery) {
            intent = new Intent(this, RandomLotteryActivity.class);
        } else if (id == R.id.btn_word_count) {
            intent = new Intent(this, WordCountActivity.class);
        } else if (id == R.id.btn_binary_calculator) {
            intent = new Intent(this, BinaryCalculatorActivity.class);
        } else if (id == R.id.btn_quick_shortcut) {
            intent = new Intent(this, QuickShortcutActivity.class);
        } else if (id == R.id.btn_device_info) {
            intent = new Intent(this, DeviceInfoActivity.class);
        } else if (id == R.id.btn_base64_codec) {
            intent = new Intent(this, Base64CodecActivity.class);
        } else if (id == R.id.btn_url_codec) {
            intent = new Intent(this, UrlCodecActivity.class);
        } else if (id == R.id.btn_md5) {
            intent = new Intent(this, Md5Activity.class);
        } else if (id == R.id.btn_hash_calculator) {
            intent = new Intent(this, HashCalculatorActivity.class);
        } else if (id == R.id.btn_qr_scan_history) {
            intent = new Intent(this, QrScanHistoryActivity.class);
        } else if (id == R.id.btn_temperature_converter) {
            intent = new Intent(this, TemperatureConverterActivity.class);
        } else if (id == R.id.btn_length_converter) {
            intent = new Intent(this, LengthConverterActivity.class);
        } else if (id == R.id.btn_weight_converter) {
            intent = new Intent(this, WeightConverterActivity.class);
        } else if (id == R.id.btn_history_detail) {
            intent = new Intent(this, HistoryDetailActivity.class);
        } else if (id == R.id.btn_family_tree) {
            intent = new Intent(this, FamilyTreeActivity.class);
        } else if (id == R.id.btn_flash_sale) {
            intent = new Intent(this, FlashSaleActivity.class);
        } else if (id == R.id.btn_screen_cast) {
            intent = new Intent(this, ScreenCastActivity.class);
        } else if (id == R.id.btn_scroll_capture) {
            intent = new Intent(this, ScrollCaptureActivity.class);
        } else if (id == R.id.btn_gif_preview) {
            intent = new Intent(this, GifPreviewActivity.class);
        } else if (id == R.id.btn_pdf_creator) {
            intent = new Intent(this, PdfCreatorActivity.class);
        } else if (id == R.id.btn_qr_pay_collection) {
            intent = new Intent(this, QrPayCollectionActivity.class);
        } else if (id == R.id.btn_exchange_calculator) {
            intent = new Intent(this, ExchangeCalculatorActivity.class);
        } else if (id == R.id.btn_relative_detail) {
            intent = new Intent(this, RelativeDetailActivity.class);
        } else if (id == R.id.btn_history_favorite) {
            intent = new Intent(this, HistoryFavoriteActivity.class);
        } else if (id == R.id.btn_area_converter) {
            intent = new Intent(this, AreaConverterActivity.class);
        } else if (id == R.id.btn_fuel_calculator) {
            intent = new Intent(this, FuelCalculatorActivity.class);
        } else if (id == R.id.btn_blood_type) {
            intent = new Intent(this, BloodTypeActivity.class);
        } else if (id == R.id.btn_pregnancy_calculator) {
            intent = new Intent(this, PregnancyCalculatorActivity.class);
        } else if (id == R.id.btn_height_predictor) {
            intent = new Intent(this, HeightPredictorActivity.class);
        } else if (id == R.id.btn_calorie_calculator) {
            intent = new Intent(this, CalorieCalculatorActivity.class);
        } else if (id == R.id.btn_period_calculator) {
            intent = new Intent(this, PeriodCalculatorActivity.class);
        } else if (id == R.id.btn_loan_calculator) {
            intent = new Intent(this, LoanCalculatorActivity.class);
        } else if (id == R.id.btn_compound_interest) {
            intent = new Intent(this, CompoundInterestActivity.class);
        } else if (id == R.id.btn_advanced_base_converter) {
            intent = new Intent(this, AdvancedBaseConverterActivity.class);
        } else if (id == R.id.btn_color_converter) {
            intent = new Intent(this, ColorConverterActivity.class);
        } else if (id == R.id.btn_tax_calculator) {
            intent = new Intent(this, TaxCalculatorActivity.class);
        } else if (id == R.id.btn_bonus_calculator) {
            intent = new Intent(this, BonusCalculatorActivity.class);
        } else if (id == R.id.btn_wallpaper) {
            intent = new Intent(this, WallpaperActivity.class);
        } else if (id == R.id.btn_app_clone) {
            intent = new Intent(this, AppCloneActivity.class);
        } else if (id == R.id.btn_power_schedule) {
            intent = new Intent(this, PowerScheduleActivity.class);
        } else if (id == R.id.btn_gps_converter) {
            intent = new Intent(this, GpsConverterActivity.class);
        } else if (id == R.id.btn_zip_code) {
            intent = new Intent(this, ZipCodeActivity.class);
        } else if (id == R.id.btn_holiday_query) {
            intent = new Intent(this, HolidayQueryActivity.class);
        } else if (id == R.id.btn_day_of_week) {
            intent = new Intent(this, DayOfWeekActivity.class);
        } else if (id == R.id.btn_date_diff) {
            intent = new Intent(this, DateDiffActivity.class);
        } else if (id == R.id.btn_random_history) {
            intent = new Intent(this, RandomHistoryActivity.class);
        } else if (id == R.id.btn_password_vault) {
            intent = new Intent(this, PasswordVaultActivity.class);
        } else if (id == R.id.btn_text_diff) {
            intent = new Intent(this, TextDiffActivity.class);
        } else if (id == R.id.btn_json_formatter) {
            intent = new Intent(this, JsonFormatterActivity.class);
        } else if (id == R.id.btn_regex_tester) {
            intent = new Intent(this, RegexTesterActivity.class);
        } else if (id == R.id.btn_number_to_chinese) {
            intent = new Intent(this, NumberToChineseActivity.class);
        } else if (id == R.id.btn_world_capitals) {
            intent = new Intent(this, WorldCapitalsActivity.class);
        } else if (id == R.id.btn_id_card_validator) {
            intent = new Intent(this, IdCardValidatorActivity.class);
        } else if (id == R.id.btn_phone_fortune) {
            intent = new Intent(this, PhoneFortuneActivity.class);
        } else if (id == R.id.btn_car_plate_fortune) {
            intent = new Intent(this, CarPlateFortuneActivity.class);
        } else if (id == R.id.btn_horoscope) {
            intent = new Intent(this, HoroscopeActivity.class);
        } else if (id == R.id.btn_birthday_code) {
            intent = new Intent(this, BirthdayCodeActivity.class);
        } else if (id == R.id.btn_love_match) {
            intent = new Intent(this, LoveMatchActivity.class);
        } else if (id == R.id.btn_blood_type_match) {
            intent = new Intent(this, BloodTypeMatchActivity.class);
        } else if (id == R.id.btn_sleep_quality) {
            intent = new Intent(this, SleepQualityActivity.class);
        } else if (id == R.id.btn_stress_test) {
            intent = new Intent(this, StressTestActivity.class);
        } else if (id == R.id.btn_attention_test) {
            intent = new Intent(this, AttentionTestActivity.class);
        } else if (id == R.id.btn_reaction_test) {
            intent = new Intent(this, ReactionTestActivity.class);
        } else if (id == R.id.btn_color_blind_test) {
            intent = new Intent(this, ColorBlindTestActivity.class);
        } else if (id == R.id.btn_mental_health) {
            intent = new Intent(this, MentalHealthActivity.class);
        } else if (id == R.id.btn_quit_smoking) {
            intent = new Intent(this, QuitSmokingActivity.class);
        } else if (id == R.id.btn_water_reminder) {
            intent = new Intent(this, WaterReminderActivity.class);
        } else if (id == R.id.btn_medicine_reminder) {
            intent = new Intent(this, MedicineReminderActivity.class);
        } else if (id == R.id.btn_step_counter) {
            intent = new Intent(this, StepCounterActivity.class);
        } else if (id == R.id.btn_food_calories) {
            intent = new Intent(this, FoodCaloriesActivity.class);
        } else if (id == R.id.btn_game_2048) {
            intent = new Intent(this, Game2048Activity.class);
        } else if (id == R.id.btn_tetris) {
            intent = new Intent(this, TetrisActivity.class);
        } else if (id == R.id.btn_jigsaw_puzzle) {
            intent = new Intent(this, JigsawPuzzleActivity.class);
        } else if (id == R.id.btn_memory_card) {
            intent = new Intent(this, MemoryCardActivity.class);
        } else if (id == R.id.btn_number_slider) {
            intent = new Intent(this, NumberSliderActivity.class);
        } else if (id == R.id.btn_one_stroke) {
            intent = new Intent(this, OneStrokeActivity.class);
        } else if (id == R.id.btn_tic_tac_toe) {
            intent = new Intent(this, TicTacToeActivity.class);
        } else if (id == R.id.btn_guess_number) {
            intent = new Intent(this, GuessNumberActivity.class);
        } else if (id == R.id.btn_dice_roller) {
            intent = new Intent(this, DiceRollerActivity.class);
        } else if (id == R.id.btn_rock_paper_scissors) {
            intent = new Intent(this, RockPaperScissorsActivity.class);
        } else if (id == R.id.btn_lunar_new_year_countdown) {
            intent = new Intent(this, LunarNewYearCountdownActivity.class);
        } else if (id == R.id.btn_countdown_days) {
            intent = new Intent(this, CountdownDaysActivity.class);
        } else if (id == R.id.btn_anniversary_manager) {
            intent = new Intent(this, AnniversaryManagerActivity.class);
        } else if (id == R.id.btn_world_wonders) {
            intent = new Intent(this, WorldWondersActivity.class);
        } else if (id == R.id.btn_country_info) {
            intent = new Intent(this, CountryInfoActivity.class);
        } else if (id == R.id.btn_emergency_contact) {
            intent = new Intent(this, EmergencyContactActivity.class);
        } else if (id == R.id.btn_item_storage) {
            intent = new Intent(this, ItemStorageActivity.class);
        } else if (id == R.id.btn_password_manager) {
            intent = new Intent(this, PasswordManagerActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
