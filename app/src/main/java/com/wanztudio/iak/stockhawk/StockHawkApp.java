package com.wanztudio.iak.stockhawk;

import android.app.Application;

import com.wanztudio.iak.stockhawk.BuildConfig;

import timber.log.Timber;

/**
 * For LEARNING
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.wanztudio.iak.stockhawk
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
