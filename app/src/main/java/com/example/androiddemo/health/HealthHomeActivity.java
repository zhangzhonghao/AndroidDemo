package com.example.androiddemo.health;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.BmiCalculatorActivity;
import com.example.androiddemo.tools.RelativeCalculatorActivity;
import com.example.androiddemo.tools.CountdownChallengeActivity;
import com.example.androiddemo.tools.WasteSortActivity;
import com.example.androiddemo.tools.StepCounterActivity;
import com.example.androiddemo.tools.FoodCaloriesActivity;
import com.example.androiddemo.tools.BloodTypeMatchActivity;
import com.example.androiddemo.tools.SleepQualityActivity;
import com.example.androiddemo.tools.StressTestActivity;
import com.example.androiddemo.tools.AttentionTestActivity;
import com.example.androiddemo.tools.ReactionTestActivity;
import com.example.androiddemo.tools.QuitSmokingActivity;
import com.example.androiddemo.tools.WaterReminderActivity;
import com.example.androiddemo.tools.PregnancyCalculatorActivity;
import com.example.androiddemo.tools.HeightPredictorActivity;
import com.example.androiddemo.tools.CalorieCalculatorActivity;
import com.example.androiddemo.tools.PeriodCalculatorActivity;

/**
 * 健康首页
 * 包含：BMI、亲戚计算、倒计时挑战、垃圾分类、计步器、食物热量等健康功能
 */
public class HealthHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_bmi_calculator) {
            intent = new Intent(this, BmiCalculatorActivity.class);
        } else if (id == R.id.btn_relative_calculator) {
            intent = new Intent(this, RelativeCalculatorActivity.class);
        } else if (id == R.id.btn_countdown_challenge) {
            intent = new Intent(this, CountdownChallengeActivity.class);
        } else if (id == R.id.btn_waste_sort) {
            intent = new Intent(this, WasteSortActivity.class);
        } else if (id == R.id.btn_step_counter) {
            intent = new Intent(this, StepCounterActivity.class);
        } else if (id == R.id.btn_food_calories) {
            intent = new Intent(this, FoodCaloriesActivity.class);
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
        } else if (id == R.id.btn_quit_smoking) {
            intent = new Intent(this, QuitSmokingActivity.class);
        } else if (id == R.id.btn_water_reminder) {
            intent = new Intent(this, WaterReminderActivity.class);
        } else if (id == R.id.btn_pregnancy_calculator) {
            intent = new Intent(this, PregnancyCalculatorActivity.class);
        } else if (id == R.id.btn_height_predictor) {
            intent = new Intent(this, HeightPredictorActivity.class);
        } else if (id == R.id.btn_calorie_calculator) {
            intent = new Intent(this, CalorieCalculatorActivity.class);
        } else if (id == R.id.btn_period_calculator) {
            intent = new Intent(this, PeriodCalculatorActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
