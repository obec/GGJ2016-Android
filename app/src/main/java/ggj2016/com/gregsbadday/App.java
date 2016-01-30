package ggj2016.com.gregsbadday;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Sami on 1/29/16.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
