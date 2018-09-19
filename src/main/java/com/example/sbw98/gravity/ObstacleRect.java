package com.example.sbw98.gravity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

public class ObstacleRect {
    private Random ran = new Random();

    private Rect hitBox;
    private Bitmap bitmap;

    private int x;
    private int y;
    private int width;
    private int height;

    private int screenHeight = SurvivalView.getScreenHeight();
    private int screenWidth = SurvivalView.getScreenWidth();

    private int speedX;

    public ObstacleRect(Context context, int startX, int startY, int speedX) {
        this.x = startX;
        this.y = startY;
        this.speedX = speedX;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.obstaclebig);
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        hitBox = new Rect(x, y, width, height);
    }

    public void update() {
        x -= speedX;

        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    public void drawRect(Canvas canvas, Paint paint) {
        canvas.drawRect(hitBox, paint);
    }

    public float getX() {
        return x;
    }

    public void setX(int new_x) {
        x = new_x;
    }

    public float getY() {
        return y;
    }

    public void setY(int new_y) {
        y = new_y;
    }

    public int getSpeedX() {
        return speedX;
    }

    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rect getHitBox() {
        return hitBox;
    }

    public void placeTop() {
        setPosition((int)(screenWidth), 0);
    }

    public void placeBottom() {
        setPosition((int)(screenWidth), (int)(screenHeight-height));
    }

    public void placeMiddle() {
        setPosition((int)(screenWidth), (int)(screenHeight/2-height/2));
    }
}
