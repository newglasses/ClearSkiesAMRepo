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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by newglasses on 02/08/2016.
 * * Accesses the Open Notify API
 * Downloads data to local file
 * Informs the ClearSkiesService via Broadcast when work is complete
 * Code adapted from: http://www.newthinktank.com/2014/12/make-android-apps-18/
 */
public class OpenNotifyService extends IntentService {

    // for logging
    private final String LOG_TAG = OpenNotifyService.class.getSimpleName();

    // Used to identify when the IntentService finishes
    public static final String OPEN_NOTIFY_DONE = "com.example.newglasses.amclearskies.OPEN_NOTIFY_DONE";

    SharedPreferences sharedPrefs;

    final String OPEN_NOTIFY_BASE_URL = "http://api.open-notify.org/iss-pass.json?";

    final String OPEN_NOTIFY_LAT_PARAM = "lat";
    final String OPEN_NOTIFY_LNG_PARAM = "lon";

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e(LOG_TAG, "Service Started");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String lat = sharedPrefs.getString("lat", "lat error");
        String lng = sharedPrefs.getString("lng", "lng error");

        try {

            Uri builtOpenNotifyUri = Uri.parse(OPEN_NOTIFY_BASE_URL).buildUpon()
                    .appendQueryParameter(OPEN_NOTIFY_LAT_PARAM, lat)
                    .appendQueryParameter(OPEN_NOTIFY_LNG_PARAM, lng)
                    .build();

            URL openNotifyURL = new URL (builtOpenNotifyUri.toString());

            Log.e(LOG_TAG, "Built Open Notify URI: " + openNotifyURL);

            downloadFile(openNotifyURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(LOG_TAG, "Service Stopped");

        // Broadcast an intent back to Main when file is downloaded
        Intent i = new Intent(OPEN_NOTIFY_DONE);
        this.sendBroadcast(i);
    }

    protected void downloadFile(URL theURL) throws IOException {

        // The name for the file we will save data to
        String fileName = "Open_Notify_File";

        Log.e(LOG_TAG, "Inside downloadFile()");

        try {

            Log.e(LOG_TAG, "url: " + theURL.toString());

            // Create an output stream to write data to a file (private to everyone except our app)
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Create a connection we can use to read data from a url
            URLConnection urlConnection = theURL.openConnection();

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
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // downloadFile

    // Validates resource references inside Android XML files
    public OpenNotifyService() {
        super(OpenNotifyService.class.getName());
    }


    public OpenNotifyService(String name) {
        super(name);
    }
}
