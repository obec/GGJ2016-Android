package com.ggj2016.gregsbadday;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ggj2016.gregsbadday.messages.GameStateMessage;
import com.ggj2016.gregsbadday.messages.PinMessage;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class PuzzleSandbox extends AppCompatActivity {
    private Bitmap bitmap = null;

    @Bind(R.id.trace_view) TraceView traceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        ButterKnife.bind(this);

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

    @OnClick(R.id.clear_button)
    void onClearButtonClicked(View view) {
        traceView.clearDrawing();
    }
}
