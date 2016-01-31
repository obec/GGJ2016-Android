package com.ggj2016.voudousnafu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ggj2016.gregsbadday.R;
import com.ggj2016.voudousnafu.messages.GameStateMessage;
import com.ggj2016.voudousnafu.messages.PinMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Sami on 1/29/16.
 */
public class MainActivity extends AppCompatActivity {

    public static final String KEY_IS_GOOD = "is_good";
    public static final int RESULT_RUNE_COMPLETE = 0;
    public static final int RESULT_TIME_UP = 1;

    private static final String TAG = MainActivity.class.getSimpleName();


    private static final long SCALE_DURATION = 300L;
    private static final long ROUND_TIME = TimeUnit.SECONDS.toMillis(15);
    @Bind(R.id.test_view) View testView;
    @Bind(R.id.second_test_view) View secondTestView;
    @Bind(R.id.voodoo_target_map) ImageView colorWheel;
    @Bind(R.id.root_view) RelativeLayout rootView;

    private long mRoundStartTime;
    private View mWaitingView;

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
    private List<Score> scoreList = new ArrayList();
    private boolean isGood;
    private Point windowSize = new Point();


    private boolean mRoundOver;

    private Handler mHandler = new Handler();

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewList.add(testView);
        viewList.add(secondTestView);
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(windowSize);

        bitmap = ((BitmapDrawable) colorWheel.getDrawable()).getBitmap();

        mRoundStartTime = System.currentTimeMillis();
        showRune();
        Intent intent = getIntent();
        if (intent != null) {
            isGood = intent.getBooleanExtra(KEY_IS_GOOD, false);
        }
    }


    private class PinTouchListener implements View.OnTouchListener {

        PinTouchListener(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        Bitmap bitmap;
        float deltaX;
        float deltaY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getRawX();
            float y = event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    deltaX = x - lParams.leftMargin;
                    deltaY = y - lParams.topMargin;
                    lParams.leftMargin = (int) (v.getLeft());
                    lParams.topMargin = (int) (v.getTop());
                    lParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    lParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    onPinTouch(v);

                    break;
                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.leftMargin = (int) (x - deltaX);
                    layoutParams.topMargin = (int) (y - deltaY);
                    v.setLayoutParams(layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, String.format("Am I good? %b", isGood));
                    PointF pinPoint = getPinPoint(v);
                    Timber.d("X: %d, Y: %d", (int) pinPoint.x, (int) pinPoint.y);
                    float percentageX = pinPoint.x / windowSize.x;
                    float percentageY = pinPoint.y / windowSize.y;
                    float targetX = bitmap.getWidth() * percentageX;
                    float targetY = bitmap.getHeight() * percentageY;

                    int color = bitmap.getPixel((int) targetX, (int) targetY);
                    Timber.d("#%06X  %d", (0xFFFFFF & color), color);
                    Region pinnedRegion = map.get(color);
                    if (pinnedRegion != null) {
                        Toast.makeText(MainActivity.this, pinnedRegion.toString(), Toast.LENGTH_SHORT).show();
                        Object tag = v.getTag();
                        if (tag instanceof Score) {
                            ((Score) tag).setScore(pinnedRegion.name, isGood ? 1 : -1);
                        }
                        v.setEnabled(false);
                        showRune();

                    }
                    onPinDrop(v);

                    break;
                default:
                    return false;
            }
            return true;


        }
    }

    private View createPin(){
        ImageView newPin = new ImageView(this);
        newPin.setImageResource(R.drawable.pin);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
                                                (getResources().getDimensionPixelSize(R.dimen.pin_width),
                                                getResources().getDimensionPixelSize(R.dimen.pin_height));
        newPin.setLayoutParams(lp);
        newPin.setOnTouchListener(new PinTouchListener(bitmap));
        Score score = new Score();
        newPin.setTag(score);
        scoreList.add((Score)newPin.getTag());
        return newPin;
    }

    private boolean isPinOverView(View view, PointF pointF) {
        PointF pinPoint = getPinPoint(view);
        if (((pinPoint.x > view.getLeft()) && (pinPoint.x < view.getRight())) &&
                ((pinPoint.y < view.getBottom()) && (pinPoint.y > view.getTop()))) {
            return true;
        } else {
            return false;
        }

    };
    private boolean arePinsPlaced() {
        boolean placed = true;
        for (Score score : scoreList) {
            placed = ((score.getScore() != 0) && (placed));
        }
        return placed;
    }

    private PinMessage preparePinMessage(){
        int head = 0;
        int lArm = 0;
        int rArm = 0;
        int body = 0;
        int lLeg = 0;
        int rLeg = 0;
        if (arePinsPlaced()) {
            for(Score score: scoreList) {
                if (score.getBodyPart().equals(Region.HEAD.name)) {
                    head = head + score.getScore();
                }
                else if (score.getBodyPart().equals(Region.LEFT_HAND.name)) {
                    lArm = lArm + score.getScore();
                }
                else if (score.getBodyPart().equals(Region.RIGHT_HAND.name)) {
                    rArm = rArm + score.getScore();
                }
                else if (score.getBodyPart().equals(Region.BODY.name)) {
                    body = body + score.getScore();
                }
                else if (score.getBodyPart().equals(Region.LEFT_LEG.name)) {
                    lLeg = lLeg + score.getScore();
                }
                else if (score.getBodyPart().equals(Region.RIGHT_LEG.name)) {
                    rLeg = rLeg + score.getScore();
                }
            }
        }
        return new PinMessage(head,body,rArm,lArm,rLeg,lLeg);
    }

