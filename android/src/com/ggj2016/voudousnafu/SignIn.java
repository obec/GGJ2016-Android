package com.ggj2016.voudousnafu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.ggj2016.gregsbadday.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sami on 1/30/16.
 */
public class SignIn extends AppCompatActivity {

    public static final String KEY_ROUND_RESULT = "round_result";

    private static final String TAG = SignIn.class.getSimpleName();

    @Bind(R.id.sign_in) Button signInButton;
    @Bind(R.id.good_evil) Switch goodEvil;
    @Bind(R.id.sign_in_text) EditText signInText;
    @Bind(R.id.round_result) TextView roundResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            String roundResultMessage = intent.getStringExtra(KEY_ROUND_RESULT);
            if (!TextUtils.isEmpty(roundResultMessage)) {
                roundResult.setText(roundResultMessage);
                roundResult.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.sign_in)
    protected void onSignInClicked(){
        Log.d(TAG, String.format("Login text: %s", signInText.getText()));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.KEY_IS_GOOD, goodEvil.isChecked());
        startActivity(intent);
        finish();
    }
}
