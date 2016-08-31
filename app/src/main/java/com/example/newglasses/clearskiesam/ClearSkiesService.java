package com.example.newglasses.clearskiesam;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by newglasses on 02/08/2016.
 * The ClearSkiesService manages the Clear Skies background work
 * It is triggered by the BackgroundService class
 * IntentServices run in the background, even when the app is not in the foreground
 * IntentServices have a priority only just below the foreground application
 * As a result they are less likely to be killed by the device
 */

public class ClearSkiesService extends IntentService {

    // For logging
    private static final String LOG_TAG = ClearSkiesService.class.getSimpleName();

    // Defining Broadcasts:
    // Used to notify the ClearSkiesService of the outcome of the background work
    // Depending on the outcome, the ClearSkiesService prepares the UI data accordingly
    // The ClearSkiesService does not update the UI

    protected static final String SETTINGS_UPDATED =
            "com.example.newglasses.amclearskies.SETTINGS_UPDATED";

    protected static final String NO_INTERNET =
            "com.example.newglasses.amclearskies.NO_INTERNET";

    private static final String NOTHING_TO_DECLARE =
            "com.example.newglasses.amclearskies.NOTHING_TO_DECLARE";

    private static final String AURORA =
            "com.example.newglasses.amclearskies.AURORA";

    private static final String AURORA_ISS =
            "com.example.newglasses.amclearskies.AURORA_ISS";

    private static final String ISS =
            "com.example.newglasses.amclearskies.ISS";

    private static final String OUT_OF_RANGE =
            "com.example.newglasses.amclearskies.OUT_OF_RANGE";

    private static final String MAKE_NOTIFICATION =
            "com.example.newglasses.amclearskies.MAKE_NOTIFICATION";

    // Recording the outcome of background work
    private static boolean auroraSuccess, issSuccess, weatherSuccess;
    protected static boolean noInternet;
    private static int weatherTier;
    private static String weatherColour;
    private static String textLocation;
    private static String sunsetTonight;
    private static String sunriseTom;
    private static String cloudCover;
    private static String visibility;
    private static String weatherSummary;

    private static String auroraForecast, issForecast;

    // When accessing via PreferenceManager, DefaultPrefs you get access to the one global set
    private static SharedPreferences sharedPrefs;
    // SharedPref variables
    private static String locationPref, alarmPref;
    private static boolean issPref, auroraPref, demoPref;

    private static final String NEXT_UPDATE = "NEXT UPDATE";

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(LOG_TAG, "onHandleIntent() ClearSkiesService started");

        // To begin, clear previous results of background work
        ApplicationController.getInstance().getDataToDisplay().clear();

        // Access the application's shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Default (& currently only) preference is roaming
        String locationPref = sharedPrefs.getString("location", "1");
        Log.e(LOG_TAG, "locationPref: " + locationPref);

        // Check event preferences to get overview of user for logging purposes
        auroraPref = sharedPrefs.getBoolean("aurora", true);
        Log.e(LOG_TAG, "auroraPref: " + auroraPref);
        issPref = sharedPrefs.getBoolean("iss", true);
        Log.e(LOG_TAG, "issPref: " + issPref);

        // Check demo pref
        demoPref = sharedPrefs.getBoolean("demo", false);
        Log.e(LOG_TAG, "demoPref: " + demoPref);

