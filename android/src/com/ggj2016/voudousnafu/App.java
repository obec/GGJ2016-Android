package com.ggj2016.voudousnafu;

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import timber.log.Timber;

/**
 * Created by Sami on 1/29/16.
 */
public class App extends Application {

    private SoundPool mSoundPool;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(20)
                .build();
    }

    public static SoundPool getSoundPool(Context context) {
        return ((App)context.getApplicationContext()).getSoundPool();
    }

    public SoundPool getSoundPool() {
        return mSoundPool;
    }

}
