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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by newglasses on 09/08/2016
 * Listener to check for internet connectivity at alarm time
 */
public class DailyListener implements WakefulIntentService.AlarmListener {

    private static final String LOG_TAG = DailyListener.class.getSimpleName();
    public static Calendar cal;
    private SharedPreferences sharedPrefs;


    public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context context) {

        Log.e(LOG_TAG, "Inside the setAlarm()");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int hourPref = sharedPrefs.getInt("selectedHour", 20);
        int minPref = sharedPrefs.getInt("selectedMinute", 00);

        cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, hourPref);
        cal.set(Calendar.MINUTE, minPref);
        cal.set(Calendar.SECOND, 00);

        if(cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Long alertTime = cal.getTimeInMillis();

        Date date = new Date(alertTime);
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String dateFormatted = formatter.format(date);

        Log.e(LOG_TAG, "Alarm Time: " + dateFormatted);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in the background or isn't active
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime, AlarmManager.INTERVAL_DAY,
                pi);
    }

    public void sendWakefulWork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        // only when connected or while connecting...
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            boolean updateOnlyOnWifi = PreferenceHelper.getUpdateOnlyOnWifi(context);

            // if we have mobile or wifi connectivity...
            if (((netInfo.getType() == ConnectivityManager.TYPE_MOBILE) && updateOnlyOnWifi == false)
                    || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                Log.e(LOG_TAG, "We have internet, start update check directly now!");

                Intent backgroundIntent = new Intent(context, BackgroundService.class);
                WakefulIntentService.sendWakefulWork(context, backgroundIntent);


            } else {
                Log.e(LOG_TAG, "We have no internet, enable ConnectivityReceiver!");

                // enable receiver to schedule update when internet is available!
                ConnectivityReceiver.enableReceiver(context);
                // AlertFlag and update UI
                ClearSkiesService.noInternet = true;
                Intent i = new Intent(ClearSkiesService.NO_INTERNET);
                context.sendBroadcast(i);
            }
        } else {
            Log.e(LOG_TAG, "We have no internet, enable ConnectivityReceiver!");
            // enable receiver to schedule update when internet is available!
            ConnectivityReceiver.enableReceiver(context);
            // AlertFlag and update UI
            ClearSkiesService.noInternet = true;
            Intent i = new Intent(ClearSkiesService.NO_INTERNET);
            context.sendBroadcast(i);
        }
    }

    @Override
    public long getMaxAge(Context ctxt) {
        return (AlarmManager.INTERVAL_DAY + 60 * 1000);
    }


}
