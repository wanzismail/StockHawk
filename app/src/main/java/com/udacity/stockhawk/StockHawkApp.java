package com.udacity.stockhawk;

import android.app.Application;

import timber.log.Timber;

/**
 * For VELO TEKNOLOGI
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.udacity.stockhawk
 */

public class StockHawkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}
