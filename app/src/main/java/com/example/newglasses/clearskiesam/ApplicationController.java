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

    private static String[] from = {BaseColumns._ID, "icon", "first", "second", "third"};
    private int[] to = {R.id.list_first, R.id.list_second, R.id.list_third};

    //public MatrixCursor cursor = new MatrixCursor(from, 5);


    public static enum PlayServices {
        NOT_CHECKED, AVAILABLE, UNAVAILABLE
    };
    public static PlayServices mPlayServices = PlayServices.NOT_CHECKED;

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static ApplicationController sInstance;


    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            ApplicationController.mPlayServices = ApplicationController.PlayServices.AVAILABLE;
        }

        addData();
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    /**
     * @return The dataToDisplay ArrayList, the ArrayList will be created if it is null
     */
    public synchronized MatrixCursor getMatrixCursor() {

        //return cursor;
        return null;
    }

    public void addData() {

        // cursor.addRow(new Object[]{0, 1, "Extra", "White", "0"});
        // cursor.addRow(new Object[]{1, 1, "Extra", "White", "1"});
        // cursor.addRow(new Object[]{2, 1, "Extra", "White", "2"});
        // cursor.addRow(new Object[]{3, 1, "Extra", "White", "3"});
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
