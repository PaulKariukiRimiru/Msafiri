package com.example.mike.msafiri.Custom;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.mike.msafiri.AppSingleton;
import com.example.mike.msafiri.MainActivity;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

/**
 * Created by Mike on 2/21/2017.
 */

public class Locator{
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_ID = "id";
    private final Context context;
    private final String postEndPoint;
    String returnText="nothing yet";
    private LocationGooglePlayServicesProvider provider;

    public Locator(final Context context,String postEndPoint){

        this.context = context;
        this.postEndPoint = postEndPoint;
    }
    public String runCode(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, postEndPoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context,response,Toast.LENGTH_LONG).show();
                        returnText = response;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        returnText = error.toString();
                        Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show();
                    }
                });
        AppSingleton.getInstance(context).addToRequestQueue(stringRequest,"Heart beat");
        Log.d("Heartbeat",returnText);
        return returnText;
    }
    public String postLocation(String postEndPoint,final String latitude, final String longitude, final String id){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, postEndPoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context,response,Toast.LENGTH_LONG).show();
                        returnText = response;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        returnText = error.toString();
                        Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_LONGITUDE,longitude);
                params.put(KEY_LATITUDE,latitude);
                params.put(KEY_ID, id);
                return params;
            }
        };
        AppSingleton.getInstance(context).addToRequestQueue(stringRequest,"Heart beat");
        Log.d("Heartbeat",returnText);
        return returnText;
    }

    public String showLast(String endPoint) {
        String latitude="";
        String longitude="";
        String id = "1";
            latitude = "1111111";
            longitude = "22222222";
            postLocation(endPoint,latitude,longitude,id);
        return latitude+longitude;
    }


}
