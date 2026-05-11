package com.example.androiddemo.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.Game2048Activity;
import com.example.androiddemo.tools.TicTacToeActivity;
import com.example.androiddemo.tools.RandomLotteryActivity;
import com.example.androiddemo.tools.TetrisActivity;
import com.example.androiddemo.tools.JigsawPuzzleActivity;
import com.example.androiddemo.tools.MemoryCardActivity;
import com.example.androiddemo.tools.NumberSliderActivity;
import com.example.androiddemo.tools.OneStrokeActivity;
import com.example.androiddemo.tools.GuessNumberActivity;
import com.example.androiddemo.tools.DiceRollerActivity;
import com.example.androiddemo.tools.RockPaperScissorsActivity;

/**
 * 游戏首页
 * 包含：2048、九宫格、抽奖、俄罗斯方块、拼图等游戏功能
 */
public class GameHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_game_2048) {
            intent = new Intent(this, Game2048Activity.class);
        } else if (id == R.id.btn_tic_tac_toe) {
            intent = new Intent(this, TicTacToeActivity.class);
        } else if (id == R.id.btn_random_lottery) {
            intent = new Intent(this, RandomLotteryActivity.class);
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
        } else if (id == R.id.btn_guess_number) {
            intent = new Intent(this, GuessNumberActivity.class);
        } else if (id == R.id.btn_dice_roller) {
            intent = new Intent(this, DiceRollerActivity.class);
        } else if (id == R.id.btn_rock_paper_scissors) {
            intent = new Intent(this, RockPaperScissorsActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
