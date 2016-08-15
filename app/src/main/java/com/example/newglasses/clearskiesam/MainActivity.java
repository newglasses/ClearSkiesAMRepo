package com.example.newglasses.clearskiesam;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // for logging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    // private static final String DETAILFRAGMENT_TAG = "DFTAG";

    public static CustomAdapter testBaseCustomAdapter;
    public static Calendar cal;

    // When accessing via PreferenceManager, DefaultPrefs you get access to the one global set
    private static SharedPreferences sharedPrefs;

    private Activity thisActivity;
    private Long alarmTime;

    private TextView dateView;
    private ListView listView;
    private boolean mUseEventLayout;
    // private static final String SELECTED_KEY = "selected_position";

    private View topLevelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        topLevelLayout = findViewById(R.id.top_layout);

        dateView = (TextView) findViewById(R.id.list_date);

        dateView.setText(Utility.getFriendlyDayString(this, System.currentTimeMillis()));

        WakefulIntentService.scheduleAlarms(new DailyListener(), this, false);

        testBaseCustomAdapter = new CustomAdapter(this,
                ApplicationController.getInstance().getImageArray(),
                ApplicationController.getInstance().getTextFirstArray(),
                ApplicationController.getInstance().getTextSecondArray(),
                ApplicationController.getInstance().getTextThirdArray(),
                ApplicationController.getInstance().getStyleArray());

        // idea comes from here: http://www.androidtutorialsworld.com/android-custom-listview-example/

        listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(testBaseCustomAdapter);

        if (isFirstTime()) {
            topLevelLayout.setVisibility(View.INVISIBLE);
        }

        /*
        int hourPref = 18, minPref = 00;

        // int hourPref = sharedPrefs.getInt("selectedHour", 18);
        // int minPref = sharedPrefs.getInt("selectedMinute", 00);

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

        if (System.currentTimeMillis() < alertTime) {

            //TRYING OUT
            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextSecondArray().add("Next Update");
            ApplicationController.getInstance().getTextThirdArray().add(dateFormatted);
            ApplicationController.getInstance().getStyleArray().add("0");
        }

        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        // THIS IS WORKING...
        /*
        boolean setPrefs = false;
        if (!setPrefs) {
            setContentView(R.layout.activity_prefs);
        } else {
            setContentView(R.layout.activity_main);
        }
        */
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            //WakefulIntentService.scheduleAlarms(new DailyListener(), MainActivity.this, false);
            Intent clearSkiesIntent = new Intent(this, ClearSkiesService.class);
            startService(clearSkiesIntent);
            Log.e(LOG_TAG, "Refresh option has been selected");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // error handling saying the @Override is not necessary here
    public void onItemSelected(Uri contentUri) {
            Intent intent = new Intent(this, MainActivity.class)
                    .setData(contentUri);
            startActivity(intent);
    }

    private boolean isFirstTime() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ranBefore = sharedPrefs.getBoolean("ranBefore", false);
        String locationPref = sharedPrefs.getString("locationPref", "Roaming");
        String alertTime = sharedPrefs.getString("timePicker", "20:00");

        if (!ranBefore) {
            sharedPrefs.edit().putBoolean("ranBefore", true).apply();
            topLevelLayout.setVisibility(View.VISIBLE);

            // Put default values in the ListView
            ApplicationController.getInstance().getTextFirstArray().add("Defaults");
            ApplicationController.getInstance().getTextFirstArray().add("NEXT UPDATE");

            ApplicationController.getInstance().getTextSecondArray().add(locationPref);
            ApplicationController.getInstance().getTextSecondArray().add("Today");

            ApplicationController.getInstance().getTextThirdArray().add(alertTime);
            ApplicationController.getInstance().getTextThirdArray().add(alertTime);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

            topLevelLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    topLevelLayout.setVisibility(View.INVISIBLE);
                    return false;
                }
            });
        }
    return ranBefore;
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
