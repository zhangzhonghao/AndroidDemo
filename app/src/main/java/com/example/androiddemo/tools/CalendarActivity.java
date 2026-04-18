package com.example.androiddemo.tools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvYear;
    private TextView tvMonth;
    private GridLayout calendarGrid;
    private TextView tvSelectedDate;
    private TextView tvLunarInfo;
    private TextView tvHolidayInfo;

    private int currentYear;
    private int currentMonth;
    private int selectedDay = -1;

    private static final String[] LUNAR_MONTHS = {
            "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
    };

    private static final String[] LUNAR_DAYS = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    private static final String[] SOLAR_TERMS = {
            "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
            "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
            "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
            "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };

    // 节假日映射 (月*100+日 -> 节日名称)
    private static final Map<Integer, String> HOLIDAYS = new HashMap<>();
    static {
        // 法定节日
        HOLIDAYS.put(101, "元旦");
        HOLIDAYS.put(102, "元旦");
        HOLIDAYS.put(103, "元旦");
        HOLIDAYS.put(214, "情人节");
        HOLIDAYS.put(308, "妇女节");
        HOLIDAYS.put(312, "植树节");
        HOLIDAYS.put(401, "愚人节");
        HOLIDAYS.put(501, "劳动节");
        HOLIDAYS.put(502, "劳动节");
        HOLIDAYS.put(503, "劳动节");
        HOLIDAYS.put(504, "劳动节");
        HOLIDAYS.put(505, "劳动节");
        HOLIDAYS.put(506, "劳动节");
        HOLIDAYS.put(507, "劳动节");
        HOLIDAYS.put(401, "青年节");
        HOLIDAYS.put(601, "儿童节");
        HOLIDAYS.put(701, "建党节");
        HOLIDAYS.put(801, "建军节");
        HOLIDAYS.put  (910, "教师节");
        HOLIDAYS.put(1001, "国庆节");
        HOLIDAYS.put(1002, "国庆节");
        HOLIDAYS.put(1003, "国庆节");
        HOLIDAYS.put(1004, "国庆节");
        HOLIDAYS.put(1005, "国庆节");
        HOLIDAYS.put(1006, "国庆节");
        HOLIDAYS.put(1007, "国庆节");
        HOLIDAYS.put(1111, "光棍节");
        HOLIDAYS.put(1224, "平安夜");
        HOLIDAYS.put(1225, "圣诞节");

        // 中国传统节日
        HOLIDAYS.put(115, "小年");
        HOLIDAYS.put(130, "除夕");
        HOLIDAYS.put(131, "春节");
        HOLIDAYS.put(201, "春节");
        HOLIDAYS.put(202, "春节");
        HOLIDAYS.put(203, "春节");
        HOLIDAYS.put(204, "春节");
        HOLIDAYS.put(205, "春节");
        HOLIDAYS.put(206, "春节");
        HOLIDAYS.put(214, "元宵节");
        HOLIDAYS.put(306, "龙抬头");
        HOLIDAYS.put(405, "清明节");
        HOLIDAYS.put(406, "清明节");
        HOLIDAYS.put(407, "清明节");
        HOLIDAYS.put(505, "端午节");
        HOLIDAYS.put(715, "中元节");
        HOLIDAYS.put(815, "中秋节");
        HOLIDAYS.put(909, "重阳节");
        HOLIDAYS.put(1208, "腊八节");
    }

    // 二十四节气计算（简化查表法，基于近几年的数据）
    private static final Map<Integer, String> SOLAR_TERMS_MAP = new HashMap<>();
    static {
        // 2024年二十四节气（简化版，按公历日期）
        SOLAR_TERMS_MAP.put(20240106, "小寒");
        SOLAR_TERMS_MAP.put(20240120, "大寒");
        SOLAR_TERMS_MAP.put(20240204, "立春");
        SOLAR_TERMS_MAP.put(20240219, "雨水");
        SOLAR_TERMS_MAP.put(20240305, "惊蛰");
        SOLAR_TERMS_MAP.put(20240320, "春分");
        SOLAR_TERMS_MAP.put(20240404, "清明");
        SOLAR_TERMS_MAP.put(20240419, "谷雨");
        SOLAR_TERMS_MAP.put(20240505, "立夏");
        SOLAR_TERMS_MAP.put(20240520, "小满");
        SOLAR_TERMS_MAP.put(20240605, "芒种");
        SOLAR_TERMS_MAP.put(20240621, "夏至");
        SOLAR_TERMS_MAP.put(20240706, "小暑");
        SOLAR_TERMS_MAP.put(20240722, "大暑");
        SOLAR_TERMS_MAP.put(20240807, "立秋");
        SOLAR_TERMS_MAP.put(20240822, "处暑");
        SOLAR_TERMS_MAP.put(20240907, "白露");
        SOLAR_TERMS_MAP.put(20240922, "秋分");
        SOLAR_TERMS_MAP.put(20241008, "寒露");
        SOLAR_TERMS_MAP.put(20241023, "霜降");
        SOLAR_TERMS_MAP.put(20241107, "立冬");
        SOLAR_TERMS_MAP.put(20241122, "小雪");
        SOLAR_TERMS_MAP.put(20241206, "大雪");
        SOLAR_TERMS_MAP.put(20241221, "冬至");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();
        initCurrentDate();
        updateCalendar();
    }

    private void initViews() {
        tvYear = findViewById(R.id.tv_year);
        tvMonth = findViewById(R.id.tv_month);
        calendarGrid = findViewById(R.id.calendar_grid);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvLunarInfo = findViewById(R.id.tv_lunar_info);
        tvHolidayInfo = findViewById(R.id.tv_holiday_info);

        findViewById(R.id.btn_prev_year).setOnClickListener(v -> changeYear(-1));
        findViewById(R.id.btn_next_year).setOnClickListener(v -> changeYear(1));
        findViewById(R.id.btn_prev_month).setOnClickListener(v -> changeMonth(-1));
        findViewById(R.id.btn_next_month).setOnClickListener(v -> changeMonth(1));
    }

    private void initCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void changeYear(int delta) {
        currentYear += delta;
        updateCalendar();
    }

    private void changeMonth(int delta) {
        currentMonth += delta;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        } else if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateCalendar();
    }

    private void updateCalendar() {
        tvYear.setText(String.valueOf(currentYear));
        tvMonth.setText(getMonthName(currentMonth));
        buildCalendarGrid();
        updateSelectedDateInfo();
    }

    private String getMonthName(int month) {
        String[] months = {"一月", "二月", "三月", "四月", "五月", "六月",
                           "七月", "八月", "九月", "十月", "十一月", "十二月"};
        return months[month - 1];
    }

    private void buildCalendarGrid() {
        calendarGrid.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH) + 1;
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                int position = i * 7 + j;
                int day = position - firstDayOfWeek + 1;

                TextView dayView = createDayView(day, daysInMonth, j, day == selectedDay && day > 0,
                        day == todayDay && currentYear == todayYear && currentMonth == todayMonth);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(j, 1f);
                params.rowSpec = GridLayout.spec(i);
                params.setMargins(2, 2, 2, 2);
                dayView.setLayoutParams(params);

                calendarGrid.addView(dayView);
            }
        }
    }

    private TextView createDayView(int day, int daysInMonth, int dayOfWeek, boolean isSelected, boolean isToday) {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);

        if (day < 1 || day > daysInMonth) {
            textView.setText("");
            textView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            textView.setText(String.valueOf(day));
            textView.setTag(day);

            // 设置字体颜色
            if (isSelected) {
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundResource(R.drawable.circle_button_background);
            } else if (dayOfWeek == 0) {
                textView.setTextColor(getResources().getColor(R.color.error, null));
            } else if (dayOfWeek == 6) {
                textView.setTextColor(getResources().getColor(R.color.primary, null));
            } else {
                textView.setTextColor(getResources().getColor(R.color.on_surface, null));
            }

            if (isToday && !isSelected) {
                textView.setBackgroundResource(R.drawable.circle_button_background);
                textView.setTextColor(getResources().getColor(R.color.primary, null));
            }

            textView.setOnClickListener(v -> {
                selectedDay = (int) v.getTag();
                buildCalendarGrid();
                updateSelectedDateInfo();
            });

            // 标注节假日
            String holiday = getHoliday(currentYear, currentMonth, day);
            if (holiday != null && !isSelected) {
                textView.setTextColor(getResources().getColor(R.color.error, null));
            }
        }

        return textView;
    }

    private void updateSelectedDateInfo() {
        if (selectedDay < 1) {
            tvSelectedDate.setText("请选择日期");
            tvLunarInfo.setText("");
            tvHolidayInfo.setText("");
            return;
        }

        String dateStr = currentYear + "年" + currentMonth + "月" + selectedDay + "日";
        tvSelectedDate.setText(dateStr);

        // 农历信息
        String lunarDate = getLunarDate(currentYear, currentMonth, selectedDay);
        tvLunarInfo.setText("农历: " + lunarDate);

        // 节假日/节气信息
        String holiday = getHoliday(currentYear, currentMonth, selectedDay);
        String solarTerm = getSolarTerm(currentYear, currentMonth, selectedDay);

        StringBuilder holidayBuilder = new StringBuilder();
        if (holiday != null) {
            holidayBuilder.append(holiday);
        }
        if (solarTerm != null) {
            if (holidayBuilder.length() > 0) {
                holidayBuilder.append(" · ");
            }
            holidayBuilder.append(solarTerm);
        }
        tvHolidayInfo.setText(holidayBuilder.toString());
    }

    private String getHoliday(int year, int month, int day) {
        int key = month * 100 + day;
        String holiday = HOLIDAYS.get(key);
        return holiday;
    }

    private String getSolarTerm(int year, int month, int day) {
        int key = year * 10000 + month * 100 + day;
        return SOLAR_TERMS_MAP.get(key);
    }

    // 简化的农历转换算法（基于查表法）
    private String getLunarDate(int year, int month, int day) {
        // 使用简化算法：通过已知参照点计算偏移
        // 2000年1月6日是小寒，农历腊月初一
        // 这里使用一个简化的查表方法

        int lunarYear = year;
        int lunarMonth = month;
        int lunarDay = day;

        // 简化的农历计算（不考虑闰月等复杂情况）
        // 以2024年为基准进行推算
        if (year >= 2020 && year <= 2030) {
            int[][] lunarData = {
                    {2020, 1, 25},  // 2020年1月25日春节
                    {2021, 2, 12},  // 2021年2月12日春节
                    {2022, 2, 1},   // 2022年2月1日春节
                    {2023, 1, 22},  // 2023年1月22日春节
                    {2024, 2, 10},  // 2024年2月10日春节
                    {2025, 1, 29},  // 2025年1月29日春节
                    {2026, 2, 17},  // 2026年2月17日春节
                    {2027, 2, 6},   // 2027年2月6日春节
                    {2028, 1, 26},  // 2028年1月26日春节
                    {2029, 2, 13},  // 2029年2月13日春节
                    {2030, 2, 3},   // 2030年2月3日春节
            };

            for (int[] data : lunarData) {
                if (data[0] == year) {
                    int springMonth = data[1];
                    int springDay = data[2];

                    // 计算距离春节的天数
                    int daysFromSpring = daysBetween(year, springMonth, springDay, year, month, day);

                    // 估算农历月和日
                    lunarMonth = springMonth + (daysFromSpring + 30) / 30;
                    lunarDay = springDay + daysFromSpring;

                    // 处理月溢出
                    while (lunarDay > 30) {
                        lunarDay -= 30;
                        lunarMonth++;
                    }
                    while (lunarDay < 1) {
                        lunarMonth--;
                        lunarDay += 30;
                    }
                    while (lunarMonth > 12) {
                        lunarMonth -= 12;
                    }
                    while (lunarMonth < 1) {
                        lunarMonth += 12;
                    }

                    break;
                }
            }
        }

        String monthStr = lunarMonth <= 12 ? LUNAR_MONTHS[lunarMonth - 1] : "未知";
        String dayStr = lunarDay <= 30 ? LUNAR_DAYS[lunarDay - 1] : "三十";

        return lunarYear + "年" + monthStr + "月" + dayStr;
    }

    private int daysBetween(int y1, int m1, int d1, int y2, int m2, int d2) {
        Calendar c1 = Calendar.getInstance();
        c1.set(y1, m1 - 1, d1);
        Calendar c2 = Calendar.getInstance();
        c2.set(y2, m2 - 1, d2);
        return (int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / (1000 * 60 * 60 * 24));
    }
}