package com.shuttleroid.vehicle.app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SessionStore {

    private static SessionStore instance;

    private final MutableLiveData<Long> orgId = new MutableLiveData<>(null);
    private final MutableLiveData<Long> driverId = new MutableLiveData<>(null);
    private final MutableLiveData<String> vehicleId = new MutableLiveData<>(null);
    private final MutableLiveData<String> currentCourseId = new MutableLiveData<>(null);

    public static synchronized SessionStore getInstance() {
        if (instance == null) instance = new SessionStore();
        return instance;
    }

    public void setOrgId(Long v){ orgId.postValue(v); }
    public void setDriverId(Long v){ driverId.postValue(v); }
    public void setVehicleId(String v){ vehicleId.postValue(v); }
    public void setCurrentCourseId(String v){ currentCourseId.postValue(v); }

    public Long getOrgId(){ return orgId.getValue(); }
    public Long getDriverId(){ return driverId.getValue(); }
    public String getVehicleId(){ return vehicleId.getValue(); }
    public String getCurrentCourseId(){ return currentCourseId.getValue(); }

    public LiveData<Long> orgIdLive(){ return orgId; }
    public LiveData<Long> driverIdLive(){ return driverId; }
    public LiveData<String> vehicleIdLive(){ return vehicleId; }
    public LiveData<String> currentCourseIdLive(){ return currentCourseId; }

    public void clear(){
        orgId.postValue(null);
        driverId.postValue(null);
        vehicleId.postValue(null);
        currentCourseId.postValue(null);
    }
}
