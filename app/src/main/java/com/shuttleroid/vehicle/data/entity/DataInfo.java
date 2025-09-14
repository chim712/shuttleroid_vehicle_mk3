package com.shuttleroid.vehicle.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DataInfo {
    @PrimaryKey(autoGenerate = false)
    public long orgID;
    @ColumnInfo(name="version", defaultValue = "0")
    public long version;
}
