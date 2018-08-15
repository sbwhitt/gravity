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

public class GameView extends SurfaceView implements Runnable, View.OnClickListener {
    volatile boolean playing;
    private boolean gameOver = false;
    private boolean touch;
    private boolean paused = false;
    private boolean gameStart = false;

    private Intent intent;
    private Thread gameThread = null;

    private Player player;
    private int extraLives = 0;
    private Bitmap playerNoCollide;
    private Bitmap playerBitmap;
    private boolean speedSwitched = false;
    private Landmark startMark;

    private Obstacle[] obstacleWave;
    private int obstacleWaveNum = 3;
    private Landmark mark1;

    private Obstacle[] obstacleWave2;
    private int obstacleWave2Num = 3;
    private Landmark mark2;

    private Obstacle[] obstacleWave3;
    private int obstacleWave3Num = 3;
    private Landmark mark3;

    private Obstacle[] obstacleWave4;
    private int obstacleWave4Num = 3;
    private Landmark mark4;

    private boolean[] passed = {false, false, false, false};
    private Obstacle[][] waves = new Obstacle[4][];
    private Landmark[] landmarks = new Landmark[4];

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

    private Power power;
    private Mode mode;
    private String currentPowerStr;
    private String currentModeStr;

    private Paint paint;
    private Paint textPaint;
    private Paint largeTextPaint;
    private Paint startPaint;
    private Paint powerPaint;
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

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        intent = new Intent(context, EndActivity.class);

        player = new Player(context);
        playerBitmap = player.getBitmap();

        SharedPreferences gameStats = context.getSharedPreferences("gameStats", 0);
        currentPowerStr = gameStats.getString("power", Power.gravOff.name());
        power = Power.valueOf(currentPowerStr);
        if (power == Power.extraLife) extraLives = 1;
        mode = Mode.valueOf(gameStats.getString("mode", Mode.SURVIVAL.name()));

        startMark = new Landmark(getScreenHeight(), 32);

        if (mode == Mode.SURVIVAL) {
            obstacleWave = new Obstacle[obstacleWaveNum];
            for (int i = 0; i < obstacleWaveNum; i++) {
                obstacleWave[i] = new Obstacle(context, (int) (getScreenWidth()), 8, 1);
            }
            mark1 = new Landmark(obstacleWave[0].getX() + obstacleWave[0].getBitmap().getWidth() - 1, 1);

            Obstacle baseOb = obstacleWave[0];
            int obsWidth = baseOb.getBitmap().getWidth();

            obstacleWave2 = new Obstacle[obstacleWave2Num];
            for (int i = 0; i < obstacleWave2Num; i++) {
                obstacleWave2[i] = new Obstacle(context, (int) (getScreenWidth() + obsWidth * 3), 8, -1);
            }
            mark2 = new Landmark(obstacleWave2[0].getX() + obstacleWave2[0].getBitmap().getWidth() - 1, 1);

            obstacleWave3 = new Obstacle[obstacleWave3Num];
            for (int i = 0; i < obstacleWave3Num; i++) {
                obstacleWave3[i] = new Obstacle(context, (int) (getScreenWidth() + obsWidth * 6), 8, 1);
            }
            mark3 = new Landmark(obstacleWave3[0].getX() + obstacleWave3[0].getBitmap().getWidth() - 1, 1);

            obstacleWave4 = new Obstacle[obstacleWave4Num];
            for (int i = 0; i < obstacleWave4Num; i++) {
                obstacleWave4[i] = new Obstacle(context, (int) (getScreenWidth() + obsWidth * 9), 8, -1);
            }
            mark4 = new Landmark(obstacleWave4[0].getX() + obstacleWave4[0].getBitmap().getWidth() - 1, 1);

            waves[0] = obstacleWave;
            waves[1] = obstacleWave2;
            waves[2] = obstacleWave3;
            waves[3] = obstacleWave4;
            landmarks[0] = mark1;
            landmarks[1] = mark2;
            landmarks[2] = mark3;
            landmarks[3] = mark4;
        }

