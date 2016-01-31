package com.ggj2016.gregsbadday;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.plattysoft.leonids.ParticleSystem;

import timber.log.Timber;

public class TraceView extends View {

    private static final float STROKE_WIDTH = 5.0f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    private final Paint paint = new Paint();
    private final Path path = new Path();

    private final PointF lastTouch = new PointF();
    private final RectF dirtyRect = new RectF();

    private ParticleSystem particleSystem;
    private Activity activity;

    Bitmap backgroundBitmap;
    Drawable tintedBackgroundDrawable;

    private Point windowSize = new Point();

    public TraceView(Context context) {
        this(context, null);
    }

    public TraceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TraceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) {
            init(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TraceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context) {

        paint.setAntiAlias(true);
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);

        backgroundBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.alpha_test);
        tintedBackgroundDrawable = getResources().getDrawable(R.drawable.alpha_test);
        if (tintedBackgroundDrawable != null) {
            tintedBackgroundDrawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            setBackground(tintedBackgroundDrawable);
        }

        if (context instanceof Activity) {
            activity = (Activity) context;

            Display display = activity.getWindowManager().getDefaultDisplay();
            display.getSize(windowSize);

            particleSystem = new ParticleSystem(activity, 100, R.drawable.star_white, 800);
            particleSystem.setScaleRange(0.7f, 1.3f);
            particleSystem.setSpeedRange(0.05f, 0.1f);
            particleSystem.setRotationSpeedRange(90, 180);
            particleSystem.setFadeOut(200, new AccelerateInterpolator());
        } else {
            throw new IllegalStateException("try again with an activity context");
        }
    }

    public void clearDrawing() {
        path.reset();
        particleSystem.cancel();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                lastTouch.set(x, y);
                particleSystem.emit((int) x, (int) y, 40);
                break;
            case MotionEvent.ACTION_UP:
                particleSystem.stopEmitting();
                break;
            case MotionEvent.ACTION_MOVE:
                resetDirtyRect(x, y);

                int historySize = event.getHistorySize();

                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }

                path.lineTo(x, y);
                particleSystem.updateEmitPoint((int) x, (int) y);

                float percentageX = Math.max(0, Math.min(0.999f, x / windowSize.x));
                float percentageY = Math.max(0, Math.min(0.999f, y / windowSize.y));
                float targetX = backgroundBitmap.getWidth() * percentageX;
                float targetY = backgroundBitmap.getHeight() * percentageY;
                int color = backgroundBitmap.getPixel((int)targetX, (int)targetY);
                Timber.d("#%06X  %d", (0xFFFFFF & color), color);

                //TODO COUNT DEM POINTS

                break;
            default:
                return false;
        }

        invalidate(
            (int) (dirtyRect.left - HALF_STROKE_WIDTH),
            (int) (dirtyRect.top - HALF_STROKE_WIDTH),
            (int) (dirtyRect.right + HALF_STROKE_WIDTH),
            (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastTouch.set(x, y);

        return true;
    }

    private void expandDirtyRect(float x, float y) {
        if (x < dirtyRect.left) {
            dirtyRect.left = x;
        } else if (x > dirtyRect.right) {
            dirtyRect.right = x;
        }

        if (y < dirtyRect.top) {
            dirtyRect.top = y;
        } else if (y > dirtyRect.bottom) {
            dirtyRect.bottom = y;
        }
    }

    private void resetDirtyRect(float x, float y) {
        dirtyRect.left = Math.min(lastTouch.x, x);
        dirtyRect.right = Math.max(lastTouch.x, x);
        dirtyRect.top = Math.min(lastTouch.y, y);
        dirtyRect.bottom = Math.max(lastTouch.y, y);
    }
}
