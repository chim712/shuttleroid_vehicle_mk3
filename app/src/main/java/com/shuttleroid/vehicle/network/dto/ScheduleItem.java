// app/src/main/java/com/shuttleroid/vehicle/network/dto/ScheduleItem.java
package com.shuttleroid.vehicle.network.dto;

import com.google.gson.annotations.SerializedName;

/** GET /schedule 응답 원소 (routeID가 문자열로 내려옴) */
public class ScheduleItem {
    @SerializedName("routeID") public Long routeID;       // "102" / "192"
    @SerializedName("departureTime") public String departureTime; // "HH:mm"

    public Long getRouteID() {
        return routeID;
    }

    public String getDepartTime() {
        return departureTime;
    }
}


