package com.example.sbw98.gravity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

enum Power {
    gravOff, collideOff, destroyWaves, extraLife
}

enum Mode {
    SURVIVAL, POINTS, SWIPE, EASY, MEDIUM, HARD
}

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private GameView gameView;
    private SwipeView swipeView;

    private boolean gameViewRunning;
    private boolean swipeViewRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences gameStats = getApplicationContext().getSharedPreferences("gameStats", 0);
        String modeStr = gameStats.getString("mode", Mode.SURVIVAL.name());
        Mode mode = Mode.valueOf(modeStr);

        if (mode == Mode.SURVIVAL || mode == Mode.POINTS) {
            gameViewRunning = true;
            swipeViewRunning = false;
            gameView = new GameView(this);
            setContentView(gameView);
        }
        else if (mode == Mode.SWIPE) {
            swipeViewRunning = true;
            gameViewRunning = false;
            swipeView = new SwipeView(this);

            swipeView.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeDown() {
                    swipeView.onSwipeDown();
                }

                @Override
                public void onSwipeLeft() {
                    swipeView.onSwipeLeft();
                }

                @Override
                public void onSwipeUp() {
                    swipeView.onSwipeUp();
                }

                @Override
                public void onSwipeRight() {
                    swipeView.onSwipeRight();
                }
            });

            setContentView(swipeView);
        }

        if (Build.VERSION.SDK_INT >= 21) getWindow().setExitTransition(null);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameViewRunning) gameView.pause();
        else if (swipeViewRunning) swipeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameViewRunning) gameView.resume();
        else if (swipeViewRunning) swipeView.resume();
    }

    @Override
    public void onClick(View v) {
    }
}
