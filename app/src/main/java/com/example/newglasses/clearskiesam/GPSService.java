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


/**
 * Created by newglasses on 02/08/2016.
 * Uses Google API Client and Fused Location Provider to access device coordinates
 * TODO: Implement runtime permissions
 * TODO: Deal with failed connections
 * Code adapted from: https://www.youtube.com/watch?v=QazGb6wJed8
 */

public class GPSService extends IntentService implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // for logging
    private final String LOG_TAG = GPSService.class.getSimpleName();

    // Used to identify when the IntentService finishes
    public static final String GPS_DONE = "com.example.newglasses.amclearskies.GPS_DONE";

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;

    SharedPreferences sharedPrefs;

    GoogleApiClient mGoogleApiClient;

    private String lat, lng;

    LocationRequest locationRequest;

    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int FIVE_MINUTE = 60 * 5 * MILLISECONDS_PER_SECOND;

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e(LOG_TAG, "GPS Service Started");

        if (ApplicationController.mPlayServices != ApplicationController.PlayServices.UNAVAILABLE) {

            buildGoogleApiClient();

            locationRequest = new LocationRequest();

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

        // get the gps coords of the device using the location listener
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);

        // TODO:
        // Location access permission now required at runtime For API 23 +
        // The following two if statements check permissions and device OS
        // Currently it does not deal with getting runtime permissions
        // Need to work out how to request permissions as it is not possible to do so directly from an IntentService
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Access fine location implies coarse too
                // this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                // ...
                // Possible solution: http://stackoverflow.com/questions/30141631/send-location-updates-to-intentservice
            }
            return;
        }
    }

    @Override
    public void onLocationChanged(Location location) {


        lat = String.valueOf(location.getLatitude());

        lng = String.valueOf(location.getLongitude());

        Log.e(LOG_TAG, "Data from GPSService" + lat + " " + lng);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        ApplicationController.getInstance().getDataToDisplay().add(lat + " " + lng);
        sharedPrefs.edit().putString("lat", String.valueOf(location.getLatitude())).apply();
        sharedPrefs.edit().putString("lng", String.valueOf(location.getLongitude())).apply();

        Log.e(LOG_TAG, "Data from GPSService after added to sharedPrefs" + lat + " " + lng);

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

    // TODO: Implement checkLocationSettings() method
    // code taken from here: http://stackoverflow.com/questions/29861580/locationservices-settingsapi-reset-settings-change-unavailable-flag
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


    // Validates resource references inside Android XML files
    public GPSService() {
        super(GPSService.class.getName());
    }

    public GPSService(String name) {
        super(name);
    }

}
