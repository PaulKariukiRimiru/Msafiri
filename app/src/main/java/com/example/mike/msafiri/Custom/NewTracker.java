package com.example.mike.msafiri.Custom;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.mike.msafiri.Interfaces.IInterfaceUpdate;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import static io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider.REQUEST_CHECK_SETTINGS;

/**
 * Created by Mike on 3/4/2017.
 */

public class NewTracker implements LocationListener {

    private final Context context;
    private final IInterfaceUpdate interfaceUpdate;
    GoogleApiClient client;
    private final String TAG;
    // Defined in mili seconds.
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;
    LocationRequest locationRequest;


    public NewTracker(Context context, String TAG, IInterfaceUpdate interfaceUpdate){
        this.context = context;
        this.TAG = TAG;
        this.interfaceUpdate = interfaceUpdate;
    }

    // Create GoogleApiClient instance
    public void createGoogleApi(Context context, GoogleApiClient.ConnectionCallbacks callbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        Log.d(TAG, "createGoogleApi()");
        if ( client == null ) {
            client = new GoogleApiClient.Builder( context )
                    .addConnectionCallbacks( callbacks )
                    .addOnConnectionFailedListener( connectionFailedListener )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    public void connectClient(){
        client.connect();
    }

    public void disconnectClient(){
        client.disconnect();
    }

    // Start location Updates
    public void startLocationUpdates(PermissionsRequest askPermision){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if ( askPermision.checkPermission() ){
            checkLocationSettings();
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, NewTracker.this);
        }
        else {
            askPermision.askPermission();
            if (askPermision.checkPermission()){
                checkLocationSettings();
                LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, NewTracker.this);
            }
            else{
                Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
            }

        }
    }
    //check for location settings
    private void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    // Get last known location
    public LatLng getLastKnownLocation(PermissionsRequest permissionsRequest) {
        Log.d(TAG, "getLastKnownLocation()");
        LatLng latLng = null;
        if ( permissionsRequest.checkPermission() ) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            if ( lastLocation != null ) {
                Log.i(TAG, "LastKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                writeLastLocation(latLng);
                startLocationUpdates(permissionsRequest);
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates(permissionsRequest);
            }
        }
        else permissionsRequest.askPermission();
        return latLng;
    }

    private void writeLastLocation(LatLng location) {
        interfaceUpdate.updateViewsLatlng(location);
    }

    public GoogleApiClient getClient(){
        return client;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Detected");
        Toast.makeText(context,  "Location change Detected from "+location.getProvider(), Toast.LENGTH_SHORT).show();
        LatLng mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        writeLastLocation(mLatLng);
    }

}
