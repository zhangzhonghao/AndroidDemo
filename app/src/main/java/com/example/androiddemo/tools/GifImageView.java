package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;

public class GifImageView extends View {
    private Movie movie;
    private long startTime;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying;

    public GifImageView(Context context) {
        super(context);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadGif(Uri uri) {
        try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
            if (is != null) {
                movie = Movie.decodeStream(is);
                if (movie != null) {
                    startAnimation();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimation() {
        if (movie == null) return;
        isPlaying = true;
        startTime = System.currentTimeMillis();
        invalidate();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isPlaying && movie != null) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int duration = movie.duration();
                    if (duration == 0) duration = 1000;
                    movie.setTime((int) (elapsed % duration));
                    invalidate();
                    handler.postDelayed(this, 50);
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (movie != null) {
            movie.draw(canvas, 0, 0);
        }
    }

    public void stopAnimation() {
        isPlaying = false;
    }
}