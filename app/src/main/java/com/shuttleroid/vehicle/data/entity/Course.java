package com.shuttleroid.vehicle.data.entity;

import java.time.LocalTime;

public class Course {
    long routeID;
    LocalTime departureTime;

    public Course(){}
    public Course(long routeID, LocalTime departureTime) {
        this.routeID = routeID;
        this.departureTime = departureTime;
    }

    public String toString(){
        return routeID + " " + departureTime;
    }

    public long getRouteID() {
        return routeID;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }
}
