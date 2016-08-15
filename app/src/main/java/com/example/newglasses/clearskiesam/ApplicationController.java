package com.example.newglasses.clearskiesam;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

/**
 * Created by newglasses on 02/08/2016.
 */
public class ApplicationController extends Application {

    private final String LOG_TAG = ApplicationController.class.getSimpleName();

    // The ArrayList of data that is shared by all of the IntentServices
    public static ArrayList<String> dataToDisplay = new ArrayList<>();

    // USING THE BASE ADAPTER
    // The ArrayList of data that is shared by all of the IntentServices
    public static ArrayList<String> imageArray = new ArrayList<>();
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

        // initialize the singleton
        sInstance = this;

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            ApplicationController.mPlayServices = ApplicationController.PlayServices.AVAILABLE;
        }
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    // ACCESSING EACH ARRAYLIST IN ORDER TO UPDATE
    public synchronized ArrayList<String> getImageArray() {
        return imageArray;
    }
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

    /**
     * @return The dataToDisplay ArrayList, the ArrayList will be created if it is null
     */
    public synchronized ArrayList<String> getDataToDisplay() {
        return dataToDisplay;
    }
}
