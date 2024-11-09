package com.example.whatdoisee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.example.whatdoisee.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, TargetFinderCallback {

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private final LatLng BEER_SHEVA = new LatLng(31.25181, 34.7913);
    private double direction;
    private final float DEFAULT_ZOOM = 13.0F;
    private final float DEFAULT_TILT = 10.0F;
    private static final double EARTH_RADIUS = 6371000;
    private static int MINIMAL_LENGTH_TO_TARGET; // = 100;
    private static int MAXIMAL_LENGTH_TO_TARGET; // = 40000;
    private static int DISTANCE_RESOLUTION; // = 100;
    private double cameraAngleFromTheGround;
    private Toast searchingToast;
    private PolylineOptions polylineOptions;
    private Thread targetFinderThread;
    private TargetFinder targetFinder;
    private static int BATCH_SIZE = SettingsActivity.BATCH_SIZE;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchingToast = Toast.makeText(this, "searching location", Toast.LENGTH_LONG);
        polylineOptions = new PolylineOptions();
        retrieveBundle();
        retrieveSettings();

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, R.string.failure, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void retrieveSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        MAXIMAL_LENGTH_TO_TARGET = sharedPreferences.getInt("maximum", SettingsActivity.MAXIMAL_LENGTH_TO_TARGET) * 1000;
        MINIMAL_LENGTH_TO_TARGET = sharedPreferences.getInt("minimum", SettingsActivity.MINIMAL_LENGTH_TO_TARGET) * 1000;
        DISTANCE_RESOLUTION = sharedPreferences.getInt("resolution", SettingsActivity.DISTANCE_RESOLUTION);
        BATCH_SIZE = sharedPreferences.getInt("batchSize", SettingsActivity.BATCH_SIZE);
        if (MINIMAL_LENGTH_TO_TARGET == 0)
            MINIMAL_LENGTH_TO_TARGET = 100;
    }

    private void retrieveBundle() {
        Bundle bundle = getIntent().getExtras();
        direction = 0;
        cameraAngleFromTheGround = 0;
        if (bundle != null) {
            try {
                direction = bundle.getDouble("azimuth");
            } catch (NullPointerException e) {
                direction = 0;
            }
            try {
                cameraAngleFromTheGround = bundle.getDouble("angle");
            } catch (NullPointerException e) {
                cameraAngleFromTheGround = 0;
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(BEER_SHEVA));
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && // permission
                ((LocationManager) getSystemService(LOCATION_SERVICE)).isLocationEnabled()) { // location is turned on
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition(myLocation, DEFAULT_ZOOM, DEFAULT_TILT, (float) Math.toDegrees(direction));
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                    targetFinder = new TargetFinder(myLocation, direction, cameraAngleFromTheGround, MAXIMAL_LENGTH_TO_TARGET, MINIMAL_LENGTH_TO_TARGET, DISTANCE_RESOLUTION, this);
                    targetFinder = new TargetFinder(myLocation, direction, cameraAngleFromTheGround, MAXIMAL_LENGTH_TO_TARGET, MINIMAL_LENGTH_TO_TARGET, DISTANCE_RESOLUTION, this, BATCH_SIZE);
                    targetFinderThread = new Thread(targetFinder);
                    targetFinderThread.start();
                } else
                    Toast.makeText(this, R.string.failure, Toast.LENGTH_LONG).show();
            });
        } else
            Toast.makeText(this, R.string.no_location, Toast.LENGTH_LONG).show();
    }

    public void buttonPressed(View v) {
        startActivity(new Intent(MapActivity.this, MainActivity.class));
    }

    @Override
    public void TargetFoundCallback(Target target, double height) {
        runOnUiThread(()-> {
            searchingToast.cancel();
            mMap.clear();
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(target.location);
            circleOptions.radius(DISTANCE_RESOLUTION / 2.0);
            mMap.addCircle(circleOptions);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(target.location);
            markerOptions.contentDescription("distance: " + target.distance + "\nheight: " + height);
            mMap.addMarker(markerOptions);
            CameraPosition cameraPosition = new CameraPosition(target.location, DEFAULT_ZOOM, DEFAULT_TILT, (float) Math.toDegrees(direction));
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//            mMap.clear();
//            mMap.addCircle(circleOptions);
//            mMap.addMarker(markerOptions);
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
    }

    @Override
    public void failureCallback() {
        runOnUiThread(()-> {
            searchingToast.cancel();
            Toast.makeText(this, "couldn't find", Toast.LENGTH_LONG).show();
            polylineOptions.color(R.color.darkRed);
            mMap.clear();
            mMap.addPolyline(polylineOptions);
        });
    }

    @Override
    public void NotHereCallback(Target target) {
        runOnUiThread(()-> {
        searchingToast.show();
        polylineOptions.add(target.location);
        mMap.addPolyline(polylineOptions);
        });
    }

}