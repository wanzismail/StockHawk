package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;

/**
 * For VELO TEKNOLOGI
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.udacity.stockhawk.sync
 */

public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
