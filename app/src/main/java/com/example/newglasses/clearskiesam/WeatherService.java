package com.example.newglasses.clearskiesam;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by newglasses on 02/08/2016.
 * Accesses the Forecast.io API
 * Downloads data to local file
 * Informs the ClearSkiesService via Broadcast when work is complete
 * Code adapted from: http://www.newthinktank.com/2014/12/make-android-apps-18/
 */
public class WeatherService extends IntentService {

    // for logging
    private final String LOG_TAG = WeatherService.class.getSimpleName();

    // Used to identify when the IntentService finishes
    public static final String WEATHER_DONE = "com.example.newglasses.amclearskies.WEATHER_DONE";

    private String location, alarm;
    private boolean iss, aurora;
    private boolean weatherResult = false, openNotifyResult = false;

    protected static SharedPreferences sharedPrefs;

    private String flatlng = "54.640891,-5.941169100000025"; // split these out

    private String forecastListItem, forecastDeterminants;
    private String timeFinal;
    private String openNotifyListItem;

    // These are the names of the JSON objects that need to be extracted.
    final String FIO_DAILY = "daily";
    final String FIO_CURRENTLY = "currently";

    // These are the names of the JSON arrays that need to be extracted.
    final String FIO_DAILY_DATA = "data";

    // These are the names of the Strings that need to be extracted.

    final String FIO_DAILY_SUNSET = "sunsetTime";
    final String FIO_DAILY_SUNRISE = "sunriseTime";
    final String FIO_CURRENTLY_SUMMARY = "summary";
    final String FIO_CURRENTLY_CLOUDCOVER = "cloudCover";
    final String FIO_CURRENTLY_VISIBILITY = "visibility";
    final String FIO_CURRENTLY_PRECIPPROB = "precipProbability";
    final String FIO_CURRENTLY_HUMIDPROB = "humidity";


    @Override
    protected void onHandleIntent(Intent intent) {


        Log.e(LOG_TAG, "Service Started");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String lat = sharedPrefs.getString("lat", "lat error");
        String lng = sharedPrefs.getString("lng", "lng error");

        final String FORECAST_BASE_URL = "https://api.forecast.io/forecast/";

        // when i get the other work into this project these will be defined from there
        // TODO: Make the API key private
        // TODO: Exclude "params" data blocks from the query that are not required by the app

        String apiKey = "57e606614d55dbee13c97a1736097f91/";
        String separator = ",";
        String params = "?exclude=[currently,minutely,hourly,alerts,flags]?units=[si]&visibility:";


        StringBuilder sbForecastURL = new StringBuilder()
                .append(FORECAST_BASE_URL)
                .append(apiKey)
                .append(lat)
                .append(separator)
                .append(lng);

        String forecastURL = sbForecastURL.toString();

        try {
            downloadFile(forecastURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(LOG_TAG, "Service Stopped");

        // Broadcast an intent back to Main when file is downloaded
        Intent i = new Intent(WEATHER_DONE);
        this.sendBroadcast(i);
    }

    protected void downloadFile(String theURL) throws IOException {

        // The name for the file we will save data to
        String fileName = "Forecast_IO_File";

        Log.e(LOG_TAG, "Inside downloadFile()");

        try {

            Log.e(LOG_TAG, "url: " + theURL);

            // Create an output stream to write data to a file (private to everyone except our app)
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Get File
            URL fileURL = new URL(theURL);

            // Create a connection we can use to read data from a url
            URLConnection urlConnection = fileURL.openConnection();

            if (urlConnection != null)
                urlConnection.setReadTimeout(60 * 1000);
            if (urlConnection != null && urlConnection.getInputStream() != null) {
                InputStream in = urlConnection.getInputStream();

                // Define the size of the buffer
                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                // read reads a byte of data from the stream until there is nothing more
                while ((bufferLength = in.read(buffer)) > 0) {

                    // Write the data received to our file
                    outputStream.write(buffer, 0, bufferLength);
                }

                // Close our connection to our file
                outputStream.close();

                // Get File Done
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Validates resource references inside Android XML files
    public WeatherService() {
        super(WeatherService.class.getName());
    }


    public WeatherService(String name) {
        super(name);
    }
}
