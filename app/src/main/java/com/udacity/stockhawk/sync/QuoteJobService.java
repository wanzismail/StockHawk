package com.udacity.stockhawk.sync;


import android.content.Intent;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import timber.log.Timber;

/**
 * For VELO TEKNOLOGI
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.udacity.stockhawk.sync
 */

public class QuoteJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        Timber.d("onStartJob. Job TAG: " + job.getTag());
        Intent nowIntent = new Intent(getApplicationContext(), QuoteIntentService.class);
        getApplicationContext().startService(nowIntent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
