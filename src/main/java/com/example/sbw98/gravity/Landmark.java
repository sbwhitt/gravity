package com.example.sbw98.gravity;

import android.graphics.Rect;

public class Landmark {
    private Rect marker;

    private int start;
    private int y = 0;

    private int screenHeight = SurvivalView.getScreenHeight();

    public Landmark(int start, int end) {
        this.start = start;

        marker = new Rect(this.start, y, this.start+end, screenHeight);
    }

    public void update(int start, int end) {
        this.start = start;
        marker.left = this.start;
        marker.top = 0;
        marker.right = this.start+end;
        marker.bottom = screenHeight;
    }

    public boolean hits(Player player) {
        return player.getHitbox().left == start;
    }

    public Rect getRect() {
        return marker;
    }

    public int getStart() {
        return start;
    }
}
