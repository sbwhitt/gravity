package com.example.sbw98.gravity;

import android.content.Context;
import android.content.Intent;
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

public class SwipeView extends SurfaceView implements Runnable, View.OnClickListener {
    volatile boolean playing;
    private boolean gameOver = false;
    private boolean paused = false;
    private Thread gameThread = null;

    private Intent homeIntent;
    private Intent gameIntent;
    private Intent endIntent;

    private Player player;

    private final int numObstacles = 6;
    private ObstacleRect[] obstacles;

    //private Landmark mark1;
    //private Landmark mark2;
    //private Landmark mark3;

    //private boolean[] passed = {false, false, false};

    private int obWidth;
    private int obHeight;
    private Random ran = new Random();

    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Paint textPaint;
    private Paint largeTextPaint;
    private Paint blackTextPaint;

    private boolean touch = false;
    private boolean swipeDown = false;
    private boolean swipeUp = false;
    private boolean swipeRight = false;
    private boolean swipeLeft = false;

    private final int SWIPE_TIMER = 30;
    private int swipeTimer = SWIPE_TIMER;

    private boolean speedSwitchedX = false;
    private boolean speedSwitchedY = false;

    //private final int passScore = 100;
    private int score;
    private boolean speedAdjusted = false;

    private String scoreStr;
    private String speedStr;

    private Bitmap pauseBitmap;
    private Bitmap playBitmap;
    private String pauseStr;
    private CustomButton pauseButton;

    private CustomButton quitButton;
    private CustomButton restartButton;
    private String quitStr;
    private String restartStr;

    public SwipeView(Context context) {
        super(context);
        init(context);
    }

    public SwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        homeIntent = new Intent(context, MainActivity.class);
        gameIntent = new Intent(context, GameActivity.class);
        endIntent = new Intent(context, EndActivity.class);

        player = new Player(context);

        obstacles = new ObstacleRect[numObstacles];

        for (int i = 0; i < numObstacles; i++) {
            obstacles[i] = new ObstacleRect(context, 0, 0, 13);
        }
        obWidth = obstacles[0].getWidth();
        obHeight = obstacles[0].getHeight();
        obstacles[0].setPosition( (int)(getScreenWidth()), ran.nextInt(getScreenHeight()-obHeight) );
        obstacles[1].setPosition( (int)(getScreenWidth()), ran.nextInt(getScreenHeight()-obHeight) );
        //mark1 = new Landmark((int)(obstacles[0].getX()+obWidth-1), 1);

