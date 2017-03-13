package com.example.mike.msafiri.Interfaces;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Mike on 2/26/2017.
 */

public interface IInterfaceUpdate {
    void updateViews(Location location);
    void updateViewsLatlng(LatLng latLng);
}
