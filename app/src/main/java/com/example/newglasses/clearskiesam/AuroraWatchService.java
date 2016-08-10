package com.example.newglasses.clearskiesam;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by newglasses on 02/08/2016.
 */
public class AuroraWatchService extends IntentService {

    // for logging
    private static final String LOG_TAG = AuroraWatchService.class.getSimpleName();

    // Used to identify when the IntentService finishes
    public final String AURORA_DONE = "com.example.newglasses.amclearskies.AURORA_DONE";


    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e(LOG_TAG, "Service Started");

        // Get the URL for the file to download
        String auroraURL = "http://aurorawatch.lancs.ac.uk/api/0.1/status.xml";

        downloadFile(auroraURL);

        Log.e(LOG_TAG, "Service Stopped");

        // Broadcast an intent back to Main when file is downloaded
        Intent i = new Intent(AURORA_DONE);
        this.sendBroadcast(i);

    }

    protected void downloadFile(String theURL) {

        // The name for the file we will save data to
        String fileName = "Aurora_Watch_File";

        Log.e(LOG_TAG, "Inside downloadFile()");

        try {

            Log.e(LOG_TAG, "url: " + theURL);
            // Create an output stream to write data to a file (private to everyone except our app)
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Get File
            URL fileURL = new URL(theURL);

            // Create a connection we can use to read data from a url
            HttpURLConnection urlConnection = (HttpURLConnection) fileURL.openConnection();

            // We are using the GET method
            urlConnection.setRequestMethod("GET");

            // Set that we want to allow output for this connection
            urlConnection.setDoOutput(true);

            // Connect to the url
            urlConnection.connect();

            // Gets an input stream for reading data
            InputStream inputStream = urlConnection.getInputStream();

            // Define the size of the buffer
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            // read reads a byte of data from the stream until there is nothing more
            while ((bufferLength = inputStream.read(buffer)) > 0) {

                // Write the data received to our file
                outputStream.write(buffer, 0, bufferLength);
            }
            // Close our connection to our file
            outputStream.close();
            // Get File Done
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Validates resource references inside Android XML files
    public AuroraWatchService() {
        super(AuroraWatchService.class.getName());
    }

    public AuroraWatchService(String name) {
        super(name);
    }
}
