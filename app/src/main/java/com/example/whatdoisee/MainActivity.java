package com.example.whatdoisee;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] lastAccelerometerReading = new float[3];
    private float[] lastMagnetometerReading = new float[3];
    private float[] orientation = new float[3];
    private TextView azimuthDisplay;
    private final int LOCATION_AND_CAMERA_REQUEST = 0;
    private double cameraAzimuth;
    private double cameraAngleFromTheGround;
    private final double HalfPI = Math.PI/2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        DisplayCameraOnCreate();
        tutorial();
    }

    private void tutorial() {
        String first_time = "first_time";
        boolean firsTime = getPreferences(MODE_PRIVATE).getBoolean(first_time, true);
        if (firsTime) {
            getPreferences(MODE_PRIVATE).edit().putBoolean(first_time, false).apply();
            startActivity(new Intent(MainActivity.this, TutorialActivity.class));
        }
    }

    public void buttonPressed(View v) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra("azimuth", cameraAzimuth);
        intent.putExtra("angle", cameraAngleFromTheGround);
        startActivity(intent);
    }

    public void goToSettings(View v) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometerReading,
                    0, lastAccelerometerReading.length);
            updateOrientationAngles();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometerReading,
                    0, lastMagnetometerReading.length);
            updateOrientationAngles();
        }
    }
    

    private void updateOrientationAngles(){
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R,null, lastAccelerometerReading, lastMagnetometerReading);
        SensorManager.getOrientation(R ,orientation);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            cameraAngleFromTheGround = Math.abs(orientation[2]);
            if (Math.toDegrees(orientation[2]) < 0)
                cameraAzimuth = orientation[0] + HalfPI;
            else
                cameraAzimuth = orientation[0] - HalfPI;
            if (cameraAzimuth < 0)
                cameraAzimuth += 2*Math.PI;
        }
        else {
            cameraAzimuth = 555;
        }
        String text = "azimuth: " + (int) Math.toDegrees(cameraAzimuth) + "     angle: " +  (int) Math.toDegrees(cameraAngleFromTheGround);
        azimuthDisplay.setText(text);
    }


    private void DisplayCameraOnCreate() {
        setContentView(R.layout.camera);

        LifecycleCameraController cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        cameraController.setPinchToZoomEnabled(true);

        PreviewView cameraView = (PreviewView) findViewById(R.id.CameraPreviewView);
        cameraView.setController(cameraController);

        DisplayMetrics DM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(DM);


        View circle = findViewById(R.id.circle_center);
        circle.setLeftTopRightBottom(DM.widthPixels/2 - 50,
                                    DM.heightPixels/2 - 50,
                                    DM.widthPixels/2 + 50,
                                    DM.heightPixels/2 + 50);
        cameraView.getOverlay().add(circle);

        azimuthDisplay = (TextView) findViewById(R.id.azimuth);
        azimuthDisplay.setLeftTopRightBottom(20,
                                            DM.heightPixels*5/6,
                                            DM.widthPixels,
                                            DM.heightPixels);
        cameraView.getOverlay().add(azimuthDisplay);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasCameraPer   = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        boolean hasLocationPer = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasCameraPer | !hasLocationPer) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            if (!hasCameraPer & !hasLocationPer )
                Toast.makeText(this, R.string.allow_both, Toast.LENGTH_LONG).show();
            else if (!hasCameraPer & hasLocationPer)
                Toast.makeText(this, R.string.allow_camera_please, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.allow_location_please, Toast.LENGTH_LONG).show();
        }

        if (requestCode == LOCATION_AND_CAMERA_REQUEST & (grantResults.length < 1 || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
            finish();
        }
        if (requestCode == LOCATION_AND_CAMERA_REQUEST & (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkPermissions() {
        boolean hasCameraPer   = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        boolean hasLocationPer = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasCameraPer | !hasLocationPer)
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA}, LOCATION_AND_CAMERA_REQUEST);
    }

}
