package com.example.mike.msafiri;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.mike.msafiri.Custom.Locator;
import com.example.mike.msafiri.Custom.TrackGPS;
import com.example.mike.msafiri.Interfaces.IInterfaceUpdate;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,IInterfaceUpdate {
    Boolean test;
    String endPoint = "http://192.168.88.244:3000/ping";
    private TrackGPS gps;
    TextView tvLocation;
    Pubnub pubnub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        test = false;

        pubnub = new Pubnub(getString(R.string.com_pubnub_publishKey), getString(R.string.com_pubnub_subscribeKey));



        tvLocation = (TextView) findViewById(R.id.tvlocation);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Locator locator = new Locator(MainActivity.this,endPoint);
                //String response = locator.runCode( );
                String response = locator.showLast("http://192.168.88.244:3000/matatu/position");
                Snackbar.make(view, "Response"+response, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                startTracking();
                if(gps.canGetLocation()){
                    tvLocation.setText("Latitude: "+gps.getLatitude()+"\n"+"Longitude: "+gps.getLongitude());
                    transferData(gps.getLatitude(),gps.getLongitude());
                    Snackbar.make(view, "Longitude:"+Double.toString(gps.getLongitude())+"\nLatitude:"+Double.toString(gps.getLatitude()), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else
                {
                    gps.showSettingsAlert();
                }


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
    public void startTracking(){
        gps = new TrackGPS(MainActivity.this,this);
    }
    public void transferData(double lat, double lon){
        Locator locator = new Locator(MainActivity.this,endPoint);
        locator.showLast("http://192.168.88.244:3000/matatu/position");
        pubnub.publish("KAA ABC1", String.valueOf(lat)+" "+String.valueOf(lon), new Callback(){
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
        transferData(location.getLatitude(),location.getLongitude());
        tvLocation.setText("Latitude: "+location.getLatitude()+"\n"+"Longitude: "+location.getLongitude());
    }
}
