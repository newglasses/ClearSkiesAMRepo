package com.example.newglasses.clearskiesam;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by newglasses on 09/08/2016
 * Code taken from here: https://www.sufficientlysecure.org/2012/05/24/service-daily.html
 * Includes BackgroundSErvice, DailyListener
 * My solution to this problem is to enable a BroadcastReceiver which is triggered when the connectivity in Android changes.
 * Then when Internet is available the background service is started and the receiver disables itself.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d("ConnectivityReceiver", "ConnectivityReceiver invoked...");

            // only when background update is enabled in prefs
            if (PreferenceHelper.getUpdateCheckDaily(context)) {
                Log.d("ConnectivityReceiver", "Update check daily is enabled!");

                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (!noConnectivity) {

                    ConnectivityManager cm = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();

                    // only when connected or while connecting...
                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {

                        boolean updateOnlyOnWifi = PreferenceHelper.getUpdateOnlyOnWifi(context);

                        // if we have mobile or wifi connectivity...
                        if (((netInfo.getType() == ConnectivityManager.TYPE_MOBILE) && updateOnlyOnWifi == false)
                                || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                            Log.d("ConnectivityReceiver", "We have internet, start update check and disable receiver!");

                            // Start service with wakelock by using WakefulIntentService
                            Intent backgroundIntent = new Intent(context, BackgroundService.class);
                            WakefulIntentService.sendWakefulWork(context, backgroundIntent);

                            // disable receiver after we started the service
                            disableReceiver(context);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables ConnectivityReceiver
     *
     * @param context
     */
    public static void enableReceiver(Context context) {
        ComponentName component = new ComponentName(context, ConnectivityReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    /**
     * Disables ConnectivityReceiver
     *
     * @param context
     */
    public static void disableReceiver(Context context) {
        ComponentName component = new ComponentName(context, ConnectivityReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
