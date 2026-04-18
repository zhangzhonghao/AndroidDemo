package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LunarCalendarActivity extends AppCompatActivity {

    private TextView tvSolarDate;
    private TextView tvWeekday;
    private TextView tvLunarYear;
    private TextView tvLunarDate;
    private TextView tvZodiac;
    private GridLayout solarTermsGrid;

    private int currentYear;
    private int currentMonth;
    private int currentDay;

    private static final String[] WEEKDAYS = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    private static final String[] LUNAR_MONTHS = {
            "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
    };

    private static final String[] LUNAR_DAYS = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    private static final String[] ZODIACS = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};

    private static final String[] SOLAR_TERMS = {
            "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
            "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
            "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
            "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };

    // 二十四节气日期（按公历日期，每年略有差异，这里使用2024年数据）
    private static final Map<Integer, String> SOLAR_TERMS_MAP = new HashMap<>();
    static {
        SOLAR_TERMS_MAP.put(106, "小寒");
        SOLAR_TERMS_MAP.put(120, "大寒");
        SOLAR_TERMS_MAP.put(204, "立春");
        SOLAR_TERMS_MAP.put(219, "雨水");
        SOLAR_TERMS_MAP.put(305, "惊蛰");
        SOLAR_TERMS_MAP.put(320, "春分");
        SOLAR_TERMS_MAP.put(404, "清明");
        SOLAR_TERMS_MAP.put(419, "谷雨");
        SOLAR_TERMS_MAP.put(505, "立夏");
        SOLAR_TERMS_MAP.put(520, "小满");
        SOLAR_TERMS_MAP.put(605, "芒种");
        SOLAR_TERMS_MAP.put(621, "夏至");
        SOLAR_TERMS_MAP.put(706, "小暑");
        SOLAR_TERMS_MAP.put(722, "大暑");
        SOLAR_TERMS_MAP.put(807, "立秋");
        SOLAR_TERMS_MAP.put(822, "处暑");
        SOLAR_TERMS_MAP.put(907, "白露");
        SOLAR_TERMS_MAP.put(922, "秋分");
        SOLAR_TERMS_MAP.put(1008, "寒露");
        SOLAR_TERMS_MAP.put(1023, "霜降");
        SOLAR_TERMS_MAP.put(1107, "立冬");
        SOLAR_TERMS_MAP.put(1122, "小雪");
        SOLAR_TERMS_MAP.put(1206, "大雪");
        SOLAR_TERMS_MAP.put(1221, "冬至");
    }

    // 传统节日（按公历日期）
    private static final Map<Integer, String> TRADITIONAL_FESTIVALS = new HashMap<>();
    static {
        // 农历节日（按公历日期近似）
        TRADITIONAL_FESTIVALS.put(115, "小年");
        TRADITIONAL_FESTIVALS.put(130, "除夕");
        TRADITIONAL_FESTIVALS.put(131, "春节");
        TRADITIONAL_FESTIVALS.put(201, "春节");
        TRADITIONAL_FESTIVALS.put(202, "春节");
        TRADITIONAL_FESTIVALS.put(214, "元宵节");
        TRADITIONAL_FESTIVALS.put(306, "龙抬头");
        TRADITIONAL_FESTIVALS.put(505, "端午节");
        TRADITIONAL_FESTIVALS.put(715, "中元节");
        TRADITIONAL_FESTIVALS.put(815, "中秋节");
        TRADITIONAL_FESTIVALS.put(909, "重阳节");
        TRADITIONAL_FESTIVALS.put(1208, "腊八节");
    }

    // 法定节日
    private static final Map<Integer, String> LEGAL_HOLIDAYS = new HashMap<>();
    static {
        LEGAL_HOLIDAYS.put(101, "元旦");
        LEGAL_HOLIDAYS.put(214, "情人节");
        LEGAL_HOLIDAYS.put(308, "妇女节");
        LEGAL_HOLIDAYS.put(312, "植树节");
        LEGAL_HOLIDAYS.put(401, "愚人节");
        LEGAL_HOLIDAYS.put(501, "劳动节");
        LEGAL_HOLIDAYS.put(601, "儿童节");
        LEGAL_HOLIDAYS.put(701, "建党节");
        LEGAL_HOLIDAYS.put(801, "建军节");
        LEGAL_HOLIDAYS.put(910, "教师节");
        LEGAL_HOLIDAYS.put(1001, "国庆节");
        LEGAL_HOLIDAYS.put(1024, "联合国日");
        LEGAL_HOLIDAYS.put(1111, "光棍节");
        LEGAL_HOLIDAYS.put(1224, "平安夜");
        LEGAL_HOLIDAYS.put(1225, "圣诞节");
    }

    // 春节日期（用于计算农历年份）
    private static final Map<Integer, int[]> SPRING_FESTIVAL = new HashMap<>();
    static {
        SPRING_FESTIVAL.put(2024, new int[]{2, 10});
        SPRING_FESTIVAL.put(2025, new int[]{1, 29});
        SPRING_FESTIVAL.put(2026, new int[]{2, 17});
        SPRING_FESTIVAL.put(2027, new int[]{2, 6});
        SPRING_FESTIVAL.put(2028, new int[]{1, 26});
        SPRING_FESTIVAL.put(2029, new int[]{2, 13});
        SPRING_FESTIVAL.put(2030, new int[]{2, 3});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunar_calendar);

        initViews();
        initCurrentDate();
        updateDisplay();
        buildSolarTermsGrid();
    }

    private void initViews() {
        tvSolarDate = findViewById(R.id.tv_solar_date);
        tvWeekday = findViewById(R.id.tv_weekday);
        tvLunarYear = findViewById(R.id.tv_lunar_year);
        tvLunarDate = findViewById(R.id.tv_lunar_date);
        tvZodiac = findViewById(R.id.tv_zodiac);
        solarTermsGrid = findViewById(R.id.solar_terms_grid);

        findViewById(R.id.btn_prev_day).setOnClickListener(v -> changeDay(-1));
        findViewById(R.id.btn_next_day).setOnClickListener(v -> changeDay(1));
    }

    private void initCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void changeDay(int delta) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, currentDay);
        calendar.add(Calendar.DAY_OF_MONTH, delta);

        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateDisplay();
    }

    private void updateDisplay() {
        // 公历日期
        tvSolarDate.setText(currentYear + "年" + currentMonth + "月" + currentDay + "日");

        // 星期
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, currentDay);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        tvWeekday.setText(WEEKDAYS[weekday]);

        // 农历信息
        LunarInfo lunarInfo = getLunarInfo(currentYear, currentMonth, currentDay);
        tvLunarYear.setText("农历" + lunarInfo.year + "年");
        tvLunarDate.setText(lunarInfo.monthStr + "月" + lunarInfo.dayStr);
        tvZodiac.setText("属" + lunarInfo.zodiac);
    }

    private LunarInfo getLunarInfo(int year, int month, int day) {
        LunarInfo info = new LunarInfo();
        info.year = year;

        // 找到春节日期
        int[] springDate = findSpringFestival(year);
        if (springDate == null) {
            info.monthStr = "正";
            info.dayStr = LUNAR_DAYS[day - 1];
            info.zodiac = ZODIACS[(year - 1900) % 12];
            return info;
        }

        int springMonth = springDate[0];
        int springDay = springDate[1];

        // 计算距离春节的天数
        int daysFromSpring = daysBetween(year, springMonth, springDay, year, month, day);

        // 计算农历月和日
        int lunarMonth = 1;
        int lunarDay = daysFromSpring + 1;

        // 如果在春节之前
        if (daysFromSpring < 0) {
            // 找到上一年的春节
            int[] prevSpring = findSpringFestival(year - 1);
            if (prevSpring != null) {
                int daysInPrevYear = daysBetween(year - 1, prevSpring[0], prevSpring[1], year, springMonth, springDay);
                lunarDay = daysInPrevYear + daysFromSpring + 1;
                lunarMonth = 12;
                if (lunarDay <= 0) {
                    lunarMonth = 11;
                    lunarDay += 30;
                }
            }
        }

        // 处理月和日的溢出
        while (lunarDay > 30) {
            lunarDay -= 30;
            lunarMonth++;
        }
        while (lunarDay < 1) {
            lunarMonth--;
            lunarDay += 30;
        }

        // 调整月份显示（考虑闰月等，这里简化处理）
        if (lunarMonth < 1) lunarMonth = 1;
        if (lunarMonth > 12) lunarMonth = 12;

        info.monthStr = LUNAR_MONTHS[lunarMonth - 1];
        info.dayStr = LUNAR_DAYS[Math.min(lunarDay - 1, 29)];
        info.zodiac = ZODIACS[(year - 1900) % 12];

        return info;
    }

    private int[] findSpringFestival(int year) {
        for (Map.Entry<Integer, int[]> entry : SPRING_FESTIVAL.entrySet()) {
            if (entry.getKey() == year) {
                return entry.getValue();
            }
        }
        // 估算春节日期（大约在1月21日到2月20日之间）
        return new int[]{2, (year % 4 == 0) ? 4 : 3};
    }

    private int daysBetween(int y1, int m1, int d1, int y2, int m2, int d2) {
        Calendar c1 = Calendar.getInstance();
        c1.set(y1, m1 - 1, d1);
        Calendar c2 = Calendar.getInstance();
        c2.set(y2, m2 - 1, d2);
        return (int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / (1000 * 60 * 60 * 24));
    }

    private void buildSolarTermsGrid() {
        solarTermsGrid.removeAllViews();

        for (int i = 0; i < SOLAR_TERMS.length; i++) {
            TextView termView = createSolarTermView(SOLAR_TERMS[i], i);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 4, 1f);
            params.rowSpec = GridLayout.spec(i / 4);
            params.setMargins(4, 4, 4, 4);
            termView.setLayoutParams(params);
            solarTermsGrid.addView(termView);
        }
    }

    private TextView createSolarTermView(String term, int index) {
        TextView textView = new TextView(this);
        textView.setText(term);
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(8, 12, 8, 12);

        // 检查当前日期是否有这个节气
        String todayTerm = getTodaySolarTerm();
        if (term.equals(todayTerm)) {
            textView.setBackgroundResource(R.drawable.circle_button_background);
            textView.setTextColor(getResources().getColor(R.color.primary, null));
        } else {
            textView.setTextColor(getResources().getColor(R.color.on_surface_variant, null));
        }

        return textView;
    }

    private String getTodaySolarTerm() {
        int key = currentMonth * 100 + currentDay;
        return SOLAR_TERMS_MAP.getOrDefault(key, null);
    }

    private static class LunarInfo {
        int year;
        String monthStr;
        String dayStr;
        String zodiac;
    }
}