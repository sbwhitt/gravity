package com.example.sbw98.gravity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class CustomButton {
    public Matrix buttonMatrix = new Matrix();

    public RectF buttonRect;

    float width;
    float height;
    Bitmap background;

    public CustomButton(Bitmap background) {
        this.width = background.getWidth();
        this.height = background.getHeight();
        this.background = background;

        buttonRect = new RectF(0, 0, width, height);
    }

    public CustomButton(int width, int height) {
        this.width = width;
        this.height = height;

        buttonRect = new RectF(0, 0, width, height);
    }

    public void setPosition(float x, float y) {
        buttonMatrix.setTranslate(x, y);
        buttonMatrix.mapRect(buttonRect);
    }
    public void setBackground(Bitmap bitmap) {
        background = bitmap;
    }

    public void drawBitmap(Canvas canvas) {
        canvas.drawBitmap(background, buttonMatrix, null);
    }

    public void drawRect(Canvas canvas, Paint paint) {
        canvas.drawRect(buttonRect, paint);
    }
}
