package com.example.sbw98.gravity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

public class Checkpoint {
    private Random ran = new Random();

    private Bitmap bitmap;
    private Rect hitBox;

    private int x;
    private int y;

    private int screenHeight = GameView.getScreenHeight();
    private int screenWidth = GameView.getScreenWidth();

    private int speed_x;
    private int speed_y;

    public Checkpoint(Context context, int start_x, int speed_x, int speed_y) {
        x = start_x;
        y = ran.nextInt(screenHeight-256)+128;

        this.speed_x = speed_x;
        this.speed_y = speed_y;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.checkpoint);

        hitBox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public void update() {
        x -= speed_x;
        y += speed_y;

        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        hitBox.set(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public int getX() {
        return x;
    }

    public void setX(int new_x) {
        x = new_x;
    }

    public int getY() {
        return y;
    }

    public void setY(int new_y) {
        y = new_y;
    }

    public Rect getHitbox() {
        return hitBox;
    }

    public int getSpeed_x() {
        return speed_x;
    }

    public void setSpeed_x(int speed_x) {
        this.speed_x = speed_x;
    }

    public int getSpeed_y() {
        return speed_y;
    }

    public void setSpeed_y(int speed_y) {
        this.speed_y = speed_y;
    }
}
