package net.wequick.example.small;

import net.wequick.small.Small;

import timber.log.Timber;

/**
 * Created by galen on 15/11/3.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
        Small.setBaseUri("http://m.wequick.net/demo/");
    }
}
