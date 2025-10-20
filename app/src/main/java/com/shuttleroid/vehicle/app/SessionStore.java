package com.shuttleroid.vehicle.app;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 프로세스 생존 동안 유지되는 세션 메모리.
 * - orgId, driverId, vehicleId, currentCourseId
 * - currentRouteId, currentDepartTime("HH:mm")
 *
 * 주의: 프로세스 재시작 시 휘발성. 필요하면 SharedPreferences로 영속화하세요.
 */
public class SessionStore {

    private static SessionStore instance;

    private final MutableLiveData<Long>   orgId             = new MutableLiveData<>();
    private final MutableLiveData<Long>   driverId          = new MutableLiveData<>();
    private final MutableLiveData<String> vehicleId         = new MutableLiveData<>();
    private final MutableLiveData<String> currentCourseId   = new MutableLiveData<>();

    // ★ 추가
    private final MutableLiveData<Long>   currentRouteId    = new MutableLiveData<>();
    private final MutableLiveData<String> currentDepartTime = new MutableLiveData<>(); // "HH:mm"

    public static synchronized SessionStore getInstance() {
        if (instance == null) instance = new SessionStore();
        return instance;
    }

    // ----------------- Setters -----------------
    public void setOrgId(@Nullable Long v){ orgId.postValue(v); }
    public void setDriverId(@Nullable Long v){ driverId.postValue(v); }
    public void setVehicleId(@Nullable String v){ vehicleId.postValue(v); }
    public void setCurrentCourseId(@Nullable String v){ currentCourseId.postValue(v); }

    // ★ 추가
    public void setCurrentRouteId(@Nullable Long v){ currentRouteId.postValue(v); }
    /** "HH:mm" (24h) */
    public void setCurrentDepartTime(@Nullable String v){ currentDepartTime.postValue(v); }

    // ----------------- Getters -----------------
    @Nullable public Long getOrgId(){ return orgId.getValue(); }
    @Nullable public Long getDriverId(){ return driverId.getValue(); }
    @Nullable public String getVehicleId(){ return vehicleId.getValue(); }
    @Nullable public String getCurrentCourseId(){ return currentCourseId.getValue(); }

    // ★ 추가
    @Nullable public Long getCurrentRouteId(){ return currentRouteId.getValue(); }
    @Nullable public String getCurrentDepartTime(){ return currentDepartTime.getValue(); }

    // ----------------- LiveData -----------------
    public LiveData<Long> orgIdLive(){ return orgId; }
    public LiveData<Long> driverIdLive(){ return driverId; }
    public LiveData<String> vehicleIdLive(){ return vehicleId; }
    public LiveData<String> currentCourseIdLive(){ return currentCourseId; }

    // ★ 추가
    public LiveData<Long> currentRouteIdLive(){ return currentRouteId; }
    public LiveData<String> currentDepartTimeLive(){ return currentDepartTime; }

    // ----------------- Helpers -----------------
    /** 로그인 직후 한번에 세팅할 때 편의용 */
    public void setOnLogin(@Nullable Long orgId,
                           @Nullable Long driverId,
                           @Nullable String vehicleId){
        setOrgId(orgId);
        setDriverId(driverId);
        setVehicleId(vehicleId);
    }

    /** 코스 선택/주입 시 한번에 세팅할 때 편의용 */
    public void setCurrentCourse(@Nullable String courseId,
                                 @Nullable Long routeId,
                                 @Nullable String departTime){
        setCurrentCourseId(courseId);
        setCurrentRouteId(routeId);
        setCurrentDepartTime(departTime);
    }

    public void clear(){
        orgId.postValue(null);
        driverId.postValue(null);
        vehicleId.postValue(null);
        currentCourseId.postValue(null);
        currentRouteId.postValue(null);
        currentDepartTime.postValue(null);
    }
}
