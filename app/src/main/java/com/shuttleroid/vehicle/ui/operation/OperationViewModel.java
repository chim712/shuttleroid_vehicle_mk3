package com.shuttleroid.vehicle.ui.operation;

import android.app.Application;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.controller.DriveController;
import com.shuttleroid.vehicle.location.GpsProvider;
import com.shuttleroid.vehicle.service.OperationService;

import java.time.LocalTime;

public class OperationViewModel extends AndroidViewModel {

    private final GpsProvider gps;
    private DriveController controller;

    public OperationViewModel(@NonNull Application app) {
        super(app);
        gps = new GpsProvider(app);
    }

    public LiveData<android.location.Location> locationLive(){ return gps.locationLive(); }

    public void startService(){
        getApplication().startService(new Intent(getApplication(), OperationService.class));
        gps.start();
    }

    public void stopService(){
        gps.stop();
        getApplication().stopService(new Intent(getApplication(), OperationService.class));
    }

    public DriveController ensureController(){
        if (controller==null) controller = new DriveController(getApplication(), gps.locationLive());
        return controller;
    }

    @Override protected void onCleared() {
        super.onCleared();
        stopService();
    }
}
