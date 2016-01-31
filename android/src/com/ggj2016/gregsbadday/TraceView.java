package com.ggj2016.gregsbadday;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.plattysoft.leonids.ParticleSystem;

public class TraceView extends View {

    private static final String TAG = TraceView.class.getSimpleName();

    private static final float STROKE_WIDTH = 5.0f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    private final Paint paint = new Paint();
    private final Path path = new Path();

    private final PointF lastTouch = new PointF();
    private final RectF dirtyRect = new RectF();

    private ParticleSystem particleSystem;
    private Activity activity;

    private static final int rectWidth = 20;

    private static final int pointOneLeft = 98;
    private static final int pointOneRight = pointOneLeft + rectWidth;
    private static final int pointOneTop = 210;
    private static final int pointOneBottom = pointOneTop + rectWidth;

    private static final int pointTwoLeft = 270;
    private static final int pointTwoRight = pointTwoLeft + rectWidth;
    private static final int pointTwoTop = 210;
    private static final int pointTwoBottom = pointTwoTop + rectWidth;

    private static final int pointThreeLeft = 421;
    private static final int pointThreeRight = pointThreeLeft + rectWidth;
    private static final int pointThreeTop = 210;
    private static final int pointThreeBottom = pointThreeTop + rectWidth;

    private static final int pointFourLeft = 270;
    private static final int pointFourRight = pointFourLeft + rectWidth;
    private static final int pointFourTop = 410;
    private static final int pointFourBottom = pointFourTop + rectWidth;

    private static final int pointFiveLeft = 270;
    private static final int pointFiveRight = pointFiveLeft + rectWidth;
    private static final int pointFiveTop = 620;
    private static final int pointFiveBottom = pointFiveTop + rectWidth;

    private RectF pointOneRect;
    private RectF pointTwoRect;
    private RectF pointThreeRect;
    private RectF pointFourRect;
    private RectF pointFiveRect;

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

        setupRects();

        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            throw new IllegalStateException("try again with an activity context");
        }
    }

    private void setupRects() {
        pointOneRect = new RectF(pointOneLeft, pointOneTop, pointOneRight, pointOneBottom);
        pointTwoRect = new RectF(pointTwoLeft, pointTwoTop, pointTwoRight, pointTwoBottom);
        pointThreeRect = new RectF(pointThreeLeft, pointThreeTop, pointThreeRight, pointThreeBottom);
        pointFourRect = new RectF(pointFourLeft, pointFourTop, pointFourRight, pointFourBottom);
        pointFiveRect = new RectF(pointFiveLeft, pointFiveTop, pointFiveRight, pointFiveBottom);
    }

    public void clearDrawing() {
        path.reset();

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);

        //draw points!
        canvas.drawRect(pointOneRect, paint);
        canvas.drawRect(pointTwoRect, paint);
        canvas.drawRect(pointThreeRect, paint);
        canvas.drawRect(pointFourRect, paint);
        canvas.drawRect(pointFiveRect, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                lastTouch.set(x, y);
                particleSystem = new ParticleSystem(activity, 100, R.drawable.star_white, 800);
                particleSystem.setScaleRange(0.7f, 1.3f);
                particleSystem.setSpeedRange(0.05f, 0.1f);
                particleSystem.setRotationSpeedRange(90, 180);
                particleSystem.setFadeOut(200, new AccelerateInterpolator());
                particleSystem.emit((int) x, (int) y, 40);
                return true;
            case MotionEvent.ACTION_UP:
                particleSystem.stopEmitting();
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

                if (dirtyRect.intersect(pointOneRect)) {
                    Log.e(TAG, "Point One Intersected");
                    Toast.makeText(activity, "Point One Intersected", Toast.LENGTH_SHORT).show();
                } else if (dirtyRect.intersect(pointTwoRect)) {
                    Log.e(TAG, "Point Two Intersected");
                    Toast.makeText(activity, "Point Two Intersected", Toast.LENGTH_SHORT).show();
                } else if (dirtyRect.intersect(pointThreeRect)) {
                    Log.e(TAG, "Point Three Intersected");
                    Toast.makeText(activity, "Point Three Intersected", Toast.LENGTH_SHORT).show();
                } else if (dirtyRect.intersect(pointFourRect)) {
                    Log.e(TAG, "Point Four Intersected");
                    Toast.makeText(activity, "Point Four Intersected", Toast.LENGTH_SHORT).show();
                } else if (dirtyRect.intersect(pointFiveRect)) {
                    Log.e(TAG, "Point Five Intersected");
                    Toast.makeText(activity, "Point Five Intersected", Toast.LENGTH_SHORT).show();
                }
                
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
