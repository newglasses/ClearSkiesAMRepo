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

        //cal.set(Calendar.MONTH,6);
        //cal.set(Calendar.YEAR,2011);
        //cal.set(Calendar.DAY_OF_MONTH,29);
        cal.set(Calendar.HOUR_OF_DAY, hourPref);
        cal.set(Calendar.MINUTE, minPref);
        cal.set(Calendar.SECOND, 00);

        // Define a time value of 5 seconds
        // THIS WORKS: Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;
        // Long intervalTime = Long.valueOf(5000);
        Long alertTime = cal.getTimeInMillis();

        Date date = new Date(alertTime);
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String dateFormatted = formatter.format(date);

        Log.e(LOG_TAG, "Alarm Time: " + dateFormatted);


        // Define our intention of executing the ClearSkiesService class
        Intent alertIntent = new Intent(context, ClearSkiesService.AlarmReceiver.class);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in the background or isn't active
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active (don't start a new one)
        /* WORKS:
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        /*
        // Starts the alarm at the time request, and repeats it every minute
        // GOOD FOR TESTING - EXAMPLE USER TURNING THE ALARM OFF
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime,
                1000 * 60, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        */

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                PendingIntent.getBroadcast(context, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));



        /*

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        // PREFERRED BY ANDROID - SEE ANDROID DOCUMENATION FOR RATIONALE
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alertTime,
                AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        */


    }

}
