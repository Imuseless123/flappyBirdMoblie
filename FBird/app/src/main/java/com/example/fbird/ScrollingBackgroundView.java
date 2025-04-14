package com.example.fbird;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;


public class ScrollingBackgroundView extends View {
    private Context context;
    private boolean isGameOver = false;
    private Bitmap background;
    private Bitmap bird;

    private int scrollX = 0;
    private int speed = 18;

    // Bird position and physics
    private float birdX;
    private float birdY;
    private float birdVelocity = 0;
    private float gravity = 3.0f;
    private float flapPower = -35f;
    private Bitmap pipeTop, pipeBottom;
    private int pipeWidth;
    private int gap = 400; // gap between top and bottom pipes
    private int pipeX;     // X position of the pipe
    private int pipeY;     // Y position of the top pipe (randomized)
    private int pipeSpeed = 10;


    public ScrollingBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background_game);
        bird = BitmapFactory.decodeResource(getResources(), R.drawable.player);

        // Scale the bird image (e.g., 1/4 of screen width)
        int birdWidth = getResources().getDisplayMetrics().widthPixels / 8;
        int birdHeight = bird.getHeight() * birdWidth / bird.getWidth(); // maintain aspect ratio
        bird = Bitmap.createScaledBitmap(bird, birdWidth, birdHeight, true);
        pipeTop = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_top);
        pipeBottom = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_bottom);

        // Set pipe width relative to screen size
        pipeWidth = getResources().getDisplayMetrics().widthPixels / 6;
        pipeTop = Bitmap.createScaledBitmap(pipeTop, pipeWidth, pipeTop.getHeight() * pipeWidth / pipeTop.getWidth(), true);
        pipeBottom = Bitmap.createScaledBitmap(pipeBottom, pipeWidth, pipeBottom.getHeight() * pipeWidth / pipeBottom.getWidth(), true);

        // Initialize pipe position (off-screen to right)
//        pipeX = getWidth() + 300; // will be corrected after layout
//        pipeY = getRandomPipeY();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 1. Draw the background first
        Bitmap scaled = Bitmap.createScaledBitmap(background, width, height, true);
        canvas.drawBitmap(scaled, -scrollX, 0, null);
        if (scrollX > 0) {
            canvas.drawBitmap(scaled, width - scrollX, 0, null);
        }
        // Update scroll after drawing background
        scrollX += speed;
        if (scrollX >= width) {
            scrollX = 0;
        }

        // 2. Update and draw the pipes
        pipeX -= pipeSpeed;
        if (pipeX + pipeWidth < 0) {
            pipeX = width;
            pipeY = getRandomPipeY();
        }

        // Scale top pipe to reach from top to pipeY
        @SuppressLint("DrawAllocation") Bitmap scaledTopPipe = Bitmap.createScaledBitmap(
                pipeTop,
                pipeWidth,
                pipeY,
                true
        );
        canvas.drawBitmap(scaledTopPipe, pipeX, 0, null);

        // Scale bottom pipe to reach from pipeY + gap to bottom
        int bottomPipeHeight = height - (pipeY + gap);
        @SuppressLint("DrawAllocation") Bitmap scaledBottomPipe = Bitmap.createScaledBitmap(
                pipeBottom,
                pipeWidth,
                bottomPipeHeight,
                true
        );
        canvas.drawBitmap(scaledBottomPipe, pipeX, pipeY + gap, null);


        // 3. Update bird physics
        birdVelocity += gravity;
        birdY += birdVelocity;
        if (birdY < 0) {
            birdY = 0;
        }
        if (birdY + bird.getHeight() >= height && !isGameOver) {
            isGameOver = true;
            // Delay returning to main activity (1 second)
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }, 1000);
            return; // Stop drawing further when game over
        }

        // 4. Draw the bird (centered horizontally)
        birdX = width / 4f;
        canvas.drawBitmap(bird, birdX, birdY, null);

        // Redraw the view
        invalidate();

        // 5. Collision Detection with pipes
        if (!isGameOver) {
            // Bird's bounding box
            float birdRight = birdX + bird.getWidth();
            float birdBottom = birdY + bird.getHeight();

            // Pipe top bounding box
            boolean hitsTopPipe = birdX < pipeX + pipeWidth &&
                    birdRight > pipeX &&
                    birdY < pipeY;

            // Pipe bottom bounding box
            boolean hitsBottomPipe = birdX < pipeX + pipeWidth &&
                    birdRight > pipeX &&
                    birdBottom > pipeY + gap;

            if (hitsTopPipe || hitsBottomPipe) {
                isGameOver = true;
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }, 1000);
                return; // Stop drawing if hit a pipe
            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Flap on touch
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            birdVelocity = flapPower;
        }
        return true;
    }

    private int getRandomPipeY() {
        int minY = getHeight() / 6;
        int maxY = getHeight() / 2;
        return minY + (int)(Math.random() * (maxY - minY));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pipeX = w + 300; // now width is available
        pipeY = getRandomPipeY();
    }

}
