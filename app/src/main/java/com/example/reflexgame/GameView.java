package com.example.reflexgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private List<Circle> circles = new ArrayList<>();
    private Paint circlePaint;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int score = 0;
    private int lives = 3;
    private Random rnd = new Random();
    private boolean isGameOver = false;



    // Frame rate 60fps
    private final long FRAME_DELAY = 16;

    private GameActivity context;

    public GameView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        context = (GameActivity) ctx;
        // Initialize circle paint
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.RED);

        // Start the game loop
        handler.post(gameLoop);
        // Also: periodically spawn new circles
        handler.post(spawnLoop);
    }

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if(isGameOver) return;
            Iterator<Circle> it = circles.iterator();
            while (it.hasNext()) {
                Circle c = it.next();
                c.update();
                if (c.isDead()) {
                    context.vibrate();
                    it.remove();
                    lives--;
                    if(isDead()){
                        handler.removeCallbacksAndMessages(null);
                        isGameOver = true;
                        context.gameOver(score);
                    }
                }
            }
            invalidate();  // triggers onDraw()
            handler.postDelayed(this, FRAME_DELAY);
        }
    };

    private Runnable spawnLoop = new Runnable() {
        @Override
        public void run() {
            spawnCircle();
            // spawn every 1 second (customize)
            handler.postDelayed(this, 500);
        }
    };

    private void spawnCircle() {

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float radius = 150;           // starting size
        float shrinkRate = 2;         // px per frame
        float x = rnd.nextInt((int)(getWidth() * 0.8)) + (int)(getWidth() * 0.1);
        float y = rnd.nextInt((int)(getHeight() * 0.8)) + (int)(getHeight() * 0.1);
        circles.add(new Circle(x, y, radius, shrinkRate));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw all circles
        for (Circle c : circles) {
            c.draw(canvas, circlePaint);
        }
        // draw score and lives
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);

        canvas.drawText("Score: " + score, 20, 60, textPaint);
        canvas.drawText("Lives: " + lives, getWidth() - 200, 60, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            float tx = ev.getX();
            float ty = ev.getY();
            Iterator<Circle> it = circles.iterator();
            while (it.hasNext()) {
                Circle c = it.next();
                if (c.contains(tx, ty)) {
                    it.remove();
                    score += 10;
                    break;  // one click hits only one circle
                }
            }
        }
        return true;
    }

    private boolean isDead(){
        if(lives <= 0){
            return true;
        }
        return false;
    }
}
