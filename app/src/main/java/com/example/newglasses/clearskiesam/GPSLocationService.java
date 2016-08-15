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
 * Created by newglasses on 15/08/2016.
 */
public class GPSLocationService extends IntentService {

    // for logging
    private final String LOG_TAG = GPSLocationService.class.getSimpleName();

    // Used to identify when the IntentService finishes
    public static final String LOCATION_DONE =
            "com.example.newglasses.amclearskies.LOCATION_DONE";

    SharedPreferences sharedPrefs;

    final String GPS_LOCATION_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
    final String GPS_LATLNG_PARAM = "latlng";
    final String GPS_API_KEY = "key";

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e(LOG_TAG, "Service Started");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String lat = sharedPrefs.getString("lat", "lat error");
        String lng = sharedPrefs.getString("lng", "lng error");
        String latlng = lat + "," + lng;
        String key = "&key=AIzaSyDisJSPk9wWnF8CtLO8uBhogvfcwxrKP78";

        try {

            /*
            Uri builtOpenNotifyUri = Uri.parse(GPS_LOCATION_BASE_URL).buildUpon()
                    .appendQueryParameter(GPS_LATLNG_PARAM, lat)
                    .appendQueryParameter(GPS_API_KEY, key)
                    .build();

            URL openNotifyURL = new URL (builtOpenNotifyUri.toString());

            */

            StringBuilder sb = new StringBuilder();
            sb.append(GPS_LOCATION_BASE_URL)
                    .append(latlng)
                    .append(key);


            Log.e(LOG_TAG, "Built GPS Location URI: " + sb.toString());

            downloadFile(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Service Stopped");

        // Broadcast an intent back when file is downloaded
        Intent i = new Intent(LOCATION_DONE);
        this.sendBroadcast(i);

    }



    protected void downloadFile(String theUrl) throws IOException {

        // The name for the file we will save data to
        String fileName = "GPS_Location_File";

        Log.e(LOG_TAG, "Inside downloadFile()");

        try {

            // Create an output stream to write data to a file (private to everyone except our app)
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Get File
            URL fileUrl = new URL(theUrl);

            // Create a connection we can use to read data from a url
            URLConnection urlConnection = fileUrl.openConnection();

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
    } // downloadFile

    // Validates resource references inside Android XML files
    public GPSLocationService() {
        super(GPSService.class.getName());
    }

    public GPSLocationService(String name) {
        super(name);
    }
}
