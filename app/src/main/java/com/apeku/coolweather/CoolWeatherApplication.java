package com.apeku.coolweather;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by apeku on 2018/3/29.
 */

public class CoolWeatherApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
