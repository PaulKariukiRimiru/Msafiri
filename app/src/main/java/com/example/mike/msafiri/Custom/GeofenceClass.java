package com.example.mike.msafiri.Custom;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mike.msafiri.Services.GeofenceTrasitionService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

/**
 * Created by Mike on 3/3/2017.
 */

public class GeofenceClass{
    private final String TAG;
    private final PermissionsRequest permissionsRequest;
    private final Context context;
    private GoogleApiClient googleApiClient;

    private PendingIntent geoFencePendingIntent;
    ResultCallback<Status> callback;
    Marker marker;
    GoogleMap map;

    public GeofenceClass(Context context, GoogleApiClient googleApiClient, String TAG, PermissionsRequest permissionsRequest , ResultCallback<Status> callback,LatLng latLng, GoogleMap map){
        this.googleApiClient = googleApiClient;
        this.TAG = TAG;
        this.permissionsRequest = permissionsRequest;
        this.context = context;
        this.geoFencePendingIntent = createGeofencePendingIntent();
        this.callback = callback;
        this.map = map;

        markerLocation(latLng);
    }
    // Start Geofence creation process
    public void startGeofence(float radius, String geoId, long duration) {
        Log.i(TAG, "startGeofence()");
        if( marker != null ) {
            Geofence geofence = createGeofence( marker.getPosition(), radius, geoId,duration );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
            drawGeofence(radius);
            saveGeofence(geoId);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( map!=null ) {
            if ( marker != null )
                marker.remove();
            marker = map.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    // Create a Geofence
    private Geofence createGeofence( LatLng latLng, float radius, String geoId, long duration ) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(geoId)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( duration )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    //create pending intent
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        final int GEOFENCE_REQ_CODE = 0;
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent(context , GeofenceTrasitionService.class);

        return PendingIntent.getService(context, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    public void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (permissionsRequest.checkPermission())
            LocationServices.GeofencingApi.addGeofences(googleApiClient, request, createGeofencePendingIntent()
            ).setResultCallback(callback);
    }
    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    public void drawGeofence(float radious) {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( marker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( radious );
        geoFenceLimits = map.addCircle( circleOptions );
    }

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
    public void saveGeofence(String name) {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = context.getSharedPreferences(name, Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( marker.getPosition().latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( marker.getPosition().longitude ));
        editor.apply();
    }

    // Recovering last Geofence marker
    public void recoverGeofenceMarker(String name,GoogleMap map, Marker marker, float radious) {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = context.getSharedPreferences(name, Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
            markerLocation(latLng);
            drawGeofence(radious);
        }
    }

    // Clear Geofence
    public void clearGeofence(final Marker marker) {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    removeGeofenceDraw(marker);
                }
            }
        });
    }

    private void removeGeofenceDraw(Marker marker) {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( marker != null)
            marker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }
}
