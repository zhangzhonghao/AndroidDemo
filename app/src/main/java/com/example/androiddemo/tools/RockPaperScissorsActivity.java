package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class RockPaperScissorsActivity extends AppCompatActivity {
    private static final int ROCK = 0;
    private static final int PAPER = 1;
    private static final int SCISSORS = 2;

    private TextView tvResult;
    private TextView tvPlayerChoice;
    private TextView tvComputerChoice;
    private ImageView ivPlayer;
    private ImageView ivComputer;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rock_paper_scissors);

        tvResult = findViewById(R.id.tv_result);
        tvPlayerChoice = findViewById(R.id.tv_player_choice);
        tvComputerChoice = findViewById(R.id.tv_computer_choice);
        ivPlayer = findViewById(R.id.iv_player);
        ivComputer = findViewById(R.id.iv_computer);

        Button btnRock = findViewById(R.id.btn_rock);
        Button btnPaper = findViewById(R.id.btn_paper);
        Button btnScissors = findViewById(R.id.btn_scissors);

        btnRock.setOnClickListener(v -> play(ROCK));
        btnPaper.setOnClickListener(v -> play(PAPER));
        btnScissors.setOnClickListener(v -> play(SCISSORS));
    }

    private void play(int playerChoice) {
        int computerChoice = random.nextInt(3);

        tvPlayerChoice.setText(getChoiceName(playerChoice));
        tvComputerChoice.setText(getChoiceName(computerChoice));

        ivPlayer.setImageResource(getChoiceImage(playerChoice));
        ivComputer.setImageResource(getChoiceImage(computerChoice));

        String result;
        if (playerChoice == computerChoice) {
            result = "平局!";
        } else if ((playerChoice == ROCK && computerChoice == SCISSORS) ||
                   (playerChoice == PAPER && computerChoice == ROCK) ||
                   (playerChoice == SCISSORS && computerChoice == PAPER)) {
            result = "你赢了!";
        } else {
            result = "你输了!";
        }

        tvResult.setText(result);
    }

    private String getChoiceName(int choice) {
        switch (choice) {
            case ROCK: return "石头";
            case PAPER: return "布";
            case SCISSORS: return "剪刀";
            default: return "";
        }
    }

    private int getChoiceImage(int choice) {
        switch (choice) {
            case ROCK: return android.R.drawable.ic_menu_compass;
            case PAPER: return android.R.drawable.ic_menu_edit;
            case SCISSORS: return android.R.drawable.ic_menu_delete;
            default: return android.R.drawable.ic_menu_help;
        }
    }
}