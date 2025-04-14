package com.example.fbird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Bird {
    private Bitmap birdBitmap;
    private int x, y, velocity = 0, gravity = 2;
    private int screenX, screenY;

    public Bird(Context context, int screenX, int screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
        birdBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        x = screenX / 4;
        y = screenY / 2;
    }

    public void update() {
        velocity += gravity;
        y += velocity;

        if (y < 0) y = 0;
        if (y > screenY - birdBitmap.getHeight()) y = screenY - birdBitmap.getHeight();
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(birdBitmap, x, y, paint);
    }

    public void jump() {
        velocity = -30; // jump effect
    }
}
