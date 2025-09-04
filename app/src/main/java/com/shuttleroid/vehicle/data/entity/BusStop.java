package com.shuttleroid.vehicle.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
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
}
