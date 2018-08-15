package com.example.sbw98.gravity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class EndActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent = new Intent(Intent.ACTION_MAIN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //String endStr;
        //TextView endText;

        ImageButton buttonRestart;
        TextView restartText;

        ImageButton buttonQuit;
        TextView quitText;

        ImageButton homeButton;
        TextView homeText;

        Bundle bundle;
        int finalScore;
        TextView scoreText;

        int highScore;
        TextView highScoreText;

        int level;
        int experience;
        int expCap;
        TextView levelText;
        TextView expText;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        if (Build.VERSION.SDK_INT >= 21) getWindow().setEnterTransition(null);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        bundle = getIntent().getExtras();
        try {
            finalScore = bundle.getInt("final score");
        }
        catch (NullPointerException e) {
            finalScore = 0;
        }

        android.content.SharedPreferences gameStats = getApplicationContext().getSharedPreferences("gameStats", 0);
        android.content.SharedPreferences.Editor editor = gameStats.edit();

        highScore = gameStats.getInt("highScore", 0);
        level = gameStats.getInt("level", 1);
        experience = gameStats.getInt("experience", 0);

        if (finalScore >= highScore) {
            highScore = finalScore;
            editor.putInt("highScore", highScore);
            editor.apply();
        }

        experience += finalScore/5;
        expCap = (level*level)*100;
        if (experience >= expCap) {
            level++;
        }

        editor.putInt("level", level);
        editor.putInt("experience", experience);
        editor.putInt("expCap", expCap);
        editor.apply();

        buttonQuit = (ImageButton) findViewById(R.id.buttonQuit);
        quitText = (TextView) findViewById(R.id.quitText);

        buttonRestart = (ImageButton) findViewById(R.id.buttonRestart);
        restartText = (TextView) findViewById(R.id.restartText);

        homeButton = (ImageButton) findViewById(R.id.homeButton);
        homeText = (TextView) findViewById(R.id.homeText);

        buttonQuit.setOnClickListener(this);
        quitText.setOnClickListener(this);
        buttonRestart.setOnClickListener(this);
        restartText.setOnClickListener(this);
        homeButton.setOnClickListener(this);
        homeText.setOnClickListener(this);

        scoreText = (TextView) findViewById(R.id.scoreText);
        String scoreStr = "final score: " + String.valueOf(finalScore);
        scoreText.setText(scoreStr);

        highScoreText = (TextView) findViewById(R.id.highScoreText);
        String highScoreStr = "high score: " + String.valueOf(highScore);
        highScoreText.setText(highScoreStr);

        levelText = (TextView) findViewById(R.id.levelText);
        String levelStr = "level: " + String.valueOf(level);
        levelText.setText(levelStr);

        expText = (TextView) findViewById(R.id.expText);
        String expStr = "experience: " + String.valueOf(experience);
        expText.setText(expStr);

        /*endText = (TextView) findViewById(R.id.endText);
        endStr = gameStats.getString("endText", "game over");
        endText.setText(endStr);*/
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonQuit:
                System.out.println("quit trigger");
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case R.id.quitText:
                System.out.println("quit trigger");
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;

            case R.id.buttonRestart:
                startActivity(new Intent(this, GameActivity.class));
                finish();
                break;
            case R.id.restartText:
                startActivity(new Intent(this, GameActivity.class));
                finish();
                break;

            case R.id.homeButton:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.homeText:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
    }
}
