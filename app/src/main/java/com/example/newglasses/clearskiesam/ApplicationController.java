package com.example.newglasses.clearskiesam;

import android.app.Application;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

/**
 * Created by newglasses on 02/08/2016.
 * ApplicationController is shared by the whole application.
 * Holds ArrayLists of data that are shared by all of the IntentServices
 */

public class ApplicationController extends Application {

    private final String LOG_TAG = ApplicationController.class.getSimpleName();

    // Contains the results of background work so that app logic can be verified
    public static ArrayList<String> dataToDisplay = new ArrayList<>();

    // Each ArrayList contains the required UI data for a TextView
    // The ArrayLists hold data by TextView, not by ListItem
    // Each ListView has 3 TextViews
    // The styleArray defines the ListView layout
    // The ArrayList of data that is shared by all of the IntentServices

    public static ArrayList<String> textFirstArray = new ArrayList<>();
    // The ArrayList of data that is shared by all of the IntentServices
    public static ArrayList<String> textSecondArray = new ArrayList<>();
    // The ArrayList of data that is shared by all of the IntentServices
    public static ArrayList<String> textThirdArray = new ArrayList<>();
    // The ArrayList of data that is shared by all of the IntentServices
    public static ArrayList<String> styleArray = new ArrayList<>();

    public static enum PlayServices {
        NOT_CHECKED, AVAILABLE, UNAVAILABLE
    };
    public static PlayServices mPlayServices = PlayServices.NOT_CHECKED;

   // A singleton instance of the application class for easy access in other places
    private static ApplicationController sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the singleton
        sInstance = this;

        // Connect to Google Play Services
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            ApplicationController.mPlayServices = ApplicationController.PlayServices.AVAILABLE;
        }
    }

    // ApplicationController singleton instance
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    // Accessing ArrayLists that will update the UI
    // Accessed by one thread at a time
    public synchronized ArrayList<String> getTextFirstArray() {
        return textFirstArray;
    }
    public synchronized ArrayList<String> getTextSecondArray() {
        return textSecondArray;
    }
    public synchronized ArrayList<String> getTextThirdArray(){
        return textThirdArray;
    }
    public synchronized ArrayList<String> getStyleArray() {
        return styleArray;
    }

    // Accessing ArrayList that will be used with logs to display results of background work
    // Accessed by one thread at a time
    public synchronized ArrayList<String> getDataToDisplay() {
        return dataToDisplay;
    }
}
