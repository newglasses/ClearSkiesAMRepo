package com.example.newglasses.clearskiesam;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import com.android.volley.VolleyLog;

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
import java.util.Date;
import java.util.Locale;

/**
 * Created by newglasses on 02/08/2016.
 * The ClearSkiesService manages the background work required for the application
 * It is triggered by the AlarmManager in the MainActivity
 * IntentServices run in the background, even when the app is not in the foreground
 * IntentServices have a priority only just below the foreground application
 * As a result they are unlikely to be killed by the device
 */

public class ClearSkiesService extends IntentService {

    // For logging
    private static final String LOG_TAG = ClearSkiesService.class.getSimpleName();

    // Used to identify when the IntentServices are finished
    protected static final String UPDATE_UI =
            "com.example.newglasses.amclearskies.UPDATE_UI";
    // Used to identify when the IntentServices are finished
    protected static final String SETTINGS_UPDATED =
            "com.example.newglasses.amclearskies.SETTINGS_UPDATED";
    // Used to identify when the IntentServices are finished
    protected static final String NO_INTERNET =
            "com.example.newglasses.amclearskies.NO_INTERNET";
    // Used to identify when the IntentServices are finished
    private static final String NOTHING_TO_DECLARE =
            "com.example.newglasses.amclearskies.NOTHING_TO_DECLARE";
    // Used to identify when the IntentServices are finished
    private static final String AURORA =
            "com.example.newglasses.amclearskies.AURORA";
    // Used to identify when the IntentServices are finished
    private static final String AURORA_ISS =
            "com.example.newglasses.amclearskies.AURORA_ISS";
    // Used to identify when the IntentServices are finished
    private static final String ISS =
            "com.example.newglasses.amclearskies.ISS";
    // Used to identify when the IntentServices are finished
    private static final String OUT_OF_RANGE =
            "com.example.newglasses.amclearskies.OUT_OF_RANGE";
    // Used to identify when the IntentServices are finished
    private static final String MAKE_NOTIFICATION =
            "com.example.newglasses.amclearskies.MAKE_NOTIFICATION";

    // DATA OUTCOMES DETERMINE UPDATE OF THE UI
    private static boolean auroraSuccess, issSuccess, weatherSuccess, outOfRange;
    protected static boolean noInternet;
    private static int weatherTier;

    // When accessing via PreferenceManager, DefaultPrefs you get access to the one global set
    private static SharedPreferences sharedPrefs;
    // SharedPref variables
    private static String locationPref, alarmPref;
    private static boolean issPref, auroraPref;
    private static int key = 0;

    @Override
    protected void onHandleIntent(Intent intent) {

        /*
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        auroraPref = sharedPrefs.getBoolean("aurora", true);
        issPref = sharedPrefs.getBoolean("iss", true);
        locationPref = sharedPrefs.getString("location", "pref_gps_list_titles");
        alarmPref = sharedPrefs.getString("timePicker", "summary");

        //ApplicationController.getInstance().getDataToDisplay().clear();
        //startGPSService(this);
        */

        // TRYING OUT
        Log.i(LOG_TAG, "onHandleIntent() started");

        //BEFORE STARTING, CLEAR EVERYTHING IN THE DATATODISPLAY ARRAYLIST
        ApplicationController.getInstance().getDataToDisplay().clear();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // No default pref:
        // String locationPref = sharedPrefs.getString("location", "pref_gps_list_titles");

        // Default preference is roaming
        String locationPref = sharedPrefs.getString("location", "1");
        Log.e(LOG_TAG, locationPref);

        // Check the device coordinates are in the UK
        boolean insideUK = sharedPrefs.getBoolean("withinBounds", false);
        Log.e(LOG_TAG, "withinBounds:" + insideUK);

        // Check event preferences
        auroraPref = sharedPrefs.getBoolean("aurora", true);
        issPref = sharedPrefs.getBoolean("iss", true);


        if (locationPref.equals("1") || locationPref.equals("-1")) {
            Log.e(LOG_TAG, "locationPref = Roaming");
            startGPSService(this);

        } else if (locationPref.equals("0")) {
            Log.e(LOG_TAG, "locationPref = Fixed");


            // CHECK IF THE SAVED COORDS ARE UK?? OR IS THAT DONE ALREADY?? FLAGGED ALREADY??

            if (!insideUK) {
                Log.e(LOG_TAG, "Out of Bounds - Device currently not in the UK");
                // startGPSService(this);
                Intent i = new Intent(OUT_OF_RANGE);
                sendBroadcast(i);
            } else {
                if (insideUK) {
                    startWeatherService(this);
                }
            }
        }
    }

