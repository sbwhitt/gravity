package com.example.sbw98.gravity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean gravOff = true;
    private boolean collideOff = false;
    private boolean destroyWaves = false;
    private boolean extraLife = false;
    private String powerStr = "grav";
    private TextView powerText;

    private boolean survival = true;
    private boolean points = false;
    private boolean swipe = false;
    private String modeStr = "survive";
    private TextView modeText;

    SharedPreferences gameStats;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context context = getApplicationContext();

        ImageButton buttonPlay;
        TextView playText;
        ImageButton powerButton;
        ImageButton modeButton;

        TextView levelText;
        TextView expText;
        TextView highScoreText;

        gameStats = context.getSharedPreferences("gameStats", 0);
        editor = gameStats.edit();

        int level = gameStats.getInt("level", 1);
        int experience = gameStats.getInt("experience", 0);
        int highScore = gameStats.getInt("highScore", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        playText = (TextView) findViewById(R.id.playText);

        powerButton = (ImageButton) findViewById(R.id.powerButton);
        powerText = (TextView) findViewById(R.id.powerText);
        powerText.setText(powerStr);

        modeButton = (ImageButton) findViewById(R.id.modeButton);
        modeText = (TextView) findViewById(R.id.modeText);
        modeText.setText(modeStr);

        levelText = (TextView) findViewById(R.id.levelText);
        String levelStr = "level: " + String.valueOf(level);
        levelText.setText(levelStr);

        expText = (TextView) findViewById(R.id.expText);
        String expStr = "experience: " + String.valueOf(experience);
        expText.setText(expStr);

        highScoreText = (TextView) findViewById(R.id.highScoreText);
        String highScoreStr = "high score: " + String.valueOf(highScore);
        highScoreText.setText(highScoreStr);

        buttonPlay.setOnClickListener(this);
        playText.setOnClickListener(this);
        powerButton.setOnClickListener(this);
        powerText.setOnClickListener(this);
        modeButton.setOnClickListener(this);
        modeText.setOnClickListener(this);

        editor.apply();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonPlay:
                applyPower();
                applyMode();
                startActivity(new Intent(this, GameActivity.class));
                break;
            case R.id.playText:
                applyPower();
                applyMode();
                startActivity(new Intent(this, GameActivity.class));
                break;

            case R.id.powerButton:
                switchPower();
                break;
            case R.id.powerText:
                switchPower();
                break;

            case R.id.modeButton:
                switchMode();
                break;
            case R.id.modeText:
                switchMode();
                break;
        }
    }

    private void switchPower() {
        if (survival) {
            if (gravOff) {
                powerStr = "collide";
                collideOff = true;
                gravOff = false;
                powerText.setText(powerStr);
            } else if (collideOff) {
                powerStr = "destroy";
                destroyWaves = true;
                collideOff = false;
                powerText.setText(powerStr);
            } else if (destroyWaves) {
                powerStr = "life";
                extraLife = true;
                destroyWaves = false;
                powerText.setText(powerStr);
            } else if (extraLife) {
                powerStr = "grav";
                gravOff = true;
                extraLife = false;
                powerText.setText(powerStr);
            }
        }
    }

    private void switchMode() {
        if (survival) {
            modeStr = "points";
            resetPower();
            points = true;
            survival = false;
            modeText.setText(modeStr);
        }
        else if (points) {
            modeStr = "swipe";
            resetPower();
            swipe = true;
            points = false;
            modeText.setText(modeStr);
        }
        else if (swipe) {
            modeStr = "survive";
            resetPower();
            survival = true;
            swipe = false;
            modeText.setText(modeStr);
        }
    }

    private void applyPower() {
        if (gravOff) {
            editor.putString("power", Power.gravOff.name());
            editor.apply();
        }
        else if (collideOff) {
            editor.putString("power", Power.collideOff.name());
            editor.apply();
        }
        else if (destroyWaves) {
            editor.putString("power", Power.destroyWaves.name());
            editor.apply();
        }
        else if (extraLife) {
            editor.putString("power", Power.extraLife.name());
            editor.apply();
        }
    }

    private void applyMode() {
        if (survival) {
            editor.putString("mode", Mode.SURVIVAL.name());
            editor.apply();
        }
        else if (points) {
            editor.putString("mode", Mode.POINTS.name());
            editor.apply();
        }
        else if (swipe) {
            editor.putString("mode", Mode.SWIPE.name());
            editor.apply();
        }
    }

    private void resetPower() {
        powerStr = "grav";
        gravOff = true;
        collideOff = false;
        destroyWaves = false;
        extraLife = false;
        powerText.setText(powerStr);
    }
}
