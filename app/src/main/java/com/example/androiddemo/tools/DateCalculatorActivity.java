package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateCalculatorActivity extends AppCompatActivity {

    private DatePicker dpStartDate;
    private DatePicker dpEndDate;
    private DatePicker dpBaseDate;
    private EditText etDaysOffset;
    private RadioGroup rgCalculationType;
    private TextView tvResult;
    private TextView tvDayOfWeek;
    private TextView tvLunarDate;
    private TextView tvQuickResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_calculator);

        initViews();
        setupListeners();
    }

    private void initViews() {
        dpStartDate = findViewById(R.id.dp_start_date);
        dpEndDate = findViewById(R.id.dp_end_date);
        dpBaseDate = findViewById(R.id.dp_base_date);
        etDaysOffset = findViewById(R.id.et_days_offset);
        rgCalculationType = findViewById(R.id.rg_calculation_type);
        tvResult = findViewById(R.id.tv_result);
        tvDayOfWeek = findViewById(R.id.tv_day_of_week);
        tvLunarDate = findViewById(R.id.tv_lunar_date);
        tvQuickResult = findViewById(R.id.tv_quick_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("日期计算器");
        }

        // 初始化日期选择器为今天
        Calendar today = Calendar.getInstance();
        dpStartDate.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), null);
        dpEndDate.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), null);
        dpBaseDate.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), null);

        updateDayOfWeek();
        updateLunarDate();
    }

    private void setupListeners() {
        rgCalculationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_days_diff) {
                findViewById(R.id.layout_days_diff).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_days_offset).setVisibility(View.GONE);
                findViewById(R.id.layout_day_of_week).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_days_offset) {
                findViewById(R.id.layout_days_diff).setVisibility(View.GONE);
                findViewById(R.id.layout_days_offset).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_day_of_week).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_day_of_week) {
                findViewById(R.id.layout_days_diff).setVisibility(View.GONE);
                findViewById(R.id.layout_days_offset).setVisibility(View.GONE);
                findViewById(R.id.layout_day_of_week).setVisibility(View.VISIBLE);
            }
        });

        // 日期选择器变化监听
        DatePicker.OnDateChangedListener dateListener = (view, year, month, dayOfMonth) -> {
            updateDayOfWeek();
            updateLunarDate();
        };
        dpBaseDate.setOnDateChangedListener(dateListener);

        Button btnCalculate = findViewById(R.id.btn_calculate);
        btnCalculate.setOnClickListener(v -> calculate());

        // 快捷计算按钮
        findViewById(R.id.btn_workday).setOnClickListener(v -> calculateWorkday());
        findViewById(R.id.btn_weekend).setOnClickListener(v -> calculateWeekend());
        findViewById(R.id.btn_month_start).setOnClickListener(v -> calculateMonthStart());
        findViewById(R.id.btn_month_end).setOnClickListener(v -> calculateMonthEnd());
        findViewById(R.id.btn_year_start).setOnClickListener(v -> calculateYearStart());
        findViewById(R.id.btn_year_end).setOnClickListener(v -> calculateYearEnd());
    }

    private void calculate() {
        int checkedId = rgCalculationType.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_days_diff) {
            calculateDaysDiff();
        } else if (checkedId == R.id.rb_days_offset) {
            calculateDaysOffset();
        }
    }

    private void calculateDaysDiff() {
        Calendar start = Calendar.getInstance();
        start.set(dpStartDate.getYear(), dpStartDate.getMonth(), dpStartDate.getDayOfMonth());

        Calendar end = Calendar.getInstance();
        end.set(dpEndDate.getYear(), dpEndDate.getMonth(), dpEndDate.getDayOfMonth());

        long diffMillis = end.getTimeInMillis() - start.getTimeInMillis();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);

        tvResult.setText(String.format(Locale.getDefault(), "相差 %d 天", diffDays));
    }

    private void calculateDaysOffset() {
        String daysStr = etDaysOffset.getText().toString();
        if (daysStr.isEmpty()) {
            tvResult.setText("请输入天数");
            return;
        }

        try {
            int days = Integer.parseInt(daysStr);

            Calendar base = Calendar.getInstance();
            base.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), dpBaseDate.getDayOfMonth());
            base.add(Calendar.DAY_OF_MONTH, days);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String resultDate = sdf.format(base.getTime());

            String prefix = days >= 0 ? "后" : "前";
            tvResult.setText(String.format(Locale.getDefault(), "%d天%s：%s\n星期%s",
                    Math.abs(days), prefix, resultDate, getDayOfWeekName(base.get(Calendar.DAY_OF_WEEK))));

            // 更新底部显示
            dpBaseDate.init(base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH), null);
            updateDayOfWeek();
            updateLunarDate();
        } catch (NumberFormatException e) {
            tvResult.setText("请输入有效的天数");
        }
    }

    private void updateDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), dpBaseDate.getDayOfMonth());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String dayName = getDayOfWeekName(dayOfWeek);
        tvDayOfWeek.setText(dayName);
    }

    private String getDayOfWeekName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "星期日";
            case Calendar.MONDAY: return "星期一";
            case Calendar.TUESDAY: return "星期二";
            case Calendar.WEDNESDAY: return "星期三";
            case Calendar.THURSDAY: return "星期四";
            case Calendar.FRIDAY: return "星期五";
            case Calendar.SATURDAY: return "星期六";
            default: return "";
        }
    }

    private void updateLunarDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), dpBaseDate.getDayOfMonth());
        LunarCalendar lunar = new LunarCalendar(cal);
        tvLunarDate.setText(lunar.toString());
    }

    private void calculateWorkday() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), dpBaseDate.getDayOfMonth());

        int workdays = 0;
        Calendar temp = (Calendar) cal.clone();
        // 计算本月工作日数（周一到周五）
        int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            temp.set(Calendar.DAY_OF_MONTH, i);
            int dayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
                workdays++;
            }
        }

        tvQuickResult.setText(String.format(Locale.getDefault(), "%d年%d月工作日数：%d天",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, workdays));
    }

    private void calculateWeekend() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), dpBaseDate.getDayOfMonth());

        int weekends = 0;
        Calendar temp = (Calendar) cal.clone();
        int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            temp.set(Calendar.DAY_OF_MONTH, i);
            int dayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                weekends++;
            }
        }

        tvQuickResult.setText(String.format(Locale.getDefault(), "%d年%d月周末日数：%d天",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, weekends));
    }

    private void calculateMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvQuickResult.setText(String.format(Locale.getDefault(), "%d年%d月月初：%s",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, sdf.format(cal.getTime())));
    }

    private void calculateMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), dpBaseDate.getMonth(), 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvQuickResult.setText(String.format(Locale.getDefault(), "%d年%d月月末：%s",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, sdf.format(cal.getTime())));
    }

    private void calculateYearStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), Calendar.JANUARY, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvQuickResult.setText(String.format(Locale.getDefault(), "%d年年初：%s",
                cal.get(Calendar.YEAR), sdf.format(cal.getTime())));
    }

    private void calculateYearEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(dpBaseDate.getYear(), Calendar.DECEMBER, 31);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvQuickResult.setText(String.format(Locale.getDefault(), "%d年年末：%s",
                cal.get(Calendar.YEAR), sdf.format(cal.getTime())));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 农历日期工具类
     */
    private static class LunarCalendar {
        private static final String[] GAN = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        private static final String[] ZHI = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        private static final String[] SHENGXIAO = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        private static final String[] LUNAR_MONTH = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
        private static final String[] LUNAR_DAY = {
                "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        };

        private int year;
        private int month;
        private int day;

        public LunarCalendar(Calendar solar) {
            int[] lunar = solarToLunar(solar.get(Calendar.YEAR), solar.get(Calendar.MONTH) + 1, solar.get(Calendar.DAY_OF_MONTH));
            this.year = lunar[0];
            this.month = lunar[1];
            this.day = lunar[2];
        }

        private int[] solarToLunar(int year, int month, int day) {
            // 使用简化算法计算农历（适用于1900-2100年）
            int lunarYear = year;
            int lunarMonth = month;
            int lunarDay = day;

            // 计算儒略日
            int julianDay = calcJulianDay(year, month, day);

            // 1900年1月31日是农历1900年正月初一
            int baseJulianDay = 2415021; // 1900-01-31

            int offset = julianDay - baseJulianDay;

            // 粗略计算农历年
            lunarYear = 1900;
            while (offset > getLunarYearDays(lunarYear)) {
                offset -= getLunarYearDays(lunarYear);
                lunarYear++;
            }

            // 计算农历月
            lunarMonth = 1;
            while (offset > getLunarMonthDays(lunarYear, lunarMonth)) {
                offset -= getLunarMonthDays(lunarYear, lunarMonth);
                lunarMonth++;
            }

            lunarDay = (int) offset + 1;

            return new int[]{lunarYear, lunarMonth, lunarDay};
        }

        private int calcJulianDay(int year, int month, int day) {
            int y = year;
            int m = month;
            if (m <= 2) {
                y--;
                m += 12;
            }
            int a = y / 100;
            int b = 2 - a + a / 4;
            return (int) (365.25 * (y + 4716) + 30.6001 * (m + 1) + day + b - 1524.5);
        }

        private int getLunarYearDays(int year) {
            int days = 348;
            for (int i = 0x8000; i != 0x8; i >>= 1) {
                if ((getLunarLeapMonth(year) & i) != 0) {
                    days++;
                }
            }
            return days + getLunarLeapDays(year);
        }

        private int getLunarMonthDays(int year, int month) {
            if ((getLunarMonthDaysArray(year) & (0x10000 >> month)) == 0) {
                return 29;
            }
            return 30;
        }

        private int getLunarLeapMonth(int year) {
            return LunarData.LUNAR_INFO[year - 1900] & 0xf;
        }

        private int getLunarLeapDays(int year) {
            if (getLunarLeapMonth(year) != 0) {
                if ((LunarData.LUNAR_INFO[year - 1900] & 0x10000) != 0) {
                    return 30;
                }
                return 29;
            }
            return 0;
        }

        private int getLunarMonthDaysArray(int year) {
            int info = LunarData.LUNAR_INFO[year - 1900];
            if (year % 4 == 0) {
                if (year % 100 == 0 && year % 400 != 0) {
                    // 世纪年不闰
                } else {
                    info |= 0x100;
                }
            }
            return info;
        }

        public String toString() {
            int tianganIndex = (year - 4) % 10;
            int dizhiIndex = (year - 4) % 12;
            String yearName = GAN[tianganIndex] + ZHI[dizhiIndex];
            String monthName = LUNAR_MONTH[month - 1] + "月";
            String dayName = LUNAR_DAY[day - 1];
            String shengxiao = SHENGXIAO[dizhiIndex];
            return String.format("%s年(%s) %s%s", year, shengxiao, monthName, dayName);
        }
    }

    /**
     * 农历数据（1900-2100年）
     */
    private static class LunarData {
        // 每个元素的高4位表示闰月，低12位表示每月大小
        static final int[] LUNAR_INFO = {
                0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
                0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
                0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
                0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
                0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
                0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
                0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
                0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
                0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
                0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
                0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
                0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
                0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
                0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
                0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
                0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
                0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
                0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
                0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
                0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
                0x0d520
        };
    }
}