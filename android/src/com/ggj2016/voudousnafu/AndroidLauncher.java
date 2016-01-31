package com.ggj2016.voudousnafu;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.ggj2016.gregsbadday.MainGame;

public class AndroidLauncher extends AndroidApplication implements ApplicationListener {

    private GLSurfaceView mSurfaceView;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        mSurfaceView = (GLSurfaceView) initializeForView(this, config);

        initialize(new MainGame(), config);
    }


    @Override
    public void create() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

}
