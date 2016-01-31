package com.ggj2016.gregsbadday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ggj2016.gregsbadday.messages.PinMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Bind(R.id.pin) ImageView pin;
    @Bind(R.id.second_pin) ImageView secondPin;
    @Bind(R.id.third_pin) ImageView thirdPin;
    @Bind(R.id.test_view) View testView;
    @Bind(R.id.second_test_view) View secondTestView;
    @Bind(R.id.voodoo_target_map) ImageView colorWheel;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewList.add(testView);
        viewList.add(secondTestView);
        final Bitmap bitmap = ((BitmapDrawable)colorWheel.getDrawable()).getBitmap();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(windowSize);
        Score scorePin1 = new Score();
        Score scorePin2 = new Score();
        Score scorePin3 = new Score();
        pin.setTag(scorePin1);
        secondPin.setTag(scorePin2);
        thirdPin.setTag(scorePin3);
        scoreList.add((Score) pin.getTag());
        scoreList.add((Score) secondPin.getTag());
        scoreList.add((Score)thirdPin.getTag());

        pin.setOnTouchListener(new PinTouchListener(bitmap));
        secondPin.setOnTouchListener(new PinTouchListener(bitmap));
        thirdPin.setOnTouchListener(new PinTouchListener(bitmap));
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
                    float percentageY = pinPoint.y/ windowSize.y;
                    float targetX = bitmap.getWidth() * percentageX;
                    float targetY = bitmap.getHeight() * percentageY;

                    int color = bitmap.getPixel((int)targetX, (int)targetY);
                    Timber.d("#%06X  %d", (0xFFFFFF & color), color);
                    Region pinnedRegion = map.get(color);
                    if (pinnedRegion != null) {
                        Toast.makeText(MainActivity.this, pinnedRegion.toString(), Toast.LENGTH_SHORT).show();
                        Object tag = v.getTag();
                        if(tag instanceof Score){
                            ((Score)tag).setScore(pinnedRegion.name, 1);
                            if (arePinsPlaced()) {
                                preparePinMessage();
                            }
                        }

                    }

                    break;
                default:
                    return false;
            }
            return true;
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
            PinMessage scoreMessage = new PinMessage(head,body,rArm,lArm,rLeg,lLeg);
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
        pin.bringToFront();
        secondPin.bringToFront();
        thirdPin.bringToFront();
    }

    @OnCheckedChanged (R.id.good_evil)
    protected void onGoodEvilChanged(boolean checked){
        isGood = checked;
    }

    private enum Region{
        HEAD("Head", -9219073),
        LEFT_HAND("Left hand", -14287090),
        RIGHT_HAND("Right hand", -65536),
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

    @OnClick( {R.id.open_sandbox_protect, R.id.open_sandbox_disrupt, R.id.open_sandbox_fire, R.id.open_sandbox_love })
         protected void onOpenSandboxClicked(View view) {

        Intent intent = new Intent(this, PuzzleSandbox.class);

        switch (view.getId()) {
            case R.id.open_sandbox_protect:
                intent.putExtra("CardType", TraceView.CardType.PROTECTION);
                break;
            case R.id.open_sandbox_disrupt:
                intent.putExtra("CardType", TraceView.CardType.DISRUPT);
                break;
            case R.id.open_sandbox_fire:
                intent.putExtra("CardType", TraceView.CardType.FIRE);
                break;
            case R.id.open_sandbox_love:
                intent.putExtra("CardType", TraceView.CardType.LOVE);
                break;
        }

        startActivity(intent);
    }




}
