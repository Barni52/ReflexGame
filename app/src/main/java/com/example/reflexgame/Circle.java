package com.example.reflexgame;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle {
    private float x, y;
    private float radius;
    private float shrinkRate; // pixels per frame

    public Circle(float x, float y, float initialRadius, float shrinkRate) {
        this.x = x;
        this.y = y;
        this.radius = initialRadius;
        this.shrinkRate = shrinkRate;
    }

    public void update() {
        radius -= shrinkRate;
    }

    public boolean isDead() {
        return radius <= 0;
    }

    public boolean contains(float touchX, float touchY) {
        float dx = touchX - x;
        float dy = touchY - y;
        return dx*dx + dy*dy <= radius*radius;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawCircle(x, y, radius, paint);
    }
}
