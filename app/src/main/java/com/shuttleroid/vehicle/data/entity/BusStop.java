package com.shuttleroid.vehicle.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class BusStop {
    @PrimaryKey(autoGenerate = false)
    public long stopID;
    public String stopName;
    public double latitude;
    public double longitude;

    // Bus Stop Approach Judge Boundary
    @ColumnInfo(name="lineApproach", defaultValue = "200")
    @NonNull
    public int approach;
    @ColumnInfo(name="lineArrive", defaultValue = "20")
    public int arrival;
    @ColumnInfo(name="lineDepart", defaultValue = "30")
    public int depart;

    // Announcement - default: TTS Sound
    @ColumnInfo(name="announce", defaultValue = "default")
    public String announce;

    public BusStop(){}
    @Ignore
    public BusStop(long stopID, String stopName, double lat, double lon, int approach, int arrival, int depart, String announce) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.latitude = lat;
        this.longitude = lon;
        this.approach = approach;
        this.arrival = arrival;
        this.depart = depart;
        this.announce = announce;
    }


    public long getStopID() {
        return stopID;
    }

    public String getStopName() {
        return stopName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getApproach() {
        return approach;
    }

    public int getArrival() {
        return arrival;
    }

    public int getDepart() {
        return depart;
    }

    public String getAnnounce() {
        return announce;
    }
}