        // If location pref is roaming OR default (roaming), then start the GPS Service
        if (locationPref.equals("1") || locationPref.equals("-1")) {

            Log.e(LOG_TAG, "locationPref = Roaming");
            startGPSService(this);

            /* FOR TESTING PURPOSES:
            ** Log.e(LOG_TAG, "Out of Bounds - Device currently not in the UK");
            ** Intent i = new Intent(OUT_OF_RANGE);
            ** sendBroadcast(i);
            */

        // If location is fixed ** THIS IS NOT CURRENTLY AN OPTION **
        } else if (locationPref.equals("0")) {
            Log.e(LOG_TAG, "locationPref = Fixed");
            // Check the device coordinates are in the UK
            boolean insideUK = sharedPrefs.getBoolean("withinBounds", false);
            Log.e(LOG_TAG, "fixed coords withinBounds? " + insideUK);
            // If fixed coords are not inside the UK, finish service and update UI
            if (!insideUK) {
                Log.e(LOG_TAG, "Out of Bounds - Device currently not in the UK");
                Intent i = new Intent(OUT_OF_RANGE);
                sendBroadcast(i);
            // If fixed coords are inside the UK, continue the service
            } else {
                if (insideUK) {
                    startGPSLocationService(this);
                }
            }
        }
    } // onHandleIntent

    /***********************************************************************************************
     ************************************************************************************************
                    BROADCAST RECEIVERS THAT DEAL WITH THIRD-PARTY DATA SERVICES
     ************************************************************************************************
     ***********************************************************************************************/

    // Starts if GPS_DONE Broadcast is received from the GPSService:
    public static class GPSRetrievedReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG =
                ClearSkiesService.GPSRetrievedReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My GPS receiver received a broadcast");

            // Access SharedPrefs to
            // check the device coordinates are in the UK
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            boolean insideUK = sharedPrefs.getBoolean("withinBounds", false);
            Log.e(LOG_TAG, "withinBounds:" + insideUK);

            // Dummy variable for testing: insideUK = false;

            if (insideUK) {
                startGPSLocationService(context);
            } else {
                // if device is outside the UK, finish the service and update the UI
                Log.e(LOG_TAG, "Out of Bounds - Device currently not in the UK");
                Intent i  = new Intent(OUT_OF_RANGE);
                context.sendBroadcast(i);
            }
        }
        public GPSRetrievedReceiver () {
            super();
        }
    }

    // Starts if GPS_LOCATION_DONE Broadcast is received from the GPSLocationService:
    public static class GPSLocationReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG =
                ClearSkiesService.GPSLocationReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My GPS Location receiver received a broadcast");

            try {
                // Parse the JSON data
                // Update SharedPrefs with the city name of the coordinates
                String locationData = parseJSON(context, "GPS_Location_File");
                parseLocation(locationData, context);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // start the weather service to check for clear skies
            startWeatherService(context);

        }
        public GPSLocationReceiver () {
            super();
        }
    }

    // Starts if WEATHER_DONE Broadcast is received from the WeatherService:
    public static class WeatherDownloadReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG =
                ClearSkiesService.WeatherDownloadReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My weather receiver received a broadcast");

            // Access user event preferences
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            auroraPref = sharedPrefs.getBoolean("aurora", true);
            issPref = sharedPrefs.getBoolean("iss", true);

            try {
                String weatherData = parseJSON(context, "Forecast_IO_File");
                weatherSuccess = parseWeather(weatherData, context);
                Log.e(LOG_TAG, "Weather is returned as a success: " + weatherSuccess);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // FOR DEMO MODE
            if (demoPref) {
                Log.e(LOG_TAG, "IN DEMO MODE");
                // update UI with successful ISS regardless of weather result
                sharedPrefs.edit().putString("onForecast", "1471558560").apply();
                sharedPrefs.edit().putInt("weatherTier", 0).apply();
                Intent i = new Intent(ISS);
                context.sendBroadcast(i);
                // raise a notification regardless of weather result
                Intent iN = new Intent(MAKE_NOTIFICATION);
                context.sendBroadcast(iN);

            } else if (weatherSuccess) {
                    if (auroraPref){
                        Log.e(LOG_TAG, "Aurora pref is: " + auroraPref);
                        startAuroraWatchService(context);
                    } else if (issPref) {
                        Log.e(LOG_TAG, "ISS pref is: " + issPref);
                        startOpenNotifyService(context);
                    } else {
                        Log.e(LOG_TAG, "No events selected in prefs");
                        // end the service and upate the UI
                        Intent i = new Intent(NOTHING_TO_DECLARE);
                        context.sendBroadcast(i);
                    }

            } else {
                Log.e(LOG_TAG, "No Clear Skies or No Events Selected");
                // end the service and upate the UI
                Intent i = new Intent(NOTHING_TO_DECLARE);
                context.sendBroadcast(i);

            /* FOR TESTING PURPOSES:
            ** Intent iN = new Intent(MAKE_NOTIFICATION);
            ** context.sendBroadcast(iN);
            */

            }
        }
        public WeatherDownloadReceiver () {
            super();
        }
    }

    // Starts if AURORA_DONE Broadcast is received from the AuroraWatchService:
    public static class AuroraDownloadReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG =
                ClearSkiesService.AuroraDownloadReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My Aurora receiver received a broadcast");

            // Access user event preferences
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            issPref = sharedPrefs.getBoolean("iss", true);

            auroraSuccess = parseAurora(context);
            // TESTING:
            // auroraSuccess = true;
            Log.e(LOG_TAG, "Aurora result success = " + auroraSuccess);

            if(issPref) {
                Log.e(LOG_TAG, "ISS pref is: " + issPref);
                startOpenNotifyService(context);
            } else {
                if (auroraSuccess) {
                    Intent i = new Intent(AURORA);
                    context.sendBroadcast(i);

                    Intent iN = new Intent(MAKE_NOTIFICATION);
                    context.sendBroadcast(iN);
                } else {
                    // create a broadcast to advise time to update the UI
                    // do not need to send broadcast to create notification
                    Intent i = new Intent(NOTHING_TO_DECLARE);
                    context.sendBroadcast(i);
                }
            }
        }
        public AuroraDownloadReceiver () {
            super();
        }
    }

    // Starts if ISS_DONE Broadcast is received from the OpenNotifyService:
    public static class OpenNotifyDownloadReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG =
                ClearSkiesService.OpenNotifyDownloadReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My ISS receiver received a broadcast");

            try {
                String openNotifyData = parseJSON(context, "Open_Notify_File");
                issSuccess = parseOpenNotify(openNotifyData, context);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (issSuccess) {

                if (auroraSuccess) {
                    Intent i = new Intent(AURORA_ISS);
                    context.sendBroadcast(i);
                    Intent iN = new Intent(MAKE_NOTIFICATION);
                    context.sendBroadcast(iN);
                } else {
                    Intent i = new Intent(ISS);
                    context.sendBroadcast(i);
                    Intent iN = new Intent(MAKE_NOTIFICATION);
                    context.sendBroadcast(iN);
                }

            } else if (auroraSuccess){
                Intent i = new Intent(AURORA);
                context.sendBroadcast(i);
                Intent iN = new Intent(MAKE_NOTIFICATION);
                context.sendBroadcast(iN);

            } else {
                Intent iA = new Intent(NOTHING_TO_DECLARE);
                context.sendBroadcast(iA);
            }
        }
        public OpenNotifyDownloadReceiver () {
            super();
        }
    }

    /***********************************************************************************************
     ************************************************************************************************
                        BROADCAST RECEIVERS THAT DEAL WITH UPDATING THE UI
     ************************************************************************************************
     ***********************************************************************************************/

    // Starts if SETTINGS_UPDATED Broadcast is received from the SettingsActivity:
    public static class SettingsUpdatedReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.SettingsUpdatedReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My SettingsUpdated receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");
            int hourPref = sharedPrefs.getInt("selectedHour", 20);
            int minPref = sharedPrefs.getInt("selectedMinute", 00);

            String nextUpdate;

            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, hourPref);
            cal.set(Calendar.MINUTE, minPref);
            cal.set(Calendar.SECOND, 00);

            if(cal.getTimeInMillis() < System.currentTimeMillis()) {
                nextUpdate = "Tomorrow";
            } else {
                nextUpdate = "Today";
            }

            // Clear data from the ArrayLists used to update the UI
            Utility.clearArrayLists();

            // Repopulate the ArrayLists used to update the UI
            ApplicationController.getInstance().getTextFirstArray().add(NEXT_UPDATE);
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");

            // Notify the CustomAdapter that the dataset has changed
            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public SettingsUpdatedReceiver () {
            super();
        }

    }

    // Starts if NOTHING_TO_DECLARE Broadcast is received from the ClearSkiesService:
    public static class NothingToDeclareReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.NothingToDeclareReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My NothingToDeclare receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");
            textLocation = sharedPrefs.getString("textLocation", "location unavailable");
            int hourPref = sharedPrefs.getInt("selectedHour", 20);
            int minPref = sharedPrefs.getInt("selectedMinute", 00);

            String nextUpdate;

            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, hourPref);
            cal.set(Calendar.MINUTE, minPref);
            cal.set(Calendar.SECOND, 00);

            if(cal.getTimeInMillis() < System.currentTimeMillis()) {
                nextUpdate = "Tomorrow";
            } else {
                nextUpdate = "Today";
            }

            // LOGGING: LOOKING AT THE GATHERED DATA
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS
            ApplicationController.getInstance().getTextFirstArray().add(textLocation);
            ApplicationController.getInstance().getTextFirstArray().add(NEXT_UPDATE);

            ApplicationController.getInstance().getTextSecondArray().add("Nothing");
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add("To Declare");
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public NothingToDeclareReceiver () {
            super();
        }
    }

    // Starts if NO_INTERNET Broadcast is received from the ClearSkiesService:
    public static class NoInternetReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.NoInternetReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My NoInternet receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");

            // LOGGING: LOOKING AT THE GATHERED DATA
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Network");

            ApplicationController.getInstance().getTextSecondArray().add("Access");

            ApplicationController.getInstance().getTextThirdArray().add("Required");

            ApplicationController.getInstance().getStyleArray().add("0");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public NoInternetReceiver () {
            super();
        }

    }

    // Starts if OUT_OF_RANGE Broadcast is received from the ClearSkiesService:
    public static class OutOfRangeReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.OutOfRangeReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My OutOfRange receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");
            int hourPref = sharedPrefs.getInt("selectedHour", 20);
            int minPref = sharedPrefs.getInt("selectedMinute", 00);

            String nextUpdate;

            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, hourPref);
            cal.set(Calendar.MINUTE, minPref);
            cal.set(Calendar.SECOND, 00);

            if(cal.getTimeInMillis() < System.currentTimeMillis()) {
                nextUpdate = "Tomorrow";
            } else {
                nextUpdate = "Today";
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS
            ApplicationController.getInstance().getTextFirstArray().add("Currently");
            ApplicationController.getInstance().getTextFirstArray().add(NEXT_UPDATE);

            ApplicationController.getInstance().getTextSecondArray().add("Out");
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add("of Range");
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }
        }
        public OutOfRangeReceiver () {
            super();
        }
    }

    // Starts if AURORA Broadcast is received from the ClearSkiesService:
    public static class AuroraSuccessReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.AuroraSuccessReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My AuroraSuccess receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            alarmPref = sharedPrefs.getString("timePicker", "20:00");
            textLocation = sharedPrefs.getString("textLocation", "location unavailable");
            Log.e(LOG_TAG, "location in shared prefs: " + textLocation);

            long sunset = sharedPrefs.getLong("sunsetTonight", 0000L);

            Date sunsetD = new Date((sunset * 1000L));
            DateFormat df = new SimpleDateFormat("HH:mm");
            sunsetTonight = df.format(sunsetD);
            Log.e(LOG_TAG, "sunset (long): " + sunset + ", sunset (df): " + sunsetTonight);

            long sunrise = sharedPrefs.getLong("sunriseTom", 0000L);
            Date sunriseD = new Date(sunrise * 1000L);
            sunriseTom = df.format(sunriseD);
            Log.e(LOG_TAG, "sunrise (long): " + sunrise + ", sunrise (df): " + sunriseTom);

            auroraForecast = sharedPrefs.getString("auroraForecast", "auroraForecast error");
            Log.e(LOG_TAG, "auroraForecast: " + auroraForecast);

            weatherTier = sharedPrefs.getInt("weatherTier", weatherTier);
            Log.e(LOG_TAG, "weatherTier: " + weatherTier);

            if (weatherTier == 0) {
                weatherSummary = "GREEN ALERT: Clear Skies Likely";
            } else if (weatherTier == 1) {
                weatherSummary = "AMBER ALERT: Clear Skies Possible";
            } else {
                weatherSummary = "Weather Error";
            }

            int hourPref = sharedPrefs.getInt("selectedHour", 20);
            int minPref = sharedPrefs.getInt("selectedMinute", 00);
            Log.e(LOG_TAG, "alarmTime in shared prefs: " + hourPref + ":" + minPref);


            String nextUpdate;

            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, hourPref);
            cal.set(Calendar.MINUTE, minPref);
            cal.set(Calendar.SECOND, 00);

            if(cal.getTimeInMillis() < System.currentTimeMillis()) {
                nextUpdate = "Tomorrow";
            } else {
                nextUpdate = "Today";
            }
            Log.e(LOG_TAG, "Next Update: " + nextUpdate);

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS
            ApplicationController.getInstance().getTextFirstArray().add(textLocation);
            ApplicationController.getInstance().getTextFirstArray().add(weatherSummary);
            ApplicationController.getInstance().getTextFirstArray().add(NEXT_UPDATE);

            ApplicationController.getInstance().getTextSecondArray().add("Aurora");
            ApplicationController.getInstance().getTextSecondArray().add(sunsetTonight);
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add(auroraForecast);
            ApplicationController.getInstance().getTextThirdArray().add(sunriseTom);
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }
        }

        public AuroraSuccessReceiver () {
            super();
        }
    }

    // Starts if ISS Broadcast is received from the ClearSkiesService:
    public static class IssSuccessReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.IssSuccessReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My IssSuccess receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            alarmPref = sharedPrefs.getString("timePicker", "20:00");
            textLocation = sharedPrefs.getString("textLocation", "location unavailable");

            Log.e(LOG_TAG, "location in shared prefs: " + textLocation);

            long sunset = sharedPrefs.getLong("sunsetTonight", 0000L);

            Date sunsetD = new Date((sunset * 1000L));
            DateFormat df = new SimpleDateFormat("HH:mm");
            sunsetTonight = df.format(sunsetD);
            Log.e(LOG_TAG, "sunset (long): " + sunset + ", sunset (df): " + sunsetTonight);

            long sunrise = sharedPrefs.getLong("sunriseTom", 0000L);
            Date sunriseD = new Date(sunrise * 1000L);
            sunriseTom = df.format(sunriseD);
            Log.e(LOG_TAG, "sunrise (long): " + sunrise + ", sunrise (df): " + sunriseTom);

            String onForecast = sharedPrefs.getString("onForecast", "onForecast error");
            long onForecastLong = Long.valueOf(onForecast);
            Log.e(LOG_TAG, "long value of the issforecast: " + onForecastLong);
            Date onForecastD = new Date(onForecastLong * 1000L);
            issForecast = df.format(onForecastD);
            Log.e(LOG_TAG, "onForecast (String): " + onForecast + "onForecast (df): " + issForecast);

            weatherTier = sharedPrefs.getInt("weatherTier", weatherTier);

            if (weatherTier == 0) {
                weatherSummary = "GREEN ALERT: Clear Skies Likely";
            } else if (weatherTier == 1) {
                weatherSummary = "AMBER ALERT: Clear Skies Possible";
            } else {
                weatherSummary = "Error";
            }
            Log.e(LOG_TAG, "weatherTier: " + weatherTier);

            int hourPref = sharedPrefs.getInt("selectedHour", 20);
            int minPref = sharedPrefs.getInt("selectedMinute", 00);
            Log.e(LOG_TAG, "alarmTime in shared prefs: " + hourPref + ":" + minPref);

            String nextUpdate;

            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, hourPref);
            cal.set(Calendar.MINUTE, minPref);
            cal.set(Calendar.SECOND, 00);

            if(cal.getTimeInMillis() < System.currentTimeMillis()) {
                nextUpdate = "Tomorrow";
            } else {
                nextUpdate = "Today";
            }
            Log.e(LOG_TAG, "Next Update: " + nextUpdate);

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add(textLocation);
            ApplicationController.getInstance().getTextFirstArray().add(weatherSummary);
            ApplicationController.getInstance().getTextFirstArray().add(NEXT_UPDATE);

            ApplicationController.getInstance().getTextSecondArray().add("ISS");
            ApplicationController.getInstance().getTextSecondArray().add(sunsetTonight);
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add(issForecast);
            ApplicationController.getInstance().getTextThirdArray().add(sunriseTom);
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }
        }

        public IssSuccessReceiver () {
            super();
        }

    }

    // Starts if AURORA_ISS Broadcast is received from the ClearSkiesService:
    public static class AuroraIssReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.AuroraIssReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My AuroraIss receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);;
            alarmPref = sharedPrefs.getString("timePicker", "20:00");
            textLocation = sharedPrefs.getString("textLocation", "location unavailable");

            long sunset = sharedPrefs.getLong("sunsetTonight", 0000L);

            Date sunsetD = new Date((sunset * 1000L));
            DateFormat df = new SimpleDateFormat("HH:mm");
            sunsetTonight = df.format(sunsetD);
            Log.e(LOG_TAG, "sunset (long): " + sunset + ", sunset (df): " + sunsetTonight);

            long sunrise = sharedPrefs.getLong("sunriseTom", 0000L);
            Date sunriseD = new Date(sunrise * 1000L);
            sunriseTom = df.format(sunriseD);
            Log.e(LOG_TAG, "sunrise (long): " + sunrise + ", sunrise (df): " + sunriseTom);

            String onForecast = sharedPrefs.getString("onForecast", "onForecast error");
            long onForecastLong = Long.valueOf(onForecast);
            Log.e(LOG_TAG, "long value of the issforecast: " + onForecastLong);
            Date onForecastD = new Date(onForecastLong * 1000L);
            issForecast = df.format(onForecastD);
            Log.e(LOG_TAG, "onForecast (String): " + onForecast + "onForecast (df): " + issForecast);

            auroraForecast = sharedPrefs.getString("auroraForecast", "auroraForecast error");
            Log.e(LOG_TAG, "auroraForecast: " + auroraForecast);

            weatherTier = sharedPrefs.getInt("weatherTier", weatherTier);
            Log.e(LOG_TAG, "weatherTier: " + weatherTier);

            if (weatherTier == 0) {
                weatherSummary = "GREEN ALERT: Clear Skies Likely";
            } else if (weatherTier == 1) {
                weatherSummary = "AMBER ALERT: Clear Skies Possible";
            } else {
                weatherSummary = "Weather Error";
            }

            int hourPref = sharedPrefs.getInt("selectedHour", 20);
            int minPref = sharedPrefs.getInt("selectedMinute", 00);
            Log.e(LOG_TAG, "alarmTime in shared prefs: " + hourPref + ":" + minPref);

            String nextUpdate;

            Calendar cal=Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, hourPref);
            cal.set(Calendar.MINUTE, minPref);
            cal.set(Calendar.SECOND, 00);

            if(cal.getTimeInMillis() < System.currentTimeMillis()) {
                nextUpdate = "Tomorrow";
            } else {
                nextUpdate = "Today";
            }
            Log.e(LOG_TAG, "Next Update: " + nextUpdate);

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add(textLocation);
            ApplicationController.getInstance().getTextFirstArray().add(textLocation);
            ApplicationController.getInstance().getTextFirstArray().add(weatherSummary);
            ApplicationController.getInstance().getTextFirstArray().add("NEXT UPDATE");

            ApplicationController.getInstance().getTextSecondArray().add("Aurora");
            ApplicationController.getInstance().getTextSecondArray().add("ISS");
            ApplicationController.getInstance().getTextSecondArray().add(sunsetTonight);
            ApplicationController.getInstance().getTextSecondArray().add(nextUpdate);

            ApplicationController.getInstance().getTextThirdArray().add(auroraForecast);
            ApplicationController.getInstance().getTextThirdArray().add(issForecast);
            ApplicationController.getInstance().getTextThirdArray().add(sunriseTom);
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }
        }

        public AuroraIssReceiver () {
            super();
        }

    }

    /***********************************************************************************************
     ************************************************************************************************
                            BROADCAST RECEIVER THAT RAISES THE NOTIFICATION
     ************************************************************************************************
     ***********************************************************************************************/

    // // Starts if NO_INTERNET Broadcast is received from the ClearSkiesService:
    public static class MakeNotificationReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.MakeNotificationReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG_TAG, "My MakeNotification receiver received a broadcast");

            String msg = "Clear Skies Tonight!";

            String msgText;

            if (auroraSuccess == true && issSuccess == true) {
                msgText = "Aurora, ISS";
            } else if (auroraSuccess) {
                msgText = "Aurora";
            } else {
                msgText = "ISS";
            }
            // For accessibility services
            String msgAlert = "Clear Skies Tonight!";

            // Create notification using dummy data
            ClearSkiesService.createNotification(context, msg , msgText, msgAlert);

        }
        public MakeNotificationReceiver () {
            super();
        }
    }

    /***********************************************************************************************
    ************************************************************************************************
                                    PARSING AND TESTING THE DOWNLOADED DATA
    ************************************************************************************************
    ***********************************************************************************************/

    public static boolean parseAurora(Context context) {
        // Code adapted from:
        // http://www.newthinktank.com/2014/12/make-android-apps-18/#sthash.BJ58qBzp.dpuf

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        double lat = Double.valueOf(sharedPrefs.getString("lat", "no lat available"));
        double lng = Double.valueOf(sharedPrefs.getString("lng", "no lng available"));

        // Will build the String from the local file
        String auroraForecast = "";
        // Array to hold parsed XML document
        String [] xmlPullParserArray = new String[6];
        int parserArrayIncrement = 0;

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = context.openFileInput("Aurora_Watch_File");
            // Gets an input stream for reading data
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            // Used to read the data in small bytes to minimize system load
            BufferedReader bufferedReader = new BufferedReader(isr);
            // Create pull parser to parse XML documents
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            // parser supports XML namespaces
            factory.setNamespaceAware(true);
            // provides the methods needed to parse XML documents
            XmlPullParser parser = factory.newPullParser();
            // InputStreamReader converts bytes of data into a stream of characters
            parser.setInput(bufferedReader);
            // Passes the parser and the first tag into the XML document for processing
            beginDocument(parser, "aurorawatch");
            // Get the currently targeted event type, which starts as START_DOCUMENT
            int eventType;
            do {
                // Cycles through elements in the XML document while neither a start nor
                // end tag are found
                nextElement(parser);
                // Switch to the next element
                parser.next();
                // Get the current event type
                eventType = parser.getEventType();
                // Check if a value was found between 2 tags
                if (eventType == XmlPullParser.TEXT){
                    // Get the text from between the tags
                    String valueFromXML = parser.getText();
                    // Store it in an array with the corresponding tag value
                    xmlPullParserArray[parserArrayIncrement] = valueFromXML;
                    parserArrayIncrement++;
                }
            } while (eventType != XmlPullParser.END_DOCUMENT);

            auroraForecast = xmlPullParserArray[1];

            // for logging: show what result was achieved from the download
            ApplicationController.getInstance().getDataToDisplay().add(auroraForecast);

            // check whether a possible siting is likely
            if (auroraForecast.equals("No significant activity")) {
                Log.e(LOG_TAG, "Nothing to show: " + auroraForecast);
                auroraSuccess = false;
                sharedPrefs.edit().putString("auroraForecast", auroraForecast).apply();

                /* FOR TESTING PURPOSES:
                ** sharedPrefs.edit().putString("auroraForecast", auroraForecast).apply();
                ** auroraSuccess = true;
                */

            } else if (auroraForecast.equals("Minor geomagnetic activity")) {
                // Aurora may be visible by eye from Scotland
                Log.e(LOG_TAG, "Aurora may be visible by eye from Scotland: " + auroraForecast);
                // Put in test of geo coordinates here
                if ((lat >= 55.0 && lat <= 60.0) &&
                        (lng >= -8.0 && lng <= -2.0)) {
                    auroraSuccess = true;
                    // Update sharedPrefs
                    sharedPrefs.edit().putString("auroraForecast", auroraForecast).apply();
                }
            } else if (auroraForecast.equals("Amber alert: possible aurora")) {
                Log.e(LOG_TAG, "Aurora is likely to be visible " +
                        "by eye from S, NE, NI: " + auroraForecast);
                // Put in test of geo coordinates here
                if ((lat >= 54.0 && lat <= 60.0) &&
                        (lng >= -9.0 && lng <= 0.0)) {
                    auroraSuccess = true;
                    // Update sharedPrefs
                    sharedPrefs.edit().putString("auroraForecast", auroraForecast).apply();

                } else if (auroraForecast.equals("Red alert: aurora likely")) {
                    Log.e(LOG_TAG, "Aurora is likely to be visible " +
                            "from anywhere in the UK: " + auroraForecast);
                    // Flag aurora success
                    auroraSuccess = true;
                    // updateSharedPrefs
                    sharedPrefs.edit().putString("auroraForecast", auroraForecast).apply();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return auroraSuccess;
    }

    public static String parseJSON(Context context, String fileName) throws JSONException, IOException {
        // Code adapted from:
        // http://www.newthinktank.com/2014/12/make-android-apps-18/#sthash.BJ58qBzp.dpuf

        // Will build the String from the local file
        StringBuilder sb = new StringBuilder();

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = context.openFileInput(fileName);

            // Gets an input stream for reading data
            // InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            // Used to read the data in small bytes to minimize system load
            BufferedReader bufferedReader = new BufferedReader(isr);

            if (bufferedReader != null) {
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static boolean parseOpenNotify(String fileData, Context context) throws JSONException, IOException {

        JSONObject satResponse = new JSONObject(fileData);

        ArrayList<JSONObject> passes = new ArrayList<>();
        ArrayList<Long> passRiseTime = new ArrayList<>();

        StringBuilder passList = new StringBuilder();

        int totPasses = satResponse.getJSONObject("request").getInt("passes");
        Log.e(LOG_TAG, "Total upcoming passes: " + totPasses);

        for (int i = 0; i < totPasses; i++) {
            JSONObject satEntry = satResponse.getJSONArray("response").getJSONObject(i);
            passes.add(satEntry);
        }

        // find out current sunset time
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long sunset = sharedPrefs.getLong("sunsetTonight", 00000L);
        long sunriseT = sharedPrefs.getLong("sunriseTom", 00000L);

        Log.e(LOG_TAG, "Accessed sunsetTime from sharedPrefs: " + String.valueOf(sunset));

        Date sunsetDate = new Date(sunset * 1000L);
        long today = System.currentTimeMillis();
        Date todayDate = new Date(today);
        DateFormat df = new SimpleDateFormat("d MMM");
        String sunsetDf = df.format(sunsetDate);
        String todayDf = df.format(todayDate);
        Log.e(LOG_TAG, "sunset (long) " + sunset + " and today (long) " + today);
        Log.e(LOG_TAG, "sunset date " + sunsetDf + " and today date " + todayDf);

        // unix timestamp * 1000L to "upcast" to Long and multiply by 1000 since since
        // Java is expecting millisecs
        //taking twilight (1hr) into account but varies according to coords
        Date darkD = new Date((sunset*1000L) + (60*60*1000));
        DateFormat dfDark = new SimpleDateFormat("HH mm");
        String darkDf = dfDark.format(darkD);
        Log.e(LOG_TAG, "dark date " + darkDf);

        // It is possible for the ISS to pass over more than once in the same night
        for (int i = 0; i < passes.size(); i++) {
            long risetime = passes.get(i).getLong("risetime");
            Date risetimeDate = new Date(risetime * 1000L);
            Log.e(LOG_TAG, "risetime " + risetime);
            Log.e(LOG_TAG, "risetimeDate: " + risetimeDate);
            String risetimeDf = df.format(risetimeDate);
            Log.e(LOG_TAG, "risetimeDf: " + risetimeDf);
            // only want results relevant to today and between the hours of sunset & sunrise
            if ((risetimeDf.equals(todayDf) && (passes.get(i).getLong("risetime") > sunset) &&
                    (passes.get(i).getLong("risetime") < sunriseT))) {

                Log.e(LOG_TAG, "pass df: " + sunsetDf + " today df: " + todayDf);
                Log.e(LOG_TAG, "sunset: " + sunset + " risetime: " + risetime);
                passRiseTime.add(passes.get(i).getLong("risetime"));
                // Flag that there is a result
                issSuccess = true;
                // Update the sharedPrefs that will populate the UI
                sharedPrefs.edit().putString("onForecast", passRiseTime.get(0).toString()).apply();
            } else {
                issSuccess = false;

                /* FOR TESTING PURPOSES:
                ** issSuccess = true;
                ** passRiseTime.add(passes.get(0).getLong("risetime"));
                * sharedPrefs.edit().putString("onForecast", passRiseTime.get(0).toString()).apply();
                * sharedPrefs.edit().putString("onForecast", "1471558560").apply();
                */

            }
        }

        // for logging: check what has been downloaded & parsed
        for (int i = 0; i < passes.size(); i++) {
            passList.append(passes.get(i).toString() + " * ");
        }
        // for logging: check the successful data
        ApplicationController.getInstance().getDataToDisplay().add(passList.toString());
        Log.e(LOG_TAG, passList.toString());

        return issSuccess;
    }

    public static boolean parseWeather(String fileData, Context context) throws JSONException, IOException {

        JSONObject object = new JSONObject(fileData);
        JSONObject daily = object.getJSONObject("daily");
        JSONArray dailyForecast = daily.getJSONArray("data");
        JSONObject todayForecast = dailyForecast.getJSONObject(1);

        //UNIX TIME
        long time = todayForecast.getLong("time");
        long sunsetTom = todayForecast.getLong("sunsetTime");
        long sunriseTom = todayForecast.getLong("sunriseTime");

        double precipProb = todayForecast.getDouble("precipProbability");
        double humidityProb = todayForecast.getDouble("humidity");
        double visibilityMiles = todayForecast.getDouble("visibility");
        double cloudCoverPercent = todayForecast.getDouble("cloudCover");

        Log.e(LOG_TAG, "findings: time: "
                + time + " sunset: " + sunsetTom + " sunrise: " + sunriseTom +
                " precipProb: " + precipProb + " humidityProb : " + humidityProb +
                " visibilityMiles: " + visibilityMiles + " cloudCoverPercent : " + cloudCoverPercent);

        JSONObject yesterdayForecast = dailyForecast.getJSONObject(0);
        long sunsetTonight = yesterdayForecast.getLong("sunsetTime");

        // DETERMINE CLEAR SKIES

        if (cloudCoverPercent <= 0.3 && precipProb <= 0.25
                && humidityProb <= 0.8 && visibilityMiles >= 9) {
            weatherSuccess = true;
            weatherTier = 0;
            weatherColour = "GREEN";

        } else if (cloudCoverPercent <= 0.5 && precipProb <= 0.5
                && humidityProb <= 0.9 && visibilityMiles >= 6) {
            weatherSuccess = true;
            weatherTier = 1;
            weatherColour = "AMBER";

        } else {
            weatherSuccess = false;
            weatherTier = 2;
            weatherColour = "RED";

            /* FOR TESTING PURPOSES:
                ** weatherSuccess = true;
                ** weatherTier = 0;
                ** weatherColour = "GREEN";
                */
        }

        // CURRENTLY UI DOES NOT SHOW CLOUD COVER OR VISIBILITY RESULT
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPrefs.edit().putLong("sunsetTonight", sunsetTonight).apply();
        sharedPrefs.edit().putLong("sunriseTom", sunriseTom).apply();
        sharedPrefs.edit().putString("cloudCover", String.valueOf(cloudCoverPercent)).apply();
        sharedPrefs.edit().putString("visibility", String.valueOf(visibilityMiles)).apply();
        sharedPrefs.edit().putInt("weatherTier", weatherTier).apply();

        ApplicationController.getInstance().getDataToDisplay()
                .add("Weather Tier: " + weatherTier + ", " + weatherColour);

        return weatherSuccess;
    }

    public static void parseLocation(String fileData, Context context) throws JSONException, IOException {

        JSONObject object = new JSONObject(fileData);

        JSONObject addressComponents = object.getJSONArray("results").getJSONObject(1);
        JSONObject city = addressComponents.getJSONArray("address_components").getJSONObject(2);
        String location = city.getString("short_name");

        Log.e("Inside JSONParser", "parsed: " + location);
        sharedPrefs.edit().putString("textLocation", location).apply();
    }


    public static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {

        int type;

        // next() advances to the next element in the XML document being a starting or ending tag, or
        // a value or the END_DOCUMENT

        while ((type=parser.next()) != parser.START_TAG && type != parser.END_DOCUMENT) {;}

        // Throw an error if a start tag isn't found

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        // Verify that the tag passed in is the first tag in the XML document

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException(
                    "Unexpected start tag: found " + parser.getName() +
                            ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {

        int type;

        // Cycles through elements in the XML document while neither a start nor end tag are found

        while ((type = parser.next()) != parser.START_TAG && type != parser.END_DOCUMENT) {
            ;
        }
    }

    /***********************************************************************************************
     ************************************************************************************************
                                    CREATING THE NOTIFICATION
     ************************************************************************************************
     ***********************************************************************************************/
    // Raising a notification
    // Code adapted from tutorial at:
    // http://www.newthinktank.com/2014/12/make-android-apps-19/#sthash.qQhbHKzz.dpuf

    // Starts if MAKE_NOTIFICATION Broadcast is received from the ClearSkiesService:
    public static void createNotification(Context context, String msg, String msgText, String msgAlert){

        // Define an Intent and an action to perform with it by another application
        PendingIntent notificIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        // Builds a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.cs_smaller_white)
                        .setContentTitle(msg)
                        .setTicker(msgAlert)
                        .setContentText(msgText);

        // Defines the Intent to fire when the notification is clicked
        mBuilder.setContentIntent(notificIntent);

        // Set the default notification option
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        // Auto cancels the notification when clicked on in the task bar
        mBuilder.setAutoCancel(true);

        // Gets a NotificationManager which is used to notify the user of the background event
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        mNotificationManager.notify(1, mBuilder.build());

    }

    /***********************************************************************************************
     ************************************************************************************************
                        EXPLICIT INTENTS TO START EACH OF THE INTENT SERVICES
     ************************************************************************************************
     ***********************************************************************************************/

    public static void startGPSService(Context context) {

        // Create an intent to run the IntentService in the background & start it
        Intent gpsIntent = new Intent(context, GPSService.class);
        context.startService(gpsIntent);
    }

    public static void startGPSLocationService(Context context) {

        // Create an intent to run the IntentService in the background & start it
        Intent gpsLocationIntent = new Intent(context, GPSLocationService.class);
        context.startService(gpsLocationIntent);
    }

    public static void startAuroraWatchService(Context context) {

        // Create an intent to run the IntentService in the background & start it
        Intent auroraIntent = new Intent(context, AuroraWatchService.class);
        context.startService(auroraIntent);
    }

    public static void startOpenNotifyService(Context context) {

        // Create an intent to run the IntentService in the background & start it
        Intent openNotifyIntent = new Intent(context, OpenNotifyService.class);
        context.startService(openNotifyIntent);
    }

    public static void startWeatherService(Context context) {

        // Create an intent to run the IntentService in the background & start it
        Intent weatherIntent = new Intent(context, WeatherService.class);
        context.startService(weatherIntent);
    }

    public ClearSkiesService(String name) {
        super(name);
    }

    // Validates resource references inside Android XML files
    public ClearSkiesService() {
        super(ClearSkiesService.class.getName());
    }
}
