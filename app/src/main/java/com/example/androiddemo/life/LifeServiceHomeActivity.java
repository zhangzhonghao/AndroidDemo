package com.example.androiddemo.life;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.TranslatorActivity;
import com.example.androiddemo.tools.CalendarActivity;
import com.example.androiddemo.tools.LunarCalendarActivity;
import com.example.androiddemo.tools.WeatherActivity;
import com.example.androiddemo.tools.IpLookupActivity;
import com.example.androiddemo.tools.HolidayQueryActivity;
import com.example.androiddemo.tools.TodayInHistoryActivity;
import com.example.androiddemo.tools.HistoryTodayActivity;
import com.example.androiddemo.tools.HistoryDetailActivity;
import com.example.androiddemo.tools.HistoryFavoriteActivity;
import com.example.androiddemo.tools.RandomHistoryActivity;
import com.example.androiddemo.tools.WorldCapitalsActivity;
import com.example.androiddemo.tools.PhoneFortuneActivity;
import com.example.androiddemo.tools.CarPlateFortuneActivity;
import com.example.androiddemo.tools.HoroscopeActivity;
import com.example.androiddemo.tools.DayOfWeekActivity;
import com.example.androiddemo.tools.DateDiffActivity;
import com.example.androiddemo.tools.TextDiffActivity;
import com.example.androiddemo.tools.JsonFormatterActivity;
import com.example.androiddemo.tools.RegexTesterActivity;
import com.example.androiddemo.tools.NumberToChineseActivity;
import com.example.androiddemo.tools.QrScanHistoryActivity;
import com.example.androiddemo.tools.PdfReaderActivity;
import com.example.androiddemo.tools.PdfCreatorActivity;
import com.example.androiddemo.tools.ScreenCastActivity;
import com.example.androiddemo.tools.ScrollCaptureActivity;
import com.example.androiddemo.tools.GifPreviewActivity;
import com.example.androiddemo.tools.PhoneLocatorActivity;
import com.example.androiddemo.tools.ZipCodeActivity;
import com.example.androiddemo.tools.IdCardValidatorActivity;
import com.example.androiddemo.tools.ExchangeRateActivity;
import com.example.androiddemo.tools.ExchangeCalculatorActivity;

/**
 * 生活服务首页
 * 包含：翻译、日历、农历、天气、IP查询、节假日、历史查询等生活服务功能
 */
public class LifeServiceHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_service_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_translator) {
            intent = new Intent(this, TranslatorActivity.class);
        } else if (id == R.id.btn_calendar) {
            intent = new Intent(this, CalendarActivity.class);
        } else if (id == R.id.btn_lunar_calendar) {
            intent = new Intent(this, LunarCalendarActivity.class);
        } else if (id == R.id.btn_weather) {
            intent = new Intent(this, WeatherActivity.class);
        } else if (id == R.id.btn_ip_lookup) {
            intent = new Intent(this, IpLookupActivity.class);
        } else if (id == R.id.btn_holiday_query) {
            intent = new Intent(this, HolidayQueryActivity.class);
        } else if (id == R.id.btn_today_in_history) {
            intent = new Intent(this, TodayInHistoryActivity.class);
        } else if (id == R.id.btn_history_today) {
            intent = new Intent(this, HistoryTodayActivity.class);
        } else if (id == R.id.btn_history_detail) {
            intent = new Intent(this, HistoryDetailActivity.class);
        } else if (id == R.id.btn_history_favorite) {
            intent = new Intent(this, HistoryFavoriteActivity.class);
        } else if (id == R.id.btn_random_history) {
            intent = new Intent(this, RandomHistoryActivity.class);
        } else if (id == R.id.btn_world_capitals) {
            intent = new Intent(this, WorldCapitalsActivity.class);
        } else if (id == R.id.btn_phone_fortune) {
            intent = new Intent(this, PhoneFortuneActivity.class);
        } else if (id == R.id.btn_car_plate_fortune) {
            intent = new Intent(this, CarPlateFortuneActivity.class);
        } else if (id == R.id.btn_horoscope) {
            intent = new Intent(this, HoroscopeActivity.class);
        } else if (id == R.id.btn_day_of_week) {
            intent = new Intent(this, DayOfWeekActivity.class);
        } else if (id == R.id.btn_date_diff) {
            intent = new Intent(this, DateDiffActivity.class);
        } else if (id == R.id.btn_text_diff) {
            intent = new Intent(this, TextDiffActivity.class);
        } else if (id == R.id.btn_json_formatter) {
            intent = new Intent(this, JsonFormatterActivity.class);
        } else if (id == R.id.btn_regex_tester) {
            intent = new Intent(this, RegexTesterActivity.class);
        } else if (id == R.id.btn_number_to_chinese) {
            intent = new Intent(this, NumberToChineseActivity.class);
        } else if (id == R.id.btn_qr_scan_history) {
            intent = new Intent(this, QrScanHistoryActivity.class);
        } else if (id == R.id.btn_pdf_reader) {
            intent = new Intent(this, PdfReaderActivity.class);
        } else if (id == R.id.btn_pdf_creator) {
            intent = new Intent(this, PdfCreatorActivity.class);
        } else if (id == R.id.btn_screen_cast) {
            intent = new Intent(this, ScreenCastActivity.class);
        } else if (id == R.id.btn_scroll_capture) {
            intent = new Intent(this, ScrollCaptureActivity.class);
        } else if (id == R.id.btn_gif_preview) {
            intent = new Intent(this, GifPreviewActivity.class);
        } else if (id == R.id.btn_phone_locator) {
            intent = new Intent(this, PhoneLocatorActivity.class);
        } else if (id == R.id.btn_zip_code) {
            intent = new Intent(this, ZipCodeActivity.class);
        } else if (id == R.id.btn_id_card_validator) {
            intent = new Intent(this, IdCardValidatorActivity.class);
        } else if (id == R.id.btn_exchange_rate) {
            intent = new Intent(this, ExchangeRateActivity.class);
        } else if (id == R.id.btn_exchange_calculator) {
            intent = new Intent(this, ExchangeCalculatorActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