        else if (mode == Mode.POINTS) {
            power = Power.gravOff;
            checkpoints = new Checkpoint[checkpointWaveNum];
            for (int i = 0; i < checkpointWaveNum; i++) {
                checkpoints[i] = new Checkpoint(context, (int) getScreenWidth() + i*256, 8, 0);
            }
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

        largeTextPaint = new Paint();
        largeTextPaint.setColor(Color.WHITE);
        largeTextPaint.setTextSize(80);

        startPaint = new Paint();
        startPaint.setColor(Color.GREEN);
        powerPaint = new Paint();

        powerButton = new CustomButton(128, 128);
        powerButton.setPosition(32, getScreenHeight()/2 - 64);
    }

    @Override
    public void run() {
        while(playing) {
            if (mode == Mode.SURVIVAL) {
                updateSurvival();
                if (!gameOver) {
                    drawSurvival();
                    control();
                }
            }
            if (mode == Mode.POINTS) {
                updatePoints();
                if (!gameOver) {
                    drawPoints();
                    control();
                }
            }
        }
        if (!gameOver & mode == Mode.SURVIVAL) drawSurvival();
        else if (!gameOver & mode == Mode.SURVIVAL) drawPoints();
    }

    ///////////////////////////////////////////
    //    //UPDATE FUNCTION FOR SURVIVAL MODE//
    ///////////////////////////////////////////
    private void updateSurvival() {
        int passScore = 50;
        int obsWidth = obstacleWave[0].getBitmap().getWidth();

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

        if (destroyed) {
            destroyWaves();
            destroyed = false;
        }
        else {
            //UPDATING OBSTACLE LOCATIONS AND CHECKING FOR COLLISION
            for (int i = 0; i < obstacleWaveNum; i++) {
                obstacleWave[i].update();
                if (obstacleWave[i].getX() <= 0) {
                    obstacleWave[i].setX(obstacleWave4[0].getX() + obsWidth * 3);
                    obstacleWave[i].setY(ran.nextInt(getScreenHeight()));
                }

                if (Rect.intersects(player.getHitbox(), obstacleWave[i].getHitbox()) & collision) {
                    if (extraLives > 0) {
                        extraLives -= 1;
                        collision = false;
                    }
                    else endGame();
                }
            }
            mark1.update(obstacleWave[0].getX() + obstacleWave[0].getBitmap().getWidth() - 1, 1);

            for (int i = 0; i < obstacleWave2Num; i++) {
                obstacleWave2[i].update();
                if (obstacleWave2[i].getX() <= 0) {
                    obstacleWave2[i].setX(obstacleWave[0].getX() + obsWidth * 3);
                    obstacleWave2[i].setY(ran.nextInt(getScreenHeight()));
                }

                if (Rect.intersects(player.getHitbox(), obstacleWave2[i].getHitbox()) & collision) {
                    if (extraLives > 0) {
                        extraLives -= 1;
                        collision = false;
                    }
                    else endGame();
                }
            }
            mark2.update(obstacleWave2[0].getX() + obstacleWave2[0].getBitmap().getWidth() - 1, 1);

            for (int i = 0; i < obstacleWave3Num; i++) {
                obstacleWave3[i].update();
                if (obstacleWave3[i].getX() <= 0) {
                    obstacleWave3[i].setX(obstacleWave2[0].getX() + obsWidth * 3);
                    obstacleWave3[i].setY(ran.nextInt(getScreenHeight()));
                }

                if (Rect.intersects(player.getHitbox(), obstacleWave3[i].getHitbox()) & collision) {
                    if (extraLives > 0) {
                        extraLives -= 1;
                        collision = false;
                    }
                    else endGame();
                }
            }
            mark3.update(obstacleWave3[0].getX() + obstacleWave3[0].getBitmap().getWidth() - 1, 1);

            for (int i = 0; i < obstacleWave4Num; i++) {
                obstacleWave4[i].update();
                if (obstacleWave4[i].getX() <= 0) {
                    obstacleWave4[i].setX(obstacleWave3[0].getX() + obsWidth * 3);
                    obstacleWave4[i].setY(ran.nextInt(getScreenHeight()));
                }

                if (Rect.intersects(player.getHitbox(), obstacleWave4[i].getHitbox()) & collision) {
                    if (extraLives > 0) {
                        extraLives -= 1;
                        collision = false;
                    }
                    else endGame();
                }
            }
            mark4.update(obstacleWave4[0].getX() + obstacleWave4[0].getBitmap().getWidth() - 1, 1);
            //END
        }

        //adding points when player crosses obstacle waves
        if (mark1.getRect().intersect(player.getHitbox()) && !passed[0]) {
            score += passScore;
            passed[0] = true;
        }
        else if (!mark1.getRect().intersect(player.getHitbox())) passed[0] = false;

        if (mark2.getRect().intersect(player.getHitbox()) && !passed[1]) {
            score += passScore;
            passed[1] = true;
        }
        else if (!mark2.getRect().intersect(player.getHitbox())) passed[1] = false;

        if (mark3.getRect().intersect(player.getHitbox()) && !passed[2]) {
            score += passScore;
            passed[2] = true;
        }
        else if (!mark3.getRect().intersect(player.getHitbox())) passed[2] = false;

        if (mark4.getRect().intersect(player.getHitbox()) && !passed[3]) {
            score += passScore;
            passed[3] = true;
        }
        else if (!mark4.getRect().intersect(player.getHitbox())) passed[3] = false;
        //end

        //INCREASING SPEED AND GRAVITY BASED ON SCORE
        if (score != 0 && score % 500 == 0 && !speedAdjusted) {
            speedAdjusted = true;
            for (int i = 0; i < waves.length; i++) {
                for (int j = 0; j < waves[i].length; j++) {
                    waves[i][j].setSpeed_x(waves[i][j].getSpeed_x() + 1);
                }
            }
            //player.setGravity(player.getGravity() + 0.06);
        }
        else if (score % 500 != 0 & speedAdjusted) {
            speedAdjusted = false;
        }
        //END
    }

