package com.ggj2016.gregsbadday;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ggj2016.gregsbadday.messages.GameStateMessage;
import com.ggj2016.gregsbadday.messages.PinMessage;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class PuzzleSandbox extends AppCompatActivity {

    public static final String KEY_TIME_REMAINING = "remaining";
    public static final String KEY_CARD_TYPE = "type";
    private Handler mHandler = new Handler();

    public static final int RESULT_RUNE_COMPLETE = 0;
    public static final int RESULT_TIME_UP = 1;

    private long mTimeRemaining;

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

        // Variable for checking the connection status
        boolean internetConnection = false;

        // Check connection with the server
        internetConnection = checkInternetConnection();
        if(internetConnection) {
            PinMessage message = new PinMessage(0, 0, 0, 0, 0, 0);
            NetworkManager.postServer(message, new NetworkManager.Listener() {
                @Override
                public void onSuccess(GameStateMessage message) {
                    Timber.d("Success!");
                }

                @Override
                public void onError() {
                    Timber.d("Failure.");
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishCardActivity(false);
            }
        }, mTimeRemaining);
    }

    // Function to check the connection with the server
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

    public void finishCardActivity(boolean didWin) {
        int result = didWin ? RESULT_RUNE_COMPLETE : RESULT_TIME_UP;
        setResult(result);
        finish();
    }

    @OnClick(R.id.clear_button)
    void onClearButtonClicked(View view) {
        traceView.clearDrawing();
    }
}
