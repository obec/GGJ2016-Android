package com.ggj2016.voudousnafu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.ggj2016.gregsbadday.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sami on 1/30/16.
 */
public class SignIn extends AppCompatActivity {

    private static final String TAG = SignIn.class.getSimpleName();

    @Bind(R.id.sign_in) Button signInButton;
    @Bind(R.id.good_evil) Switch goodEvil;
    @Bind(R.id.sign_in_text) EditText signInText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
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
