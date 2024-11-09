package com.example.whatdoisee;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TargetFinder implements Runnable {
    private final LatLng myLocation;
    private final LatLng myLocationRadians;
    private final double direction;
    private static final double EARTH_RADIUS = 6371000;
    private static int MINIMAL_LENGTH_TO_TARGET; // = 100;
    private static int MAXIMAL_LENGTH_TO_TARGET; // = 40000;
    private static int DISTANCE_RESOLUTION; // = 100;
    private final int BATCH_SIZE;
    private final double cameraAngleFromTheGround;
    private boolean searchFinished = false;
    private final TargetFinderCallback callback;

    public TargetFinder(LatLng location, double direction, double cameraAngleFromTheGround, int maximum, int minimum, int resolution, TargetFinderCallback callback, int batchSize){
        this.myLocation = location;
        this.direction = direction;
        this.callback = callback;
        this.cameraAngleFromTheGround = cameraAngleFromTheGround;
        this.BATCH_SIZE = batchSize;
        MINIMAL_LENGTH_TO_TARGET = minimum;
        MAXIMAL_LENGTH_TO_TARGET = maximum;
        DISTANCE_RESOLUTION = resolution;
        myLocationRadians = new LatLng(Math.toRadians(location.latitude), Math.toRadians(location.longitude));
    }

    private Target[] currentCheckMaker(int firstDistance) {
        Target[] ans = new Target[BATCH_SIZE];
        for (int i = 0; i < BATCH_SIZE ; i++) {
            int distance = firstDistance + (i * DISTANCE_RESOLUTION);
            ans[i] = new Target(addDistance(distance), distance);
        }
        return ans;
    }

    @Override
    public void run() {
        double myHeight = getHeights(new Target[]{new Target(myLocation, 0)})[0];
        int distance = MINIMAL_LENGTH_TO_TARGET;
        while (!searchFinished) {
            Target[] currentCheck = currentCheckMaker(distance);
            double[] heights = getHeights(currentCheck);
            for (int i = 0; i < heights.length & !searchFinished; i++) {
                double inFront = heights[i] - myHeight;
                double tan = Math.tan(cameraAngleFromTheGround - (Math.PI / 2));
                double near = currentCheck[i].distance;
                double error = inFront - (tan * near);
                if (error > 0) {
                    searchFinished = true;
                    callback.TargetFoundCallback(currentCheck[i], heights[i]);
                } else if (currentCheck[i].distance < MAXIMAL_LENGTH_TO_TARGET) {
                    callback.NotHereCallback(currentCheck[i]);
                } else {
//                } else if (currentCheck[i].distance > MAXIMAL_LENGTH_TO_TARGET) {
                    searchFinished = true;
                    callback.failureCallback();
                }
            }
            distance = currentCheck[currentCheck.length-1].distance + DISTANCE_RESOLUTION;
        }
    }

    private String targetsToLocationsString(Target[] targets) {
        StringBuilder ans = new StringBuilder();
        for (Target target : targets) {
            ans.append(target.location.latitude).append(",").append(target.location.longitude).append("|");
        }
        ans = new StringBuilder(ans.substring(0, ans.length() - 1));
        return ans.toString();
    }

    private double[] getHeights(Target[] targets) {
        double[] heights = new double[targets.length];
        try {
            String baseUrl = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
            String beforeKey = "&key=";
            String API_KEY = "API_KEY"; //TODO enter API_KEY
            URL url = new URL(baseUrl + targetsToLocationsString(targets) + beforeKey + API_KEY);
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
        return heights;
    }

    private LatLng addDistance(double distance) {
        double angularDistance = distance / EARTH_RADIUS;  // Convert distance to angular distance
        double newLatitude = Math.asin(Math.sin(myLocationRadians.latitude) * Math.cos(angularDistance) +
                Math.cos(myLocationRadians.latitude) * Math.sin(angularDistance) * Math.cos(direction));
        double newLongitude = myLocationRadians.longitude + Math.atan2(Math.sin(direction) * Math.sin(angularDistance) * Math.cos(myLocationRadians.latitude),
                Math.cos(angularDistance) - Math.sin(myLocationRadians.latitude) * Math.sin(newLatitude));
        newLatitude = Math.toDegrees(newLatitude);
        newLongitude = Math.toDegrees(newLongitude);
        newLongitude = ((newLongitude + 540) % 360) - 180;
        return new LatLng(newLatitude, newLongitude);
    }
}
