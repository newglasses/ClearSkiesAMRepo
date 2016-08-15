package com.example.newglasses.clearskiesam;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by newglasses on 02/08/2016.
 */
public class GPSService extends IntentService implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // for logging
    private final String LOG_TAG = GPSService.class.getSimpleName();

    // Used to identify when the IntentService finishes
    public static final String GPS_DONE = "com.example.newglasses.amclearskies.GPS_DONE";

    SharedPreferences sharedPrefs;

    GoogleApiClient mGoogleApiClient = null;
    private String lat, lng;

    LocationRequest locationRequest;

    private Handler listHandler;

    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int FIVE_MINUTE = 60 * 5 * MILLISECONDS_PER_SECOND;

    @Override
    protected void onHandleIntent(Intent intent) {

        // check that play services are enabled on the device - see Application class also
        // code taken from: http://stackoverflow.com/questions/21022297/fused-location-provider-doesnt-seem-to-use-gps-receiver


        Log.e(LOG_TAG, "GPS Service Started");

        if (ApplicationController.mPlayServices != ApplicationController.PlayServices.UNAVAILABLE) {

            buildGoogleApiClient();

            locationRequest = new LocationRequest();

            // handler is used as a manual workaround as some comments on StackOverflow have indicated
            // locationRequest.setExpirationDuration() & .setExpirationTime()
            // do not work well
            // code amended from:  http://stackoverflow.com/questions/16909171/android-location-request-set-expiration-duration-doesnt-work

            /*
            listHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {

                        //mLocationClient.removeLocationUpdates(MyActivity.this);

                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, GPSService.this);
                        //Location Updates are now removed
                    }
                    super.handleMessage(msg);
                }
            };

            */

            // setting to low power because wifi is required for the app to complete it's work
            // and granular accuracy is not required for it to give valuable results
            // check that android does not kill low priority apps
            // check this map approval thing that is saved in Zotero (Settings.API)
            locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

            // want to keep trying to retrive coords every 5 mins until a result has been returned successfully
            locationRequest.setInterval(FIVE_MINUTE);

            // only need one successful result returned
            locationRequest.setNumUpdates(1);

            // see if any other apps are updating more frequently every 15 seconds
            locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);




        }

        // if the apiClient has been built, then connect it
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else {
            Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnected(Bundle arg0) {

        requestLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void requestLocationUpdates() {

        /*
        IN ORDER TO USE THIS METHOD THE DEVICE MUST BE API 23 - ANYTHING LESS AND THE APP CRASHES
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        // this specific 'if' implementation is taken from: https://www.youtube.com/watch?v=QNb_3QKSmMk
        */

        // get the gps coords of the device using the location listener
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);

        //listHandler.sendEmptyMessageDelayed(0, 10000);

        ///// DON'T WANT LAST LOCATION DATA BECAUSE IT COULD BE STALE AND THEREFORE NOT VERY RELIABLE /////
    }

    // code taken from here: http://stackoverflow.com/questions/29861580/locationservices-settingsapi-reset-settings-change-unavailable-flag
    // Needs fixed and updated to suit requirements of ClearSkies app
    // Right now I don't think it's doing anything
    // Also is this only necessary if you are updating an existing app that might already be on someone's phone?

    private void checkLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();
                Intent resolutionIntent;
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Everything is OK, starting request location updates
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Seems the user need to change setting to enable locations updates,
                        // call startResolutionForResult(Activity, REQUEST_CODE)
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Error, cannot retrieve location updates.
                        break;
                }}});
    }

    // checkLocationSettings() code taken from here:
    // http://stackoverflow.com/questions/29861580/locationservices-settingsapi-reset-settings-change-unavailable-flag
    // Needs fixed and updated to suit requirements of ClearSkies app
    // Right now I don't think it's doing anything
    // Also is this only necessary if you are updating an existing app that might already be on someone's phone?

    @Override
    public void onLocationChanged(Location location) {


        lat = String.valueOf(location.getLatitude());

        lng = String.valueOf(location.getLongitude());

        Log.e(LOG_TAG, "Data from GPSService" + lat + " " + lng);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences.Editor editor = sharedPrefs.edit();

        // ClearSkiesService.dataToDisplay.add(latlng);
        ApplicationController.getInstance().getDataToDisplay().add(lat + " " + lng);
        sharedPrefs.edit().putString("lat", String.valueOf(location.getLatitude())).apply();
        sharedPrefs.edit().putString("lng", String.valueOf(location.getLongitude())).apply();

        Log.e(LOG_TAG, "Data from GPSService after added to sharedPrefs" + lat + " " + lng);

        // Get the coords in text form
        // coordsToText(this, lat, lng);

        if (withinBounds(location.getLatitude(), location.getLongitude())) {
            sharedPrefs.edit().putBoolean("withinBounds", true).apply();
        } else {
            sharedPrefs.edit().putBoolean("withinBounds", false).apply();
        }

        //Broadcast an intent back to the ClearSkiesService when work is complete
        Intent i = new Intent(GPS_DONE);
        sendBroadcast(i);
    }

    // Check that the coordinates are within bounds
    public static boolean withinBounds(double lat, double lng) {

        boolean withinBounds = false;

        if ((lat >= 50.0 && lat <= 60.0) &&
                (lng >= -9.0 && lng <= 2.0)) {
            withinBounds = true;
        }
        return withinBounds;
    }
    // Validates resource references inside Android XML files
    public GPSService() {
        super(GPSService.class.getName());
    }

    public GPSService(String name) {
        super(name);
    }

}
