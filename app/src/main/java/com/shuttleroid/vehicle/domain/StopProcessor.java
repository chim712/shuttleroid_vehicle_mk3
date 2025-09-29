package com.shuttleroid.vehicle.domain;

// Location Update and Calculate
// BusStop Status Update

// Static Util Class
public class StopProcessor {


    //=============== Main Logic ==============
    public static void updateLocation(double lat, double lon){
        // TODO: get location from gps + remain distance calculate
    }
    private static void onApproach(){
        //TODO: send server + announce
    }
    private static void onArrive(){
        //TODO: send server + route end check
    }
    private static void onLeave(){
        //TODO: send server + change stop (terminal stop check)
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
