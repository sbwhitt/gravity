package com.example.sbw98.gravity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ExpView extends SurfaceView implements Runnable {
    private Paint stroke;
    private Paint fill;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;

    private Rect expBarBG;
    private Rect expBarFG;

    public ExpView(Context context) {
        super(context);
        init(context);
    }

    public ExpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        stroke = new Paint();
        fill = new Paint();
        surfaceHolder = getHolder();

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(Color.WHITE);
        fill.setColor(Color.WHITE);
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            canvas.drawRect(expBarBG, stroke);
            canvas.drawRect(expBarFG, fill);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void createBar(float height, float width, int progress, int cap) {
        expBarBG = new Rect(0 ,0, 32, 128);
        expBarFG = new Rect(0 ,0, 32, 128*(progress/cap));
    }

    public void setPosition(int x, int y) {

    }

    public Rect getBG() {
        return expBarBG;
    }

    @Override
    public void run() {
        draw();
    }
}
