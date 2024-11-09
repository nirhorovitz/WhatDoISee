package com.example.whatdoisee;

import com.google.android.gms.maps.model.LatLng;

public interface OnHeightReady {
//    void onHeightReady( double height, int distance, LatLng location);
    void onHeightsReady( double[] heights, int[] distances, LatLng[] locations);
}


