package com.example.sbw98.gravity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.Rect;
import java.lang.Math;

public class Player {
    private Bitmap bitmap;
    private Rect hitBox;

    private int x;
    private int y;

    private int screenHeight = GameView.getScreenHeight();
    private int screenWidth = GameView.getScreenWidth();

    private double speed_x;
    private double speed_y;
    private double gravity;
    private boolean gravStatus = false;

    private Path playerPath = new Path();

    public Player(Context context) {
        gravity = 0.15;
        x = screenHeight / 3;
        if (screenHeight < screenWidth) y = screenHeight/2;
        else if (screenWidth < screenHeight) y = screenWidth/2;
        speed_x = 0;
        speed_y = 0;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);

        hitBox = new Rect(x, y, bitmap.getWidth()-8, bitmap.getHeight()-8);
    }

    public void update() {
        if (gravStatus) speed_y += gravity;
        x += Math.round(speed_x);
        y += Math.round(speed_y);

        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();
    }

    public void moveUp() {

    }

    public void moveDown() {

    }

    public void moveRight() {

    }

    public void moveLeft() {

    }

    public void addToPath(int px, int py) {
        playerPath.lineTo(px, py);
    }

    public Path getPlayerPath() {
        return playerPath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Rect getHitbox() {
        return hitBox;
    }

    public void switchGravity() {
        gravity = -gravity;
    }

    public void toggleGravityOn() {
        gravStatus = true;
    }

    public void toggleGravityOff() {
        gravStatus = false;
    }

    public boolean getGravStatus() {
        return gravStatus;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getSpeed_x() {
        return speed_x;
    }

    public void setSpeed_x(double speed_x) {
        this.speed_x = speed_x;
    }

    public void setSpeed_y(double speed) {
        speed_y = speed;
    }

    public double getSpeed_y() {
        return speed_y;
    }
}
