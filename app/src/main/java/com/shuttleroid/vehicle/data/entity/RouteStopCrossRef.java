// app/src/main/java/com/shuttleroid/vehicle/data/entity/RouteStopCrossRef.java
package com.shuttleroid.vehicle.data.entity;

import androidx.room.Entity;

@Entity(tableName = "route_stop_ref", primaryKeys = {"routeID","stopID","seq"})
public class RouteStopCrossRef {
    public long routeID;
    public long stopID;
    public int seq; // 0..n
}
