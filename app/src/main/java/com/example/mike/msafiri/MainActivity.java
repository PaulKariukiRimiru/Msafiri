package com.example.mike.msafiri;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.mike.msafiri.Custom.GeofenceClass;
import com.example.mike.msafiri.Custom.NewTracker;
import com.example.mike.msafiri.Custom.PermissionsRequest;
import com.example.mike.msafiri.Custom.TrackGPS;
import com.example.mike.msafiri.Interfaces.IInterfaceUpdate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IInterfaceUpdate,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 50.0f; // in meters
    private final int REQ_PERMISSION = 999;
    private Boolean test;
    private Pubnub pubnub;
    private TrackGPS gps;
    private PermissionsRequest askPermission;
    private NewTracker newTracker;
    private GeofenceClass geofenceClass;
    private GoogleMap map;
    private TextView tvLocation;
    private TextView textLat, textLong;
    private LocationRequest locationRequest;

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initializing Permission Class
        askPermission = new PermissionsRequest(this, MainActivity.this, TAG);

        //initialize pubnub
        pubnub = new Pubnub(getString(R.string.com_pubnub_publishKey), getString(R.string.com_pubnub_subscribeKey));

        // initialize GoogleMaps
        initGMaps();

        newTracker = new NewTracker(this, TAG, this);
        newTracker.createGoogleApi(this, this, this);

        test = false;
        tvLocation = (TextView) findViewById(R.id.tvlocation);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTracking();
                createGeoFence();
                /*if(gps.canGetLocation()){
                    tvLocation.setText("Latitude: "+gps.getLatitude()+"\n"+"Longitude: "+gps.getLongitude());
                    transferData(gps.getLatitude(),gps.getLongitude());
                    Snackbar.make(view, "Longitude:"+Double.toString(gps.getLongitude())+"\nLatitude:"+Double.toString(gps.getLatitude()), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else
                {
                    gps.showSettingsAlert();
                }*/
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void startTracking() {
        //gps = new TrackGPS(MainActivity.this,this);
        newTracker.startLocationUpdates(askPermission);
    }

    public void createGeoFence() {
        geofenceClass = new GeofenceClass(this, newTracker.getClient(), TAG, askPermission, this, newTracker.getLastKnownLocation(askPermission), map);
        geofenceClass.startGeofence(GEOFENCE_RADIUS, "Msafiri Goefence", GEO_DURATION);
    }

    private void transferData(double lat, double lon) {
        pubnub.publish("KAA ABC1", String.valueOf(lat) + " " + String.valueOf(lon), new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println(message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                System.out.println(error);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.stopUsingGPS();
    }

    @Override
    public void updateViews(Location location) {
        transferData(location.getLatitude(), location.getLongitude());
        tvLocation.setText("Latitude: " + location.getLatitude() + "\n" + "Longitude: " + location.getLongitude());
    }

    @Override
    public void updateViewsLatlng(LatLng latLng) {
        transferData(latLng.latitude, latLng.longitude);
        tvLocation.setText("Latitude: " + latLng.latitude + "\n" + "Longitude: " + latLng.longitude);
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    newTracker.getLastKnownLocation(askPermission);
                } else {
                    // Permission denied
                    askPermission.permissionsDenied();
                }
                break;
            }
        }
    }


    // Initialize GoogleMaps
    private void initGMaps() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            geofenceClass.startGeofence(GEOFENCE_RADIUS, "Msafiri Goefence", GEO_DURATION);
        } else {
            // inform about fail
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        newTracker.startLocationUpdates(askPermission);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        tvLocation.setText("Latitude: "+location.getLatitude()+"\n"+"Longitude: "+location.getLongitude());
    }

    @Override
    protected void onStart() {
        super.onStart();
        newTracker.connectClient();
        geofenceClass.recoverGeofenceMarker("Msafiri Goefence", GEOFENCE_RADIUS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        newTracker.disconnectClient();
    }
}