    ///////////////////////////////////////////
    //    //UPDATE FUNCTION FOR POINTS MODE////
    ///////////////////////////////////////////
    private void updatePoints() {
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

    /////////////////////////////////
    //DRAW FUNCTION FOR SURVIVAL MODE
    /////////////////////////////////
    private void drawSurvival() {
        pauseStr = "paused";
        speedStr = "speed: " + String.valueOf(obstacleWave[0].getSpeed_x());
        scoreStr = "score: " + String.valueOf(score);
        currentPowerStr = "current power: " + power.name();
        currentModeStr = "current mode: " + mode.name();

        if (cooldown || !player.getGravStatus()) powerPaint.setColor(Color.GRAY);
        else powerPaint.setColor(Color.WHITE);

        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            //canvas.drawBitmap(background, 0, 0, paint);
            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawRect(player.getHitbox(), textPaint);
            /*
            canvas.drawRect(mark1.getRect(), textPaint);
            canvas.drawRect(mark2.getRect(), textPaint);
            canvas.drawRect(mark3.getRect(), textPaint);
            canvas.drawRect(mark4.getRect(), textPaint);
            */
            canvas.drawRect(startMark.getRect(), startPaint);

            for (int i = 0; i < obstacleWaveNum; i++) {
                canvas.drawBitmap(obstacleWave[i].getBitmap(), obstacleWave[i].getX(), obstacleWave[i].getY(), paint);
                //canvas.drawRect(obstacleWave[i].getHitbox(), textPaint);
            }

            for (int i = 0; i < obstacleWave2Num; i++) {
                canvas.drawBitmap(obstacleWave2[i].getBitmap(), obstacleWave2[i].getX(), obstacleWave2[i].getY(), paint);
                //canvas.drawRect(obstacleWave2[i].getHitbox(), textPaint);
            }

            for (int i = 0; i < obstacleWave3Num; i++) {
                canvas.drawBitmap(obstacleWave3[i].getBitmap(), obstacleWave3[i].getX(), obstacleWave3[i].getY(), paint);
                //canvas.drawRect(obstacleWave3[i].getHitbox(), textPaint);
            }

            for (int i = 0; i < obstacleWave4Num; i++) {
                canvas.drawBitmap(obstacleWave4[i].getBitmap(), obstacleWave4[i].getX(), obstacleWave4[i].getY(), paint);
                //canvas.drawRect(obstacleWave4[i].getHitbox(), textPaint);
            }
            canvas.drawBitmap(gravArrow, getScreenWidth()-196, 32, paint);
            canvas.drawText(currentPowerStr, getScreenWidth()/2-48, 48, textPaint);
            canvas.drawText(currentModeStr, getScreenWidth()/2-48, 96, textPaint);
            canvas.drawText(speedStr, getScreenWidth()-256, getScreenHeight()-48, textPaint);
            canvas.drawText(scoreStr, getScreenWidth()-256, getScreenHeight()-128, textPaint);
            if (!playing) {
                canvas.drawText(pauseStr, getScreenWidth()/2-largeTextPaint.getTextSize(), getScreenHeight()/2, largeTextPaint);
                pauseButton.setBackground(playBitmap);
            }
            else pauseButton.setBackground(pauseBitmap);
            pauseButton.drawBitmap(canvas);

            powerButton.drawRect(canvas, powerPaint);
            if (!collision) {
                player.setBitmap(playerNoCollide);
                powerTimer -= 1;
                canvas.drawText(String.valueOf((int)Math.ceil(powerTimer/60+1)), player.getX(), player.getY(), textPaint);
                if (powerTimer == 0) {
                    powerTimer = 180;
                    collision = true;
                    player.setBitmap(playerBitmap);
                }
            }
            if (cooldown) {
                cooldownTimer -= 1;
                canvas.drawText(String.valueOf((int)Math.ceil(cooldownTimer/60+1)),84, getScreenHeight()/2+16, textPaint);
                if (cooldownTimer == 0) {
                    cooldownTimer = 480;
                    cooldown = false;
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    ///////////////////////////////
    //DRAW FUNCTION FOR POINTS MODE
    ///////////////////////////////
    private void drawPoints() {
        pauseStr = "paused";
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
            if (!playing) {
                canvas.drawText(pauseStr, getScreenWidth()/2-largeTextPaint.getTextSize(), getScreenHeight()/2, largeTextPaint);
                pauseButton.setBackground(playBitmap);
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
            gameThread.sleep(17);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void destroyWaves() {
        int obsWidth = obstacleWave[0].getBitmap().getWidth();

        for (int i = 0; i < obstacleWaveNum; i++) {
            obstacleWave[i].update();
            obstacleWave[i].setX((int)(getScreenWidth()));
            obstacleWave[i].setY(ran.nextInt(getScreenHeight()));
        }
        mark1.update(obstacleWave[0].getX()+obstacleWave[0].getBitmap().getWidth()-1, 1);

        for (int i = 0; i < obstacleWave2Num; i++) {
            obstacleWave2[i].update();
            obstacleWave2[i].setX((int)(getScreenWidth()+obsWidth*3));
            obstacleWave2[i].setY(ran.nextInt(getScreenHeight()));
        }
        mark2.update(obstacleWave2[0].getX()+obstacleWave2[0].getBitmap().getWidth()-1, 1);

        for (int i = 0; i < obstacleWave3Num; i++) {
            obstacleWave3[i].update();
            obstacleWave3[i].setX((int)(getScreenWidth()+obsWidth*6));
            obstacleWave3[i].setY(ran.nextInt(getScreenHeight()));
        }
        mark3.update(obstacleWave3[0].getX()+obstacleWave3[0].getBitmap().getWidth()-1, 1);

        for (int i = 0; i < obstacleWave4Num; i++) {
            obstacleWave4[i].update();
            obstacleWave4[i].setX((int)(getScreenWidth()+obsWidth*9));
            obstacleWave4[i].setY(ran.nextInt(getScreenHeight()));
        }
        mark4.update(obstacleWave4[0].getX()+obstacleWave4[0].getBitmap().getWidth()-1, 1);
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
        intent.putExtra("final score", score);
        getContext().startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
