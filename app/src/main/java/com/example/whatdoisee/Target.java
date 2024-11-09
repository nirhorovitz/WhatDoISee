package com.example.whatdoisee;

import com.google.android.gms.maps.model.LatLng;

public class Target {
    protected LatLng location;
    protected int distance;
    public Target(LatLng location, int distance) {
        this.location = location;
        this.distance = distance;
    }

}