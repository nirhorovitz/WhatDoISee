package com.example.whatdoisee;

import com.google.android.gms.maps.model.LatLng;

public class Target {
    protected LatLng location;
    protected int distance;
    protected double height;
    public Target() {
        this(null, 0);
    }
    public Target(LatLng location) {
        this(location, 0);
    }
    public Target(int distance) {
        this(null, distance);
    }
    public Target(LatLng location, int distance) {
        this.location = location;
        this.distance = distance;
    }

}