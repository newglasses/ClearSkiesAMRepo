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
 * Created by newglasses on 09/08/2016.
 */
public class DailyListener implements WakefulIntentService.AlarmListener {

    private static final String LOG_TAG = DailyListener.class.getSimpleName();
    public static Calendar cal;
    private SharedPreferences sharedPrefs;


    public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context context) {

        /*
        // register when enabled in preferences
        if (PreferenceHelper.getUpdateCheckDaily(context)) {
            Log.i("DailyListener", "Schedule update check...");

            // every day at 9 am
            Calendar calendar = Calendar.getInstance();
            // if it's after or equal 9 am schedule for next day
            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) &gt;= 9) {
                calendar.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
            }
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            mgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        }

        */

        Log.e(LOG_TAG, "Inside the setAlarm()");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int hourPref = sharedPrefs.getInt("selectedHour", 18);
        int minPref = sharedPrefs.getInt("selectedMinute", 00);

        cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, hourPref);
        cal.set(Calendar.MINUTE, minPref);
        cal.set(Calendar.SECOND, 00);

        Long alertTime = cal.getTimeInMillis();

        Date date = new Date(alertTime);
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String dateFormatted = formatter.format(date);

        Log.e(LOG_TAG, "Alarm Time: " + dateFormatted);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in the background or isn't active
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
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
                Log.d(LOG_TAG, "We have internet, start update check directly now!");

                // FROM EXAMPLE:
                Intent backgroundIntent = new Intent(context, BackgroundService.class);
                WakefulIntentService.sendWakefulWork(context, backgroundIntent);

                //AlarmTime alarmTime = new AlarmTime();
                //alarmTime.setAlarm(context);

            } else {
                Log.d(LOG_TAG, "We have no internet, enable ConnectivityReceiver!");

                // enable receiver to schedule update when internet is available!
                ConnectivityReceiver.enableReceiver(context);
                // AlertFlag and update UI
                ClearSkiesService.noInternet = true;
                Intent i = new Intent(ClearSkiesService.NO_INTERNET);
                context.sendBroadcast(i);
            }
        } else {
            Log.d(LOG_TAG, "We have no internet, enable ConnectivityReceiver!");
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
