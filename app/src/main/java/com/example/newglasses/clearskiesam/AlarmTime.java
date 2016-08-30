package com.example.newglasses.clearskiesam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by newglasses on 09/08/2016.
 */
public class AlarmTime {

    private static final String LOG_TAG = AlarmTime.class.getSimpleName();
    public static Calendar cal;
    private SharedPreferences sharedPrefs;

    public AlarmTime() {

    }

    public void setAlarm(Context context) {

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

        /*
        // Starts the alarm at the time request, and repeats it every minute
        // GOOD FOR TESTING - EXAMPLE USER TURNING THE ALARM OFF
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime,
                1000 * 60, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        */


    }

}
