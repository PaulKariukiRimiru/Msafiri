package com.example.mike.msafiri.Custom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Mike on 3/3/2017.
 */

public class PermissionsRequest{
    private final int REQ_PERMISSION = 999;
    Context context;
    Activity activity;
    static String TAG = "";

    public PermissionsRequest(Context context, Activity activity, String TAG){
        this.context = context;
        this.activity = activity;
        this.TAG = TAG;
    }

    // Check for permission to access Location
    public boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    public void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    // App cannot work without the permissions
    public void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
        Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
        activity.finish();
    }
}
