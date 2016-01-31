package com.ggj2016.gregsbadday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by Sami on 1/29/16.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.head) Button headButton;
    @Bind(R.id.left_hand) Button leftHandButton;
    @Bind(R.id.right_hand) Button rightHandButton;
    @Bind(R.id.body) Button bodyButton;
    @Bind(R.id.right_leg) Button rightLegButton;
    @Bind(R.id.left_leg) Button leftLegButton;
    @Bind(R.id.pin) ImageView pin;
    @Bind(R.id.test_view) View testView;
    @Bind(R.id.second_test_view) View secondTestView;
    @Bind(R.id.color_wheel) ImageView colorWheel;



    private List<View> viewList = new ArrayList();
    private boolean isGood;
    private Point windowSize = new Point();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        headButton.setTag(Region.HEAD);
        leftHandButton.setTag(Region.LEFT_HAND);
        rightHandButton.setTag(Region.RIGHT_HAND);
        bodyButton.setTag(Region.BODY);
        rightLegButton.setTag(Region.RIGHT_LEG);
        leftLegButton.setTag(Region.LEFT_LEG);
        viewList.add(testView);
        viewList.add(secondTestView);
        final Bitmap bitmap = ((BitmapDrawable)colorWheel.getDrawable()).getBitmap();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(windowSize);

        pin.setOnTouchListener(new View.OnTouchListener() {
            float deltaX;
            float deltaY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) pin.getLayoutParams();
                        deltaX = x - lParams.leftMargin;
                        deltaY = y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pin.getLayoutParams();
                        layoutParams.leftMargin = (int) (x - deltaX);
                        layoutParams.topMargin = (int) (y - deltaY);
                        pin.setLayoutParams(layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        PointF pinPoint = getPinPoint();
                        Timber.d("X: %d, Y: %d", (int) pinPoint.x, (int) pinPoint.y);
                        float percentageX = pinPoint.x / windowSize.x;
                        float percentageY = pinPoint.y/ windowSize.y;
                        float targetX = bitmap.getWidth() * percentageX;
                        float targetY = bitmap.getHeight() * percentageY;

                        int color = bitmap.getPixel((int)targetX, (int)targetY);
                        Timber.d("#%06X", (0xFFFFFF & color));
                        for(View view: viewList) {
                            boolean pinOverView = isPinOverView(view, pinPoint);
                            Timber.d("Is the pin over the view? %b", pinOverView);
                            Toast.makeText(MainActivity.this, "Is pin over view? " + pinOverView, Toast.LENGTH_SHORT).show();
                        }

                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }
    private boolean isPinOverView(View view, PointF pointF){
        PointF pinPoint = getPinPoint();
        if (((pinPoint.x > view.getLeft()) && (pinPoint.x < view.getRight())) &&
                ((pinPoint.y < view.getBottom()) && (pinPoint.y > view.getTop()))){
            return true;
        }
        else{
            return false;
        }

    }
    private PointF getPinPoint(){
        return new PointF(pin.getX(), pin.getY() + pin.getHeight());
    }

    @Override
    protected void onStart() {
        super.onStart();
        pin.bringToFront();
    }

    @OnCheckedChanged (R.id.good_evil)
    protected void onGoodEvilChanged(boolean checked){
        isGood = checked;
    }

    @OnClick({R.id.head, R.id.left_hand, R.id.right_hand, R.id.body, R.id.right_leg, R.id.left_leg})
    protected void onRegionClicked(View view){
        Log.d(TAG, String.format("Am I good? %b", isGood));
        Region region = (Region) view.getTag();
        switch (region) {
            case HEAD:
                Log.d("main", "headClicked");
                break;
            case LEFT_HAND:
                Log.d("main", "leftHandClicked");
                break;
            case RIGHT_HAND:
                Log.d("main", "rightHandClicked");
                break;
            case BODY:
                Log.d("main", "bodyClicked");
                break;
            case RIGHT_LEG:
                Log.d("main", "rightLegClicked");
                break;
            case LEFT_LEG:
                Log.d("main", "leftLegClicked");
                break;
        }

    }

    @OnClick(R.id.open_sandbox)
    protected void onOpenSandboxClicked(View view) {
        Intent intent = new Intent(this, PuzzleSandbox.class);
        startActivity(intent);
    }

    @OnClick(R.id.open_libgdx_sandbox)
    protected void onOpenLibGdxSandbox(View view) {
        Intent intent = new Intent(this, AndroidLauncher.class);
        startActivity(intent);
    }

    private enum Region{
        HEAD,
        LEFT_HAND,
        RIGHT_HAND,
        BODY,
        RIGHT_LEG,
        LEFT_LEG,
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Rect pinRect = new Rect();
        pin.getGlobalVisibleRect(pinRect);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:



        }


        return true;

    }
}
