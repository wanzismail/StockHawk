package com.udacity.stockhawk.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.utils.MockUtils;
import com.udacity.stockhawk.utils.Utils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

import static com.udacity.stockhawk.data.PrefUtils.setStockStatus;

/**
 * For VELO TEKNOLOGI
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.udacity.stockhawk.sync
 */

public final class QuoteSyncJob {

    public static final String JOB_TAG_ONE_OFF = "job_tag_one_off";
    public static final String JOB_TAG_PERIODIC = "job_tag_periodic";
    public static final String JOB_TAG_PERIODIC_WIDGET = "job_tag_periodic_widget";

    public static final int PERIOD_SYNC_WIDGET = 7200;  // 2 hours
    public static final int PERIOD_SYNC = 30; // 30 seconds
    private static final int PERIOD_HISTORY = 1; // 1 year

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;
    private static boolean validSymbol = true;

    /** Integration points and error cases **/
    public static final int STOCK_STATUS_OK = 0;
    public static final int STOCK_STATUS_SERVER_DOWN = 1;
    public static final int STOCK_STATUS_SERVER_INVALID = 2;
    public static final int STOCK_STATUS_INVALID = 3;
    public static final int STOCK_STATUS_EMPTY = 4;
    public static final int STOCK_STATUS_UNKNOWN = 5;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STOCK_STATUS_OK, STOCK_STATUS_SERVER_DOWN, STOCK_STATUS_SERVER_INVALID,
            STOCK_STATUS_INVALID, STOCK_STATUS_EMPTY, STOCK_STATUS_UNKNOWN})
    public @interface StockStatus{}



    private QuoteSyncJob() {
    }

    static void getQuotes(final Context context) {

        Timber.d("Running sync job");
        String symbol = null;
        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                setStockStatus(context, STOCK_STATUS_EMPTY);
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            if (quotes.isEmpty()){
                setStockStatus(context, STOCK_STATUS_SERVER_DOWN);
            }

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                symbol = iterator.next();
                StockQuote quote;
                Stock stock = quotes.get(symbol);
                List<HistoricalQuote> history;
                float price, change, percentChange;
                String name;
                StringBuilder historyBuilder = new StringBuilder();


                try {
                    quote = stock.getQuote();
                    price = quote.getPrice().floatValue();
                    change = quote.getChange().floatValue();
                    percentChange = quote.getChangeInPercent().floatValue();
                    name = quotes.get(symbol).getName();

                } catch (NullPointerException npe) {
                    String errorMsg = context.getString(R.string.error_invalid_symbol)+": "+symbol;
                    Utils.showLongToastHandler(context, errorMsg);
                    PrefUtils.removeStock(context, symbol);
                    validSymbol = false;
                    continue;
                }
                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

                //history = stock.getHistory(from, to, Interval.WEEKLY);

                // Note for reviewer:
                // Due to problems with Yahoo API we have commented the line above
                // and included this one to fetch the history from MockUtils
                // This should be enough as to develop and review while the API is down
                history = MockUtils.getHistory();



                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_NAME, name);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);


                context.getContentResolver()
                        .bulkInsert(
                                Contract.Quote.URI,
                                quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                context.sendBroadcast(dataUpdatedIntent);

                setStockStatus(context, STOCK_STATUS_OK);

            }
        } catch (IOException exception) {
            // TODO some symbol is added but are not valid ex : CAD
            Timber.e(exception, "Error fetching stock quotes : "+symbol);
            PrefUtils.removeStock(context, symbol);
            setStockStatus(context, STOCK_STATUS_SERVER_DOWN);
        } catch (Exception unknownException){
            Timber.e(unknownException, "Unknown Error");
            setStockStatus(context, STOCK_STATUS_UNKNOWN);
        }
    }

    public static synchronized void initialize(final Context context) {

        syncImmediately(context, QuoteSyncJob.JOB_TAG_ONE_OFF);
        schedulePeriodic(context, QuoteSyncJob.JOB_TAG_PERIODIC, QuoteSyncJob.PERIOD_SYNC);
    }

    public static void schedulePeriodic(Context context, String tag, int period) {
        Timber.d("Scheduling a periodic sync every " + period + " seconds");

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job myJob = dispatcher.newJobBuilder()
                .setService(QuoteJobService.class)
                .setTag(tag)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(period, period))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(myJob);
    }

    synchronized public static void syncImmediately(Context context, String tag) {
        Timber.d("Scheduling a immediate sync");

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver
                (context));

        Bundle bundle = new Bundle();
        Job myJob = dispatcher.newJobBuilder()
                .setService(QuoteJobService.class)
                .setTag(tag)
                .setExtras(bundle)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(0, 0))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(myJob);

    }

    synchronized public static void stopSyncJob(final Context context, String tag) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver
                (context));
        dispatcher.cancel(tag);
    }

}
