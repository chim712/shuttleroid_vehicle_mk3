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
    public double lat;
    public double lon;

    // Bus Stop Approach Judge Boundary
    @ColumnInfo(name="lineApproach", defaultValue = "200")
    @NonNull
    public int lineApproach;
    @ColumnInfo(name="lineArrive", defaultValue = "20")
    public int lineArrive;
    @ColumnInfo(name="lineDepart", defaultValue = "30")
    public int lineDepart;

    // Announcement - default: TTS Sound
    @ColumnInfo(name="announce", defaultValue = "default")
    public String announce;

    public BusStop(){}
    @Ignore
    public BusStop(long stopID, String stopName, double lat, double lon, int lineApproach, int lineArrive, int lineDepart, String announce) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.lat = lat;
        this.lon = lon;
        this.lineApproach = lineApproach;
        this.lineArrive = lineArrive;
        this.lineDepart = lineDepart;
        this.announce = announce;
    }
}