        obstacles[2].setPosition( (int)(getScreenWidth()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
        obstacles[3].setPosition( (int)(getScreenWidth()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
        //mark2 = new Landmark((int)(obstacles[2].getX()+obWidth-1), 1);

        obstacles[4].setPosition( (int)(getScreenWidth()+obWidth*4), ran.nextInt(getScreenHeight()-obHeight) );
        obstacles[5].setPosition( (int)(getScreenWidth()+obWidth*4), ran.nextInt(getScreenHeight()-obHeight) );
        //mark3 = new Landmark((int)(obstacles[4].getX()+obWidth-1), 1);

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

        pauseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        playBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.play);
        pauseButton = new CustomButton(pauseBitmap);
        pauseButton.setPosition(32, 32);

        quitButton = new CustomButton(256, 128);
        quitButton.setPosition(getScreenWidth()/2-largeTextPaint.getTextSize()*4, getScreenHeight()/2+128);
        restartButton = new CustomButton(256, 128);
        restartButton.setPosition(getScreenWidth()/2+largeTextPaint.getTextSize()*2, getScreenHeight()/2+128);
    }

    @Override
    public void run() {
        while (playing) {
            update();
            if (!gameOver) {
                draw();
                control();
            }
        }
        draw();
    }

    private void update() {
        //updating player and checking for ceiling and floor collision
        player.update();
        //for x
        if (player.getX() > getScreenWidth()-72 & !speedSwitchedX) {
            player.setX(player.getX()-8);
            player.setSpeed_x(0);
            speedSwitchedX = true;
        }
        else if (player.getX() < 8 & !speedSwitchedX) {
            player.setX(player.getX()+8);
            player.setSpeed_x(0);
            speedSwitchedX = true;
        }
        else if (player.getX() < getScreenWidth()-96 || player.getX() > 24) speedSwitchedX = false;
        //for y
        if (player.getY() > getScreenHeight()-72 & !speedSwitchedY) {
            player.setY(player.getY()-8);
            player.setSpeed_y(0);
            speedSwitchedY = true;
        }
        else if (player.getY() < 8 & !speedSwitchedY) {
            player.setY(player.getY()+8);
            player.setSpeed_y(0);
            speedSwitchedY = true;
        }
        else if (player.getY() < getScreenHeight()-96 || player.getY() > 24) speedSwitchedY = false;
        //end

        //updating obstacles/landmarks
        if (obstacles[0].getX() <= -obWidth) {
            obstacles[0].setPosition( (int)(obstacles[4].getX()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
            obstacles[1].setPosition( (int)(obstacles[5].getX()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
            //passed[0] = false;
            System.out.println("first moved");
        }
        else if (obstacles[2].getX() <= -obWidth) {
            obstacles[2].setPosition( (int)(obstacles[0].getX()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
            obstacles[3].setPosition( (int)(obstacles[1].getX()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
            //passed[1] = false;
            System.out.println("second moved");
        }
        else if (obstacles[4].getX() <= -obWidth) {
            obstacles[4].setPosition( (int)(obstacles[2].getX()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
            obstacles[5].setPosition( (int)(obstacles[3].getX()+obWidth*2), ran.nextInt(getScreenHeight()-obHeight) );
            //passed[2] = false;
            System.out.println("third moved");
        }
        //checking player/obstacle collision
        for (ObstacleRect obstacle : obstacles) {
            if (Rect.intersects(player.getHitbox(), obstacle.getHitBox())) {
                endGame();
            }
        }
        //updating obstacle positions
        for (ObstacleRect obstacle : obstacles) {
            obstacle.update();
        }
        //updating landmark positions
        //mark1.update((int)(obstacles[0].getX()+obWidth-1), 1);
        //mark2.update((int)(obstacles[2].getX()+obWidth-1), 1);
        //mark3.update((int)(obstacles[4].getX()+obWidth-1), 1);
        //checking landmark collisions
        /*if (Rect.intersects(player.getHitbox(), mark1.getRect()) & !passed[0]) {
            passed[0] = true;
            score += passScore;
        }
        //else if (!Rect.intersects(player.getHitbox(), mark1.getRect())) passed[0] = false;
        if (Rect.intersects(player.getHitbox(), mark2.getRect()) & !passed[1]) {
            passed[1] = true;
            score += passScore;
        }
        //else if (!Rect.intersects(player.getHitbox(), mark2.getRect())) passed[1] = false;
        if (Rect.intersects(player.getHitbox(), mark3.getRect()) & !passed[2]) {
            passed[2] = true;
            score += passScore;
        }
        //else if (!Rect.intersects(player.getHitbox(), mark3.getRect())) passed[2] = false;
        //end*/

        score++;

        //handling swipes
        if (swipeUp & !paused) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeUp = false;
                player.setSpeed_y(0.0);
            }
        }
        else if (swipeDown & !paused) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeDown = false;
                player.setSpeed_y(0.0);
            }
        }
        else if (swipeRight & !paused) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeRight = false;
                player.setSpeed_x(0.0);
            }
        }
        else if (swipeLeft & !paused) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeLeft = false;
                player.setSpeed_x(0.0);
            }
        }
        //end

        //INCREASING SPEED AND GRAVITY BASED ON SCORE
        if (score != 0 && score % 500 == 0 && !speedAdjusted) {
            speedAdjusted = true;
            for (ObstacleRect obstacle : obstacles) {
                obstacle.setSpeedX(obstacle.getSpeedX() + 1);
            }
            //player.setGravity(player.getGravity() + 0.06);
        }
        else if (score % 500 != 0 & speedAdjusted) {
            speedAdjusted = false;
        }
        //END
    }

    //draw function
    private void draw() {
        pauseStr = "paused";
        quitStr = "quit";
        restartStr = "restart";

        speedStr = "speed: " + String.valueOf(obstacles[0].getSpeedX());
        scoreStr = "score: " + String.valueOf(score);
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawRect(player.getHitbox(), textPaint);

            for (ObstacleRect obstacle : obstacles) {
                obstacle.draw(canvas, paint);
            }

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

            canvas.drawText(speedStr, getScreenWidth()-256, getScreenHeight()-48, textPaint);
            canvas.drawText(scoreStr, getScreenWidth()-256, getScreenHeight()-128, textPaint);

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
        gameOver = true;
        endIntent.putExtra("final score", score);
        getContext().startActivity(endIntent);
        endIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        //performClick();

        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (true) {
                    touch = true;

                    if (pauseButton.buttonRect.contains(x, y) & !paused) {
                        paused = true;
                        pause();
                    }
                    else if (pauseButton.buttonRect.contains(x, y) & paused) {
                        paused = false;
                        resume();
                    }
                    else if (paused) {
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

    public void onSwipeUp() {
        if (!paused) {
            swipeTimer = SWIPE_TIMER;
            if (swipeUp) player.setSpeed_y(-40.0);
            else player.setSpeed_y(-20.0);
            player.setSpeed_x(0.0);
            swipeUp = true;
            swipeDown = false;
            swipeRight = false;
            swipeLeft = false;
        }
    }

    public void onSwipeDown() {
        if (!paused) {
            swipeTimer = SWIPE_TIMER;
            if (swipeDown) player.setSpeed_y(40.0);
            else player.setSpeed_y(20.0);
            player.setSpeed_x(0.0);
            swipeUp = false;
            swipeDown = true;
            swipeRight = false;
            swipeLeft = false;
        }
    }

    public void onSwipeRight() {
        if (!paused) {
            swipeTimer = SWIPE_TIMER;
            if (swipeRight) player.setSpeed_x(40.0);
            else player.setSpeed_x(20.0);
            player.setSpeed_y(0.0);
            swipeUp = false;
            swipeDown = false;
            swipeRight = true;
            swipeLeft = false;
        }
    }

    public void onSwipeLeft() {
        if (!paused) {
            swipeTimer = SWIPE_TIMER;
            if (swipeLeft) player.setSpeed_x(-40.0);
            else player.setSpeed_x(-20.0);
            player.setSpeed_y(0.0);
            swipeUp = false;
            swipeDown = false;
            swipeRight = false;
            swipeLeft = true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return false;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