    /*
    ****************************************************************************
    HERE ARE ALL THE BROADCAST RECEIVERS THAT DEAL WITH FINISHED INTENT SERVICES
    ****************************************************************************
    */

    public static class AlarmReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.AlarmReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(LOG_TAG, "My alarm receiver received a broadcast");

            /*
            //BEFORE STARTING, CLEAR EVERYTHING IN THE DATATODISPLAY ARRAYLIST
            ApplicationController.getInstance().getDataToDisplay().clear();

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            // No default pref:
            // String locationPref = sharedPrefs.getString("location", "pref_gps_list_titles");

            // Default preference is roaming
            String locationPref = sharedPrefs.getString("location", "1");
            Log.e(LOG_TAG, locationPref);

            // Check the device coordinates are in the UK
            boolean insideUK = sharedPrefs.getBoolean("withinBounds", false);
            Log.e(LOG_TAG, "withinBounds:" + insideUK);

            // Check event preferences
            auroraPref = sharedPrefs.getBoolean("aurora", true);
            issPref = sharedPrefs.getBoolean("iss", true);


            if (locationPref.equals("1") || locationPref.equals("-1")) {
                Log.e(LOG_TAG, "locationPref = Roaming");
                startGPSService(context);

            } else if (locationPref.equals("0")) {
                Log.e(LOG_TAG, "locationPref = Fixed");
                if (!insideUK) {
                    Log.e(LOG_TAG, "insideUK = false");
                    startGPSService(context);
                } else {
                    if (insideUK) {
                        if (auroraPref){
                            startAuroraWatchService(context);

                        } else if (issPref) {
                            startOpenNotifyService(context);
                        }

                    } else {
                        // HERE I NEED TO UPDATE THE UI ACCORDINGLY
                        Log.e(LOG_TAG, "Out of Bounds - Device currently not in the UK");
                        Intent i  = new Intent(UPDATE_UI);
                        context.sendBroadcast(i);
                    }
                }
                */
                /*

            } else if (locationPref.equals("-1")) {
                Log.e(LOG_TAG, "locationPref = Default");
                startAuroraWatchService(context);
            } else if (auroraPref){
                Log.e(LOG_TAG, "auroraPref = true");
                startAuroraWatchService(context);

            } else if ((!auroraPref) && (issPref)) {
                Log.e(LOG_TAG, "auroraPref = false, issPref = true");
                startOpenNotifyService(context);
            }



            }
            // consider cancelling the alarm here if there are no prefs set up - and restarting it
            // whenever prefs are updated?

            */
        }

        public AlarmReceiver () {
            super();
        }

    }

    public static class GPSRetrievedReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.GPSRetrievedReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(LOG_TAG, "My GPS receiver received a broadcast");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            /*
            // dummy data
            boolean auroraPref = true;
            boolean issPref = true;
           */

            // Check the device coordinates are in the UK
            boolean insideUK = sharedPrefs.getBoolean("withinBounds", false);
            Log.e(LOG_TAG, "withinBounds:" + insideUK);

            auroraPref = sharedPrefs.getBoolean("aurora", true);
            issPref = sharedPrefs.getBoolean("iss", true);

            // Dummy variable for testing: insideUK = false;

            if (insideUK) {
                startWeatherService(context);
            } else {
                // HERE I NEED TO UPDATE THE UI ACCORDINGLY
                Log.e(LOG_TAG, "Out of Bounds - Device currently not in the UK");
                Intent i  = new Intent(OUT_OF_RANGE);
                context.sendBroadcast(i);
            }
        }
        public GPSRetrievedReceiver () {
            super();
        }
    }

    public static class WeatherDownloadReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.WeatherDownloadReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            // Dummy data
            //weatherSuccess = false;

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            auroraPref = sharedPrefs.getBoolean("aurora", true);
            issPref = sharedPrefs.getBoolean("iss", true);

            Log.e(LOG_TAG, "My weather receiver received a broadcast");

            try {
                String weatherData = parseJSON(context, "Forecast_IO_File");
                weatherSuccess = parseWeather(weatherData, context);
                Log.e(LOG_TAG, "Weather is returned as a success: " + weatherSuccess);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (weatherSuccess) {
                if (auroraPref){
                    Log.e(LOG_TAG, "Aurora pref is: " + auroraPref);
                    startAuroraWatchService(context);
                } else if (issPref) {
                    Log.e(LOG_TAG, "ISS pref is: " + issPref);
                    startOpenNotifyService(context);
                }

            } else {
                Log.e(LOG_TAG, "No Clear Skies");
                // HERE I NEED TO UPDATE THE UI ACCORDINGLY
                // AlertFlag already set to false for weather
                Intent i = new Intent(NOTHING_TO_DECLARE);
                context.sendBroadcast(i);
            }

            /*                              ** USE FOR TESTING **
            if (weatherSuccess) {

                Intent iN = new Intent(MAKE_NOTIFICATION);
                context.sendBroadcast(iN);
            }
            */
        }
        public WeatherDownloadReceiver () {
            super();
        }
    }

    public static class AuroraDownloadReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.AuroraDownloadReceiver.class.getSimpleName();

        /*
        // dummy data
        boolean issPref = true;
        */

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(LOG_TAG, "My Aurora receiver received a broadcast");

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            issPref = sharedPrefs.getBoolean("iss", true);

            auroraSuccess = parseAurora(context);
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
                    // startWeatherService(context);
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

    public static class OpenNotifyDownloadReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.OpenNotifyDownloadReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            // Dummy data for testing: boolean openNotifySuccess = true;

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

                if (auroraSuccess && issSuccess) {
                    Intent i = new Intent(AURORA_ISS);
                    context.sendBroadcast(i);
                    Intent iN = new Intent(MAKE_NOTIFICATION);
                    context.sendBroadcast(iN);
                } else if (issSuccess) {
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
                // create a broadcast to advise time to update the UI
                // do not need to send broadcast to create notification
                Intent iA = new Intent(NOTHING_TO_DECLARE);
                context.sendBroadcast(iA);
            }
        }
        public OpenNotifyDownloadReceiver () {
            super();
        }
    }

    public static class UpdateUIReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.UpdateUIReceiver.class.getSimpleName();

        private static int key = 0;

        @Override
        public void onReceive(Context context, Intent intent) {



            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            weatherSuccess = sharedPrefs.getBoolean("weatherSuccess", false);
            auroraSuccess = sharedPrefs.getBoolean("auroraSuccess", false);
            issSuccess = sharedPrefs.getBoolean("issSuccess", false);
            outOfRange = sharedPrefs.getBoolean("outOfRange", false);
            // noInternet = sharedPrefs.getBoolean("noInternet", false);

            alarmPref = sharedPrefs.getString("timePicker", "20:00");

            Log.e(LOG_TAG, "weatherSuccess: " + weatherSuccess);
            Log.e(LOG_TAG, "auroraSuccess: " + auroraSuccess);
            Log.e(LOG_TAG, "issSuccess: " + issSuccess);
            Log.e(LOG_TAG, "outOfRange: " + outOfRange);
            Log.e(LOG_TAG, "noInternet: " + noInternet);

            // Dummy data for testing
            // weatherSuccess = false; - set up with default false unless goes through the weather & is found to be cloudy
            // auroraSuccess = false; - set up with default false unless goes through aurora & result found
            // issSuccess = false; - set up with default false unless goes through ISS & result found
            // outOfRange = false; - set up with default false unless goes through the GPS & is found to be out of bounds
            // noInternet = false; - set up with default false unless goes through the DailyListener & is found to be no connection

            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
                //MainActivity.mClearSkiesAdapter.clear();
            }
            for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    //MainActivity.mClearSkiesAdapter.add(s);
                    Log.e(LOG_TAG, " What's is now in the dataToDisplay arraylist: " + s);
                }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            if (weatherSuccess && auroraSuccess && issSuccess) {



            } else if (weatherSuccess && auroraSuccess) {

                ApplicationController.getInstance().getTextFirstArray().add("Date");
                ApplicationController.getInstance().getTextFirstArray().add("Visibility");
                ApplicationController.getInstance().getTextFirstArray().add("Next Update");

                ApplicationController.getInstance().getTextSecondArray().add("Aurora");
                ApplicationController.getInstance().getTextSecondArray().add("SS");
                ApplicationController.getInstance().getTextSecondArray().add("Tomorrow");

                ApplicationController.getInstance().getTextThirdArray().add("Minor Activity");
                ApplicationController.getInstance().getTextThirdArray().add("SR");
                ApplicationController.getInstance().getTextThirdArray().add("AlarmT");

                ApplicationController.getInstance().getStyleArray().add("0");
                ApplicationController.getInstance().getStyleArray().add("1");
                ApplicationController.getInstance().getStyleArray().add("1");

                MainActivity.testBaseCustomAdapter.notifyDataSetChanged();

            } else if (!weatherSuccess && !auroraSuccess && !issSuccess && !outOfRange && !noInternet){

                ApplicationController.getInstance().getTextFirstArray().add("ALL FALSE");

                ApplicationController.getInstance().getTextSecondArray().add("Network Access");

                ApplicationController.getInstance().getTextThirdArray().add("Required");

                ApplicationController.getInstance().getStyleArray().add("0");
                // ApplicationController.getInstance().getStyleArray().add("1");

                MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
            }
        }

        public UpdateUIReceiver () {
            super();
        }

    }

    public static class SettingsUpdatedReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.SettingsUpdatedReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextFirstArray().add("NEXT UPDATE");

            ApplicationController.getInstance().getTextSecondArray().add(locationPref);
            ApplicationController.getInstance().getTextSecondArray().add("Today");

            ApplicationController.getInstance().getTextThirdArray().add("No Events");
            ApplicationController.getInstance().getTextThirdArray().add(alarmPref);

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public SettingsUpdatedReceiver () {
            super();
        }

    }

    public static class NothingToDeclareReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.NothingToDeclareReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextFirstArray().add("Next Update");

            ApplicationController.getInstance().getTextSecondArray().add("No Events");
            ApplicationController.getInstance().getTextSecondArray().add("Tomorrow");

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

    public static class NoInternetReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.NoInternetReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");

            ApplicationController.getInstance().getTextSecondArray().add("Network Access");

            ApplicationController.getInstance().getTextThirdArray().add("Required");

            ApplicationController.getInstance().getStyleArray().add("0");
            // ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public NoInternetReceiver () {
            super();
        }

    }

    public static class OutOfRangeReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.OutOfRangeReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextFirstArray().add("Next Update");

            ApplicationController.getInstance().getTextSecondArray().add("Currently");
            ApplicationController.getInstance().getTextSecondArray().add("Tomorrow");

            ApplicationController.getInstance().getTextThirdArray().add("Out of Range");
            ApplicationController.getInstance().getTextThirdArray().add("AlarmT");

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public OutOfRangeReceiver () {
            super();
        }

    }

    public static class AuroraReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.AuroraReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextFirstArray().add("Visibility");
            ApplicationController.getInstance().getTextFirstArray().add("Next Update");

            ApplicationController.getInstance().getTextSecondArray().add("Aurora");
            ApplicationController.getInstance().getTextSecondArray().add("SS");
            ApplicationController.getInstance().getTextSecondArray().add("Tomorrow");

            ApplicationController.getInstance().getTextThirdArray().add("Minor Activity");
            ApplicationController.getInstance().getTextThirdArray().add("SR");
            ApplicationController.getInstance().getTextThirdArray().add("AlarmT");

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public AuroraReceiver () {
            super();
        }

    }



    public static class IssReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.IssReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextFirstArray().add("Visibility");
            ApplicationController.getInstance().getTextFirstArray().add("Next Update");

            ApplicationController.getInstance().getTextSecondArray().add("ISS");
            ApplicationController.getInstance().getTextSecondArray().add("SS");
            ApplicationController.getInstance().getTextSecondArray().add("Tomorrow");

            ApplicationController.getInstance().getTextThirdArray().add("Time");
            ApplicationController.getInstance().getTextThirdArray().add("SR");
            ApplicationController.getInstance().getTextThirdArray().add("AlarmT");

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public IssReceiver () {
            super();
        }

    }

    public static class AuroraIssReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.AuroraIssReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            locationPref = sharedPrefs.getString("locationPref", "Roaming");
            alarmPref = sharedPrefs.getString("timePicker", "20:00");


            // LOOKING AT THE GATHERED DATA FOR LOGGING
            if (ApplicationController.getInstance().getDataToDisplay() != null) {
                for (String s : ApplicationController.getInstance().getDataToDisplay()) {
                    Log.e(LOG_TAG, " What's in the dataToDisplay arraylist: " + s);
                }
            }

            Utility.clearArrayLists();

            // REPOPULATE THE ARRAYLISTS DEPENDING ON RESULTS

            ApplicationController.getInstance().getTextFirstArray().add("Date");
            ApplicationController.getInstance().getTextFirstArray().add("ISS");
            ApplicationController.getInstance().getTextFirstArray().add("Visibility");
            ApplicationController.getInstance().getTextFirstArray().add("Next Update");

            ApplicationController.getInstance().getTextSecondArray().add("Aurora");
            ApplicationController.getInstance().getTextSecondArray().add("Time");
            ApplicationController.getInstance().getTextSecondArray().add("SS");
            ApplicationController.getInstance().getTextSecondArray().add("Tomorrow");

            ApplicationController.getInstance().getTextThirdArray().add("Minor Activity");
            ApplicationController.getInstance().getTextThirdArray().add("Duration");
            ApplicationController.getInstance().getTextThirdArray().add("SR");
            ApplicationController.getInstance().getTextThirdArray().add("AlarmT");

            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("0");
            ApplicationController.getInstance().getStyleArray().add("1");
            ApplicationController.getInstance().getStyleArray().add("1");

            MainActivity.testBaseCustomAdapter.notifyDataSetChanged();
        }

        public AuroraIssReceiver () {
            super();
        }

    }

    public static class MakeNotificationReceiver extends BroadcastReceiver {

        // For logging
        private static final String LOG_TAG = ClearSkiesService.MakeNotificationReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            // String msg = ApplicationController.getInstance().getDataToDisplay().get(0);
            // String msgText = ApplicationController.getInstance().getDataToDisplay().get(1);
            // String msgText2 = ApplicationController.getInstance().getDataToDisplay().get(2);
            // String msgText3 = ApplicationController.getInstance().getDataToDisplay().get(2);
            // String msgAlert = ApplicationController.getInstance().getDataToDisplay().get(1);

            // Create notification using dummy data
            ClearSkiesService.createNotification(context, "MSG" , "MSG Text", "MSG Alert");
            // ClearSkiesService.createNotification(context, msg , msgText, msgAlert);

        }
        public MakeNotificationReceiver () {
            super();
        }
    }

    /***********************************************************************************************
    ************************************************************************************************
                                    PARSING THE DOWNLOADED DATA
    ************************************************************************************************
    ***********************************************************************************************/

    public static boolean parseAurora(Context context) {

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
        long sunset = sharedPrefs.getLong("sunsetTime", 00000L);

        Log.e(LOG_TAG, "Accessed sunsetTime from sharedPrefs: " + String.valueOf(sunset));

        Date passDate = new Date(sunset);
        Date todayDate = new Date(System.currentTimeMillis());
        DateFormat df = new SimpleDateFormat("d MMM");
        String passDf = df.format(passDate);
        String todayDf = df.format(todayDate);

        // It is possible for the ISS to passover more than once in the same night
        for (int i = 0; i < passes.size(); i++) {
            long risetime = passes.get(i).getLong("risetime");
            Date risetimeDate = new Date(risetime * 1000L);
            if ((passes.get(i).getLong("risetime") > sunset) && passDf.equals(todayDf)) {
                Log.e(LOG_TAG, "pass df: " + passDf + " today df: " + todayDf);
                Log.e(LOG_TAG, "sunset: " + sunset + " risetime: " + risetime);
                passRiseTime.add(passes.get(i).getLong("risetime"));
                // Flag that there is a result
                issSuccess = true;
                // Update the sharedPrefs that will populate the UI
                sharedPrefs.edit().putString("onForecast", passRiseTime.get(0).toString());
            } else {
                issSuccess = false;
            }
        }

        // for logging: check what has been downloaded & parsed
        for (int i = 0; i < passes.size(); i++) {
            passList.append(passes.get(i).toString() + " * ");
        }
        // for logging: check the successful data
        ApplicationController.getInstance().getDataToDisplay().add(passList.toString());
        Log.e(LOG_TAG, passList.toString());

        // duration is in seconds
        // risetime is in UTC which is same as GMT except daylight savings have not been taken into consideration

        /* USED PREVIOUSLY : WAS WORKING
        JSONObject pass = passes.get(1);
        String duration = pass.getString("duration");
        int durationInt = Integer.parseInt(pass.getString("duration"));
        // String risetime = pass.getString("risetime");
        int risetimeInt = Integer.parseInt(pass.getString("risetime"));

        // unix timestamp * 1000L to "upcast" to Long and multiply by 1000 since since Java is expecting millisecs
        Date time = new Date (risetimeInt*1000L);
        String timeFinal = time.toString();

        //String address = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");
       */

        return issSuccess;
    }

    public static boolean parseWeather(String fileData, Context context) throws JSONException, IOException {

        /*
        JSONObject object = new JSONObject(fileData);
        JSONObject currently = object.getJSONObject("currently");
        String summary = currently.getString("summary");
        String visibility = currently.getString("visibility");
        String weatherCombined = summary + " " + visibility;

        */
        // ApplicationController.getInstance().getMatrixCursor().addRow(new Object[]{6, 1, "Weather", "White", "Out"});

        // JSONObject satEntry = satResponse.getJSONArray("response").getJSONObject(i);

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

        } else if (cloudCoverPercent <= 0.5 && precipProb <= 0.5
                && humidityProb <= 90 && visibilityMiles >= 6) {
            weatherSuccess = true;
            weatherTier = 1;

        } else {
            // JUST FOR NOW FOR TESTING KEEP WEATHERSUCCESS AS TRUE
            weatherSuccess = false;
            weatherTier = 2;
        }

        // SAVE THE INFO NEEDED IN OTHER PARTS OF THE APP IN THE SHARED PREFS
        // SAVE SHARED PREFS BASED ON WHETHER OR NOT WEATHER IS A SUCCESS
        // IF NOT SUCCESSFUL, ONLY REQUIRE SUNSET & SUNRISE TIMES
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPrefs.edit().putLong("sunsetTonight", sunsetTonight).apply();
        sharedPrefs.edit().putLong("sunriseTom", sunriseTom).apply();
        sharedPrefs.edit().putString("cloudCover", String.valueOf(cloudCoverPercent)).apply();
        sharedPrefs.edit().putString("visibility", String.valueOf(visibilityMiles)).apply();
        sharedPrefs.edit().putInt("weatherTier", weatherTier);

        ApplicationController.getInstance().getDataToDisplay().add(String.valueOf(time));
        return weatherSuccess;
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
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
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

    public static void createNotification(Context context, String msg, String msgText, String msgAlert){

        // Define an Intent and an action to perform with it by another application
        PendingIntent notificIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        // Builds a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msg)
                        .setTicker(msgAlert)
                        .setContentText(msgText);

        // Defines the Intent to fire when the notification is clicked
        mBuilder.setContentIntent(notificIntent);

        // Set the default notification option
        // DEFAULT_SOUND : Make sound
        // DEFAULT_VIBRATE : Vibrate
        // DEFAULT_LIGHTS : Use the default light notification
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        // Auto cancels the notification when clicked on in the task bar
        mBuilder.setAutoCancel(true);

        // Gets a NotificationManager which is used to notify the user of the background event
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        mNotificationManager.notify(1, mBuilder.build());

    }
    // See more at: http://www.newthinktank.com/2014/12/make-android-apps-19/#sthash.qQhbHKzz.dpuf

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
        // add two hours in millisecs = 7,200,000
        //long unixTime = (System.currentTimeMillis() + 7200000) / 1000L;
    }

    public ClearSkiesService(String name) {
        super(name);
    }

    // Validates resource references inside Android XML files
    public ClearSkiesService() {
        super(ClearSkiesService.class.getName());
    }
}
