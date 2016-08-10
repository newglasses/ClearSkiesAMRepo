package com.example.newglasses.clearskiesam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // for logging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static Calendar cal;

    private boolean mTwoPane;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainActivityFragment())
                        .commit();
            } else {
                mTwoPane = false;
                getSupportActionBar().setElevation(0f);
            }
        }

        WakefulIntentService.scheduleAlarms(new DailyListener(), this, false);

        // setAlarm();

        // WORKS:
        // AlarmTime alarmTime = new AlarmTime();
        // alarmTime.setAlarm(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // error handling saying the @Override is not necessary here
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    /*

    public void setAlarm() {

        Log.e(LOG_TAG, "Inside the setAlarm()");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        Intent alertIntent = new Intent(this, ClearSkiesService.AlarmReceiver.class);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in the background or isn't active
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

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



        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
        PendingIntent.getBroadcast(this, 1, alertIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));



        /*

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        // PREFERRED BY ANDROID - SEE ANDROID DOCUMENATION FOR RATIONALE
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alertTime,
                AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));


    }

    */

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    } // CODE TAKEN FROM HERE: http://www.vogella.com/tutorials/AndroidNetworking/article.html
}
