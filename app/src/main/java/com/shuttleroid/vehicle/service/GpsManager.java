package com.shuttleroid.vehicle.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class GpsManager implements LocationListener {


    // field ------------------------------------------------
    private static GpsManager instance;

    private LocationManager locationManager;
    private Location lastLocation;
    private Location currentLocation;
    private boolean isInitialized = false;
    // ------------------------------------------------------


    // Singleton Method -------------------------------------
    private GpsManager() {
        // private constructor to enforce singleton
    }
    public static synchronized GpsManager getInstance() {
        if (instance == null) {
            instance = new GpsManager();
        }

        return instance;
    }

    // ------------------------------------------------------


    // Permission Request -----------------------------------
    @SuppressLint("MissingPermission")
    public void init(Context context) {
        if (!isInitialized) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                // 요청 간격: 1초(1000ms), 최소 거리 이동: 1미터
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        this
                );
                isInitialized = true;
            }
        }
    }
    // ------------------------------------------------------



    // Logic ------------------------------------------------
    public double getLatitude() {
        return currentLocation != null ? currentLocation.getLatitude() : 0.0;
    }

    public double getLongitude() {
        return currentLocation != null ? currentLocation.getLongitude() : 0.0;
    }

    public float getSpeed() {
        return currentLocation != null ? currentLocation.getSpeed() : 0.0f; // m/s
    }

    public float getDistanceFromLastLocation() {
        if (lastLocation != null && currentLocation != null) {
            return lastLocation.distanceTo(currentLocation); // meters
        }
        return 0.0f;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentLocation != null) {
            lastLocation = new Location(currentLocation);
        }
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}
