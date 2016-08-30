/***
 Copyright (c) 2011 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Code tutorial: https://www.sufficientlysecure.org/2012/05/24/service-daily.html
 Source code: https://github.com/commonsguy/cwac-wakeful

 */

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
 * BroadcastReceiver enabled when the connectivity in Android changes.
 * Then when Internet is available the background service is started
 * And the receiver disables itself.
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
