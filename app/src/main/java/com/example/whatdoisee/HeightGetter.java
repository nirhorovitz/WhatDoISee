package com.example.whatdoisee;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HeightGetter implements Runnable {
    protected LatLng[] locations;
    protected int[] distances;
    protected OnHeightReady callback;

    public HeightGetter(LatLng[] locations, int[] distances, OnHeightReady callback) {
        this.locations = locations;
        this.distances = distances;
        this.callback = callback;
    }
    public HeightGetter(LatLng[] locations, int[] distances, OnHeightReady callback, int batchSize, int resolution) {
        this.locations = locations;
        this.distances = distances;
        this.callback = callback;
    }

    private String arrayToString() {
        StringBuilder ans = new StringBuilder();
        for (LatLng location : locations) {
            ans.append(location.latitude).append(",").append(location.longitude).append("|");
        }
        ans = new StringBuilder(ans.substring(0, ans.length() - 1));
        return ans.toString();
    }


    @Override
    public void run() {
        double[] heights = new double[distances.length];
        try {
            String baseUrl = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
            String beforeKey = "&key=";
            String API_KEY = "AIzaSyABuolL9hkLJ_uP1rYzxwmElWwi822A-C0";
            URL url = new URL(baseUrl + arrayToString() + beforeKey + API_KEY);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            int counter = 0;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("elevation")) {
                    heights[counter] = Double.parseDouble(inputLine.substring(23, inputLine.length() - 1));
                    counter++;
                }
            }
            in.close();
            con.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        callback.onHeightsReady(heights, distances, locations);
    }
}

//public class HeightGetter implements Runnable {
//    protected LatLng location;
//    protected int distance;
//    protected OnHeightReady callback;
//
//    public HeightGetter(LatLng location, int distance, OnHeightReady callback) {
//        this.location = location;
//        this.distance = distance;
//        this.callback = callback;
//    }
//
//    @Override
//    public void run() {
//        double height = Integer.MIN_VALUE;
//        if (location != null) {
//            try {
//                String baseUrl = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
//                String beforeKey = "&key=";
//                String API_KEY = "AIzaSyABuolL9hkLJ_uP1rYzxwmElWwi822A-C0";
//                URL url = new URL(baseUrl + location.latitude + "," + location.longitude + beforeKey + API_KEY);
//                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                con.setRequestMethod("GET");
//                con.setRequestProperty("Content-Type", "application/json");
//                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                String inputLine;
//                while ((inputLine = in.readLine()) != null & height == Integer.MIN_VALUE) {
//                    if (inputLine.contains("elevation")) {
//                        height = Double.parseDouble(inputLine.substring(23, inputLine.length() - 1));
//                    }
//                }
//                in.close();
//                con.disconnect();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        callback.onHeightReady(height, distance, location);
//    }
//}
//
