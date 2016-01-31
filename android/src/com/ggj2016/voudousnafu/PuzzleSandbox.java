package com.ggj2016.voudousnafu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ggj2016.gregsbadday.R;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PuzzleSandbox extends AppCompatActivity {

    public static final String KEY_TIME_REMAINING = "remaining";
    public static final String KEY_CARD_TYPE = "type";
    private Handler mHandler = new Handler();

    public static final int RESULT_RUNE_COMPLETE = 0;
    public static final int RESULT_TIME_UP = 1;

    private long mTimeRemaining;
    private Runnable mTimesUpRunnable = new Runnable() {
        @Override
        public void run() {
            Timber.d("Round over from puzzle.");
            finishCardActivity(false);
        }
    };

    @Bind(R.id.trace_view) TraceView traceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        TraceView.CardType cardType = (TraceView.CardType) intent.getSerializableExtra(KEY_CARD_TYPE);
        mTimeRemaining = intent.getLongExtra(KEY_TIME_REMAINING, TimeUnit.SECONDS.toMillis(7));
        traceView.setCardType(cardType);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mTimesUpRunnable, mTimeRemaining);
    }

    public void finishCardActivity(boolean didWin) {
        mHandler.removeCallbacksAndMessages(null);
        int result = didWin ? RESULT_RUNE_COMPLETE : RESULT_TIME_UP;
        setResult(result);
        finish();
    }

}
