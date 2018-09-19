package com.wanztudio.iak.stockhawk.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * For LEARNING
 * Created by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.wanztudio.iak.stockhawk.utils
 */

public class Utils {
    public static final String TAG = "stock_log";

    /**
     * Show a short toast Message
     * @param msg : Message to display
     */
    public static void showShortToastMessage(Context context, String msg){
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show a long toast Message
     * @param msg : Message to display
     */
    public static void showLongToastMessage(Context context, String msg){
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }


    /**
     * Show a long toast Message in a detached thread
     * @param msg : Message to display
     */
    public static void showLongToastHandler(final Context context, final String msg){
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                showLongToastMessage(context, msg);
            }
        });
    }

    /**
     * Check the network connection
     * @param context : Activity context
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }




}
