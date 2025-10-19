package com.shuttleroid.vehicle.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.location.*;

public class GpsProvider {
    private final FusedLocationProviderClient fused;
    private final MutableLiveData<Location> locationLive = new MutableLiveData<>();
    private LocationCallback callback;

    public GpsProvider(Context ctx){
        fused = LocationServices.getFusedLocationProviderClient(ctx.getApplicationContext());
    }

    public LiveData<Location> locationLive(){ return locationLive; }

    @SuppressLint("MissingPermission")
    public void start(){
        if (callback != null) return;
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(1000L).build();

        callback = new LocationCallback() {
            @Override public void onLocationResult(LocationResult result) {
                if (result == null) return;
                Location l = result.getLastLocation();
                if (l != null) locationLive.postValue(l);
            }
        };
        fused.requestLocationUpdates(req, callback, null);
    }

    public void stop(){
        if (callback == null) return;
        fused.removeLocationUpdates(callback);
        callback = null;
    }
}
