package com.shuttleroid.vehicle.domain;

// Location Update and Calculate
// BusStop Status Update

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.model.UserSession;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;
import com.shuttleroid.vehicle.service.AnnounceManager;
import com.shuttleroid.vehicle.service.SyncManager;

// Static Util Class
public class StopProcessor {

    static SyncManager syncManager = SyncManager.getInstance(null);
    private static BusStop currentStop, nextStop;
    private static MutableLiveData<String> currentStopLive = new MutableLiveData<>();

    public static LiveData<String> getCurrentStopLive(){
        return currentStopLive;
    }



    //=============== Main Logic ==============
    public static void updateLocation(double lat, double lon){
        // TODO: get location from gps + remain distance calculate
    }

    public static void init(String s){
        currentStopLive.postValue(s);
    }
    public static void updateCurrentStop(BusStop stop){
        currentStop = stop;
        Log.d("StopProcessor", "Current: "+currentStop.stopName);
        currentStopLive.postValue(currentStop.stopName);
    }
    public static void updateNextStop(BusStop stop){
        nextStop = stop;
        Log.d("StopProcessor", "Next: "+nextStop.stopName);
    }

    private static void onApproach(){
        //TODO: send server + announce

        syncManager.sendLocationReport(
                String.valueOf(UserSession.getVehicleNo()),
                "학내순환",
                currentStopLive+"접근"
        );

        AnnounceManager.getInstance(null)
                .startAnnouncement(currentStop.stopName, nextStop.stopName);
    }
    private static void onArrive(){
        //TODO: send server + route end check

        syncManager.sendLocationReport(
                String.valueOf(UserSession.getVehicleNo()),
                "학내순환",
                currentStopLive+"도착"
        );
    }
    private static void onLeave(){
        //TODO: send server + change stop (terminal stop check)

        /* TODO: remove hard-coding*/
        syncManager.sendLocationReport(
                String.valueOf(UserSession.getVehicleNo()),
                "학내순환",
                currentStopLive+"출발"
        );

        if(CourseProcessor.isTerminal()){
            CourseProcessor.changeRoute();
        }else{
            CourseProcessor.nextStop();
        }
    }




    // Haversine formula
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
