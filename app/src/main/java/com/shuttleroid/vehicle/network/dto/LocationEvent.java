package com.shuttleroid.vehicle.network.dto;

/** POST /location
 *  status: "Approach" | "Arrive" | "Depart"
 */
public class LocationEvent {
    public String vehicleID;
    public Long stopID;
    public String status;

    public static LocationEvent of(String vehicleID, Long stopID, String status){
        LocationEvent e = new LocationEvent();
        e.vehicleID = vehicleID;
        e.stopID = stopID;
        e.status = status;
        return e;
    }
}
