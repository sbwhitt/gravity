package com.example.sbw98.gravity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

public class PointsView extends SurfaceView implements Runnable, View.OnClickListener {
    volatile boolean playing;
    private boolean gameOver = false;
    private boolean touch;
    private boolean paused = false;
    private boolean gameStart = false;

    private Intent homeIntent;
    private Intent gameIntent;
    private Intent endIntent;
    private Thread gameThread = null;

    private Player player;
    private int extraLives = 0;
    private Bitmap playerNoCollide;
    private Bitmap playerBitmap;
    private boolean speedSwitched = false;
    private Landmark startMark;

    private Checkpoint[] checkpoints;
    private int checkpointWaveNum = 4;
    private boolean[] hit = {false, false, false, false};

    private Bitmap gravArrow;
    private Bitmap pauseBitmap;
    private Bitmap playBitmap;
    private CustomButton pauseButton;

    private String pauseStr;
    private String speedStr;
    private String scoreStr;

    private CustomButton quitButton;
    private CustomButton restartButton;
    private String quitStr;
    private String restartStr;

    private Power power;
    private Mode mode;
    private String currentPowerStr;
    private String currentModeStr;

    private Paint paint;
    private Paint textPaint;
    private Paint largeTextPaint;
    private Paint startPaint;
    private Paint powerPaint;
    private Paint blackTextPaint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Random ran = new Random();

    private int score = 0;
    private boolean speedAdjusted = false;

    private CustomButton powerButton;
    private boolean cooldown = false;
    private int cooldownTimer = 480;
    private boolean collision = true;
    private int powerTimer = 180;
    private boolean destroyed = false;

    private int pointsTimer = 1800;
    private String pointsTimerStr;

    public PointsView(Context context) {
        super(context);
        init(context);
    }

    public PointsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PointsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        homeIntent = new Intent(context, MainActivity.class);
        gameIntent = new Intent(context, GameActivity.class);
        endIntent = new Intent(context, EndActivity.class);

        player = new Player(context);
        playerBitmap = player.getBitmap();

        SharedPreferences gameStats = context.getSharedPreferences("gameStats", 0);
        currentPowerStr = gameStats.getString("power", Power.gravOff.name());
        power = Power.valueOf(currentPowerStr);
        mode = Mode.valueOf(gameStats.getString("mode", Mode.SURVIVAL.name()));

        startMark = new Landmark(getScreenHeight(), 32);

        power = Power.gravOff;
        checkpoints = new Checkpoint[checkpointWaveNum];
        for (int i = 0; i < checkpointWaveNum; i++) {
            checkpoints[i] = new Checkpoint(context, (int) getScreenWidth() + i*256, 8, 0);
        }

        gravArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrowdown);
        pauseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        playBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.play);
        playerNoCollide = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_no_collide);
        pauseButton = new CustomButton(pauseBitmap);
        pauseButton.setPosition(32, 32);

        surfaceHolder = getHolder();

        paint = new Paint();
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);

        blackTextPaint = new Paint();
        blackTextPaint.setColor(Color.BLACK);
        blackTextPaint.setTextSize(40);

        largeTextPaint = new Paint();
        largeTextPaint.setColor(Color.WHITE);
        largeTextPaint.setTextSize(80);

        startPaint = new Paint();
        startPaint.setColor(Color.GREEN);
        powerPaint = new Paint();

        powerButton = new CustomButton(128, 128);
        powerButton.setPosition(32, getScreenHeight()/2 - 64);

        quitButton = new CustomButton(256, 128);
        quitButton.setPosition(getScreenWidth()/2-largeTextPaint.getTextSize()*4, getScreenHeight()/2+128);
        restartButton = new CustomButton(256, 128);
        restartButton.setPosition(getScreenWidth()/2+largeTextPaint.getTextSize()*2, getScreenHeight()/2+128);
    }

    @Override
    public void run() {
        while(playing) {
            update();
            if (!gameOver) {
                draw();
                control();
            }
        }
        if (!gameOver) draw();
        //else if (!gameOver && mode == Mode.SURVIVAL) drawPoints();
    }

    ///////////////////////////////////////////
    //    //UPDATE FUNCTION FOR POINTS MODE////
    ///////////////////////////////////////////
    private void update() {
        int checkpointScore = 50;
        int checkpointWidth = checkpoints[0].getBitmap().getWidth();

        //updating player and checking for ceiling and floor collision
        player.update();

        if (player.getY() > getScreenHeight()-72 & gameStart & !speedSwitched) {
            //endGame();
            player.setY(player.getY()-8);
            player.setSpeed_y(-player.getSpeed_y()/2);
            speedSwitched = true;
        }
        else if (player.getY() < 8 & gameStart & !speedSwitched) {
            //endGame();
            player.setY(player.getY()+8);
            player.setSpeed_y(-player.getSpeed_y()/2);
            speedSwitched = true;
        }
        else if (player.getY() < getScreenHeight()-96 || player.getY() > 24) speedSwitched = false;
        //System.out.println(player.getSpeed_y());
        //end

        if (startMark.getStart() >= -32) {
            startMark.update(startMark.getStart()-8, 32);
        }
        if (startMark.getRect().intersect(player.getHitbox())) {
            gameStart = true;
            player.toggleGravityOn();
        }

        //UPDATING CHECKPOINT LOCATIONS AND CHECKING FOR COLLISION
        for (int i = 0; i < checkpointWaveNum; i++) {
            checkpoints[i].update();
            if (checkpoints[i].getX() <= 0) {
                checkpoints[i].setX(getScreenWidth() + checkpointWidth*i);
                checkpoints[i].setY(ran.nextInt(getScreenHeight()-256)+128);
            }

            if (Rect.intersects(player.getHitbox(), checkpoints[i].getHitbox()) && !hit[i]) {
                score += checkpointScore;
                hit[i] = true;
            }
            else if (!Rect.intersects(player.getHitbox(), checkpoints[i].getHitbox())) hit[i] = false;
        }
        //END

        //INCREASING SPEED BASED ON SCORE
        if (score != 0 && score % 200 == 0 && !speedAdjusted) {
            speedAdjusted = true;
            for (int i = 0; i < checkpointWaveNum; i++) {
                checkpoints[i].setSpeed_x(checkpoints[i].getSpeed_x() + 1);
            }
        }
        else if (score % 200 != 0 & speedAdjusted) {
            speedAdjusted = false;
        }
        //END
    }

    ///////////////////////////////
    //DRAW FUNCTION FOR POINTS MODE
    ///////////////////////////////
    private void draw() {
        pauseStr = "paused";
        quitStr = "quit";
        restartStr = "restart";

        speedStr = "speed: " + String.valueOf(checkpoints[0].getSpeed_x());
        scoreStr = "score: " + String.valueOf(score);
        currentPowerStr = "current power: " + power.name();
        currentModeStr = "current mode: " + mode.name();
        pointsTimerStr = "time left: " + String.valueOf((int)Math.ceil(pointsTimer/60+1));

        if (cooldown || !player.getGravStatus()) powerPaint.setColor(Color.GRAY);
        else powerPaint.setColor(Color.WHITE);

        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);

            canvas.drawRect(startMark.getRect(), startPaint);

            for (int i = 0; i < checkpointWaveNum; i++) {
                canvas.drawBitmap(checkpoints[i].getBitmap(), checkpoints[i].getX(), checkpoints[i].getY(), paint);
            }

            canvas.drawBitmap(gravArrow, getScreenWidth()-196, 32, paint);
            canvas.drawText(currentPowerStr, getScreenWidth()/2-48, 48, textPaint);
            canvas.drawText(currentModeStr, getScreenWidth()/2-48, 96, textPaint);
            canvas.drawText(pointsTimerStr, getScreenWidth()/2-48, 144, textPaint);
            canvas.drawText(speedStr, getScreenWidth()-256, getScreenHeight()-48, textPaint);
            canvas.drawText(scoreStr, getScreenWidth()-256, getScreenHeight()-128, textPaint);
            if (paused) {
                canvas.drawText(pauseStr, getScreenWidth()/2-largeTextPaint.getTextSize(), getScreenHeight()/2, largeTextPaint);
                pauseButton.setBackground(playBitmap);

                quitButton.drawRect(canvas, textPaint);
                canvas.drawText(quitStr, quitButton.getX()+quitButton.width/2, quitButton.getY()+quitButton.height/2, blackTextPaint);
                restartButton.drawRect(canvas, textPaint);
                canvas.drawText(restartStr, restartButton.getX()+restartButton.width/2, restartButton.getY()+restartButton.height/2, blackTextPaint);
            }
            else pauseButton.setBackground(pauseBitmap);
            pauseButton.drawBitmap(canvas);

            if (gameStart) pointsTimer -= 1;
            if (pointsTimer == -1) {
                endGame();
            }
            powerButton.drawRect(canvas, powerPaint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(8);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        }
        catch(InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void endGame() {
        //String timeUp = "time's up";
        //String fail = "you failed";
        gameOver = true;
        endIntent.putExtra("final score", score);
        getContext().startActivity(endIntent);
        endIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        /*pauseBitmap.recycle();
        gravArrow.recycle();
        player.getBitmap().recycle();
        for (int i = 0; i < waves.length; i++) {
            for (int j = 0; j < waves[i].length; j++) {
                waves[i][j].getBitmap().recycle();
            }
        }*/
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (!touch) {
                    touch = true;
                    if (pauseButton.buttonRect.contains(x, y) & !paused & gameStart) {
                        paused = true;
                        pause();
                    }
                    else if (pauseButton.buttonRect.contains(x, y) & paused & gameStart) {
                        paused = false;
                        resume();
                    }
                    else if (!paused & gameStart) {
                        if (powerButton.buttonRect.contains(x, y) & power == Power.gravOff) {
                            if (player.getGravStatus()) {
                                player.toggleGravityOff();
                                player.setSpeed_y(0);
                            }
                            else if (!player.getGravStatus()) {
                                player.toggleGravityOn();
                            }
                        }
                        else if (powerButton.buttonRect.contains(x, y) & collision & power == Power.collideOff & !cooldown) {
                            collision = false;
                            cooldown = true;
                        }
                        else if (powerButton.buttonRect.contains(x, y) & power == Power.destroyWaves & !cooldown) {
                            destroyed = true;
                            cooldown = true;
                        }
                        else if (!powerButton.buttonRect.contains(x, y)) {
                            player.switchGravity();
                            if (player.getGravity() > 0) gravArrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrowdown);
                            else gravArrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrowup);
                        }
                    }

                    else if (paused & gameStart) {
                        if (quitButton.buttonRect.contains(x, y)) {
                            gameOver = true;
                            getContext().startActivity(homeIntent);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        }
                        else if (restartButton.buttonRect.contains(x, y)) {
                            gameOver = true;
                            getContext().startActivity(gameIntent);
                            gameIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                touch = false;
                break;
        }
        return true;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public CustomButton getPauseButton() {
        return pauseButton;
    }
}
