package com.example.newglasses.clearskiesam;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
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


    // Specify the columns we need for the small local database
    private static final String[] LISTVIEW_COLUMNS = {

            BaseColumns._ID,
            "listType",
            "image",
            "textFirst",
            "textSecond",
            "textThird"
    };

    // The ArrayList of data that is shared by all of the IntentServices
    public static MatrixCursor matrixCursor = new MatrixCursor(LISTVIEW_COLUMNS);
    // The ArrayList of data that is shared by all of the IntentServices
    public static MatrixCursor matrixMatrixCursor = new MatrixCursor(LISTVIEW_COLUMNS);

    public static enum PlayServices {
        NOT_CHECKED, AVAILABLE, UNAVAILABLE
    };
    public static PlayServices mPlayServices = PlayServices.NOT_CHECKED;

   // A singleton instance of the application class for easy access in other places
    private static ApplicationController sInstance;

    //Log or request TAG
    public static final String TAG = "VolleyPatterns";

    // Global request queue for Volley - not using anymore
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            ApplicationController.mPlayServices = ApplicationController.PlayServices.AVAILABLE;
        }

        matrixMatrixCursor.addRow(new Object[]{0, 0, R.drawable.img1, 1470833833305L, "30", "40"});
        matrixMatrixCursor.addRow(new Object[]{0, 0, R.drawable.img1, 1470833833305L, "30", "40"});
        matrixMatrixCursor.addRow(new Object[]{1, 1, R.drawable.img1, 1470833833305L, "30", "40"});
        matrixMatrixCursor.addRow(new Object[]{1, 1, R.drawable.img1, 1470833833305L, "30", "40"});

        //TRYING OUT
        textFirstArray.add("One");
        textFirstArray.add("One");
        textFirstArray.add("One");

        textSecondArray.add("two");
        textSecondArray.add("two");
        textSecondArray.add("two");

        textThirdArray.add("three");
        textThirdArray.add("three");
        textThirdArray.add("three");

        styleArray.add("0");
        styleArray.add("0");
        styleArray.add("1");

    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    public synchronized MatrixCursor getMatrixCursor() {

        return matrixCursor;
    }
    public synchronized MatrixCursor getMatrixMatrixCursor() {

        return matrixMatrixCursor;
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

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     * // @param tag
     *
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    // code taken from: http://arnab.ch/blog/2013/08/asynchronous-http-requests-in-android-using-volley/


}
