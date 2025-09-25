package com.shuttleroid.vehicle.data.dto;

import com.google.gson.annotations.SerializedName;

// From application -> To Server
public class LocationReportDto {
    @SerializedName("vehicleNo")
    private String vehicleNo;

    @SerializedName("route")
    private String route;

    @SerializedName("stopLocation")
    private String stopLocation;

    public LocationReportDto(String vehicleNo, String route, String stopLocation){
        this.vehicleNo = vehicleNo;
        this.route = route;
        this.stopLocation = stopLocation;
    }
}
