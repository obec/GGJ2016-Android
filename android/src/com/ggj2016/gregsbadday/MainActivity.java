package com.ggj2016.gregsbadday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by Sami on 1/29/16.
 */
public class MainActivity extends AppCompatActivity {

    public static final int RESULT_RUNE_COMPLETE = 0;
    public static final int RESULT_TIME_UP = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long SCALE_DURATION = 300L;
    private static final long ROUND_TIME = TimeUnit.SECONDS.toMillis(15);

    @Bind(R.id.head) Button headButton;
    @Bind(R.id.left_hand) Button leftHandButton;
    @Bind(R.id.right_hand) Button rightHandButton;
    @Bind(R.id.body) Button bodyButton;
    @Bind(R.id.right_leg) Button rightLegButton;
    @Bind(R.id.left_leg) Button leftLegButton;
    @Bind(R.id.pin) ImageView pin;
    @Bind(R.id.test_view) View testView;
    @Bind(R.id.second_test_view) View secondTestView;
    @Bind(R.id.voodoo_target_map) ImageView colorWheel;

    private long mRoundStartTime;

    TraceView.CardType[] runeCards = TraceView.CardType.values();
    int runeCardIndex = 0;

    private static Map<Integer, Region> map = new HashMap<>(Region.values().length);
    static {
        map.put(Region.HEAD.color, Region.HEAD);
        map.put(Region.BODY.color, Region.BODY);
        map.put(Region.LEFT_HAND.color, Region.LEFT_HAND);
        map.put(Region.RIGHT_HAND.color, Region.RIGHT_HAND);
        map.put(Region.LEFT_LEG.color, Region.LEFT_LEG);
        map.put(Region.RIGHT_LEG.color, Region.RIGHT_LEG);
    }

    private List<View> viewList = new ArrayList();
    private boolean isGood;
    private Point windowSize = new Point();

    private boolean mRoundOver;

    private Handler mHandler = new Handler();

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
                        onPinTouch(v);
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
                        Timber.d("#%06X  %d", (0xFFFFFF & color), color);
//                        for(View view: viewList) {
//                            boolean pinOverView = isPinOverView(view, pinPoint);
//                            Timber.d("Is the pin over the view? %b", pinOverView);
//                            Toast.makeText(MainActivity.this, "Is pin over view? " + pinOverView, Toast.LENGTH_SHORT).show();
//                        }
                        Region pinnedRegion = map.get(color);
                        if (pinnedRegion != null) {
                            Toast.makeText(MainActivity.this, pinnedRegion.toString(), Toast.LENGTH_SHORT).show();
                        }

                        onPinDrop(v);

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

        mRoundStartTime = System.currentTimeMillis();
        showRune();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pin.getLayoutParams();
            lp.leftMargin = (int) (windowSize.x - (pin.getWidth() * 1.2));
            lp.topMargin = (int) (windowSize.y - (pin.getHeight() * 1.5));
            pin.setLayoutParams(lp);
        }
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

    @OnClick( {R.id.open_sandbox_protect, R.id.open_sandbox_disrupt, R.id.open_sandbox_fire, R.id.open_sandbox_love })
         protected void onOpenSandboxClicked(View view) {
        Intent intent = new Intent(this, PuzzleSandbox.class);

        switch (view.getId()) {
            case R.id.open_sandbox_protect:
                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.PROTECTION);
                break;
            case R.id.open_sandbox_disrupt:
                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.DISRUPT);
                break;
            case R.id.open_sandbox_fire:
                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.FIRE);
                break;
            case R.id.open_sandbox_love:
                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.LOVE);
                break;
        }

        startActivity(intent);
    }

    private enum Region{
        HEAD("Head", -9219073),
        LEFT_HAND("Left hand", -14287090),
        RIGHT_HAND("Right hand", -130301),
        BODY("Body", -15840001),
        RIGHT_LEG("Right leg",-14066),
        LEFT_LEG("Left leg", -61711);

        int color;
        String name;
        Region(String name, int color) {
            this.color = color;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("%s #%06X  %d",name, (0xFFFFFF & color), color);
        }

    }

    private void onPinTouch(View view) {
        view.animate()
                .scaleXBy(0.2f)
                .scaleYBy(0.2f)
                .setDuration(SCALE_DURATION)
                .start();
    }

    private void onPinDrop(View view) {
        view.animate()
                .scaleXBy(-0.2f)
                .scaleYBy(-0.2f)
                .setDuration(SCALE_DURATION)
                .start();
    }

    private void showRune() {
        TraceView.CardType type = runeCards[runeCardIndex];
        runeCardIndex++;

        if (runeCardIndex >= runeCards.length) {
            runeCardIndex = 0;
        }

        Intent intent = new Intent(this, PuzzleSandbox.class);
        intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, type);
        startActivityForResult(intent, 0);
    }

    @OnClick(R.id.progression)
    protected void onProgresion(View view) {
        showRune();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_TIME_UP) {
            mRoundOver = true;
        }
    }
}
