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

public class SwipeView extends SurfaceView implements Runnable, View.OnClickListener {
    volatile boolean playing;
    private boolean gameOver = false;
    private boolean paused = false;
    private Thread gameThread = null;
    private Intent intent;

    private Player player;

    private ObstacleRect obstacle1;
    private ObstacleRect obstacle2;
    private ObstacleRect obstacle3;
    private final int numObstacles = 3;
    private ObstacleRect[] obstacles;
    private Bitmap obBitmap;

    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Paint textPaint;

    private boolean touch = false;
    private boolean swipeDown = false;
    private boolean swipeUp = false;
    private boolean swipeRight = false;
    private boolean swipeLeft = false;

    private final int SWIPE_TIMER = 30;
    private int swipeTimer = SWIPE_TIMER;

    private boolean speedSwitchedX = false;
    private boolean speedSwitchedY = false;

    private Bitmap pauseBitmap;
    private Bitmap playBitmap;
    private CustomButton pauseButton;

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
        intent = new Intent(context, EndActivity.class);

        player = new Player(context);

        obstacles = new ObstacleRect[numObstacles];

        obstacle1 = new ObstacleRect(context, 0, 0, 13);
        obstacles[0] = obstacle1;
        obstacle1.setPosition((int)(getScreenWidth()-obstacle1.getWidth()), (int)(getScreenHeight()-obstacle1.getHeight()));

        obstacle2 = new ObstacleRect(context, 0, 0, 13);
        obstacles[1] = obstacle2;
        obstacle2.setPosition((int)(getScreenWidth()-obstacle2.getWidth()), 0);

        obstacle3 = new ObstacleRect(context, 0, 0, 13);
        obstacles[2] = obstacle3;
        obstacle3.setPosition((int)(getScreenWidth()+obstacle3.getWidth()), getScreenHeight()/2-obstacle3.getHeight()/2);

        surfaceHolder = getHolder();
        paint = new Paint();
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);

        pauseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        playBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.play);
        pauseButton = new CustomButton(pauseBitmap);
        pauseButton.setPosition(32, 32);
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

        //updating obstacles
        if (obstacle1.getX() <= -obstacle1.getWidth()) {
            obstacle1.setPosition((int)(getScreenWidth()), (int)(getScreenHeight()-obstacle1.getHeight()));
        }
        else if (obstacle2.getX() <= -obstacle2.getWidth()) {
            obstacle2.setPosition((int)(getScreenWidth()), 0);
        }
        else if (obstacle3.getX() <= -obstacle3.getWidth()) {
            obstacle3.setPosition((int)(getScreenWidth()), getScreenHeight()/2-obstacle3.getHeight()/2);
        }

        //checking player/obstacle collision
        for (ObstacleRect obstacle : obstacles) {
            if (Rect.intersects(player.getHitbox(), obstacle.getHitBox())) {
                endGame();
            }
        }

        //updating obstacle positions
        obstacle1.update();
        obstacle2.update();
        obstacle3.update();
        //end

        //handling swipes
        if (swipeUp) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeUp = false;
                player.setSpeed_y(0.0);
            }
        }
        else if (swipeDown) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeDown = false;
                player.setSpeed_y(0.0);
            }
        }
        else if (swipeRight) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeRight = false;
                player.setSpeed_x(0.0);
            }
        }
        else if (swipeLeft) {
            swipeTimer -= 1;
            if (swipeTimer == 0) {
                swipeTimer = SWIPE_TIMER;
                swipeLeft = false;
                player.setSpeed_x(0.0);
            }
        }
        //end

    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawRect(player.getHitbox(), textPaint);

            obstacle1.draw(canvas, textPaint);
            //obstacle1.drawRect(canvas, textPaint);
            obstacle2.draw(canvas, textPaint);
            //obstacle2.drawRect(canvas, textPaint);
            obstacle3.draw(canvas, textPaint);
            //obstacle3.drawRect(canvas, textPaint);

            if (!playing) pauseButton.setBackground(playBitmap);
            else pauseButton.setBackground(pauseBitmap);
            pauseButton.drawBitmap(canvas);

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
        //intent.putExtra("final score", score);
        getContext().startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                }
                break;
            case MotionEvent.ACTION_DOWN:
                touch = false;
                break;
        }
        System.out.println("touch: " + touch);
        System.out.println("paused: " + paused);
        System.out.println("playing: " + playing);
        return true;
    }

    public void onSwipeUp() {
        swipeTimer = SWIPE_TIMER;
        swipeUp = true;
        swipeDown = false;
        swipeRight = false;
        swipeLeft = false;
        player.setSpeed_y(-20.0);
        player.setSpeed_x(0.0);
    }

    public void onSwipeDown() {
        swipeTimer = SWIPE_TIMER;
        swipeUp = false;
        swipeDown = true;
        swipeRight = false;
        swipeLeft = false;
        player.setSpeed_y(20.0);
        player.setSpeed_x(0.0);
    }

    public void onSwipeRight() {
        swipeTimer = SWIPE_TIMER;
        swipeUp = false;
        swipeDown = false;
        swipeRight = true;
        swipeLeft = false;
        player.setSpeed_y(0.0);
        player.setSpeed_x(20.0);
    }

    public void onSwipeLeft() {
        swipeTimer = SWIPE_TIMER;
        swipeUp = false;
        swipeDown = false;
        swipeRight = false;
        swipeLeft = true;
        player.setSpeed_y(0.0);
        player.setSpeed_x(-20.0);
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
