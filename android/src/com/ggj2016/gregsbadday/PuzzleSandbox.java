package com.ggj2016.gregsbadday;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PuzzleSandbox extends AppCompatActivity {

    @Bind(R.id.trace_view) TraceView traceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.clear_button)
    void onClearButtonClicked(View view) {
        traceView.clearDrawing();
    }
}