//    private boolean isPinOverView(View view, PointF pointF){
//        PointF pinPoint = getPinPoint(view);
//        if (((pinPoint.x > view.getLeft()) && (pinPoint.x < view.getRight())) &&
//                ((pinPoint.y < view.getBottom()) && (pinPoint.y > view.getTop()))){
//            return true;
//        }
//        else{
//            return false;
//        }
//
//    }
    private static PointF getPinPoint(View view){
        return new PointF(view.getX(), view.getY() + view.getHeight());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        if (hasFocus) {
//            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pin.getLayoutParams();
//            lp.leftMargin = (int) (windowSize.x - (pin.getWidth() * 1.2));
//            lp.topMargin = (int) (windowSize.y - (pin.getHeight() * 1.5));
//            pin.setLayoutParams(lp);
//        }
//    }


//    @OnClick({R.id.open_sandbox_protect, R.id.open_sandbox_disrupt, R.id.open_sandbox_fire, R.id.open_sandbox_love})
//    protected void onOpenSandboxClicked(View view) {
//
//        Intent intent = new Intent(this, PuzzleSandbox.class);
//
//        switch (view.getId()) {
//            case R.id.open_sandbox_protect:
//                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.PROTECTION);
//                break;
//            case R.id.open_sandbox_disrupt:
//                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.DISRUPT);
//                break;
//            case R.id.open_sandbox_fire:
//                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.FIRE);
//                break;
//            case R.id.open_sandbox_love:
//                intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, TraceView.CardType.LOVE);
//                break;
//        }
//
//        startActivityForResult(intent, 0);
//    }

    private enum Region {
        HEAD("Head", -8430081),
        LEFT_HAND("Left hand", -14024950),
        RIGHT_HAND("Right hand", -64512),
        BODY("Body", -15772161),
        RIGHT_LEG("Right leg", -12278),
        LEFT_LEG("Left leg", -63245);

        int color;
        String name;

        Region(String name, int color) {
            this.color = color;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("%s #%06X  %d", name, (0xFFFFFF & color), color);
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
        mHandler.removeCallbacksAndMessages(mRoundOverRunnable);
        TraceView.CardType type = runeCards[runeCardIndex];
        runeCardIndex++;

        if (runeCardIndex >= runeCards.length) {
            runeCardIndex = 0;
        }

        Intent intent = new Intent(this, PuzzleSandbox.class);
        intent.putExtra(PuzzleSandbox.KEY_CARD_TYPE, type);
        long timeRemaining = getTimeRemaining();
        Timber.d("Time remaining: %d", timeRemaining);
        intent.putExtra(PuzzleSandbox.KEY_TIME_REMAINING, timeRemaining);
        startActivityForResult(intent, 0);
    }

//    @OnClick(R.id.progression)
//    protected void onProgresion(View view) {
//        showRune();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_TIME_UP) {
            roundOver();
        } else {
            mHandler.postDelayed(mRoundOverRunnable, getTimeRemaining());
            View view = createPin();
            ((ViewGroup)rootView).addView(view);
        }
    }

    private Runnable mRoundOverRunnable = new Runnable() {
        @Override
        public void run() {
            roundOver();
        }
    };

    private void roundOver() {
        Toast.makeText(this, "Times up!", Toast.LENGTH_SHORT).show();
        mRoundOver = true;
        PinMessage message = preparePinMessage();
        boolean internetConnection = checkInternetConnection();
        if(internetConnection) {
            NetworkManager.postServer(message, new NetworkManager.Listener() {
                @Override
                public void onSuccess(GameStateMessage message) {
                    Timber.d("Success!");
                    reset();
                }

                @Override
                public void onError() {
                    Timber.d("Failure.");
                    reset();
                }
            });
        }

        mWaitingView = getLayoutInflater().inflate(R.layout.widget_waiting, rootView, true);
    }

    private long getTimeRemaining() {
        return ROUND_TIME - (System.currentTimeMillis() - mRoundStartTime);
    }

    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connection = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (connection.getActiveNetworkInfo() != null) {
            Toast.makeText(this, " Connected ", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, " Disconnected ", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void reset() {
        if (mWaitingView != null) {
            rootView.removeView(mWaitingView);
        }
    }

}
