package com.shuttleroid.vehicle.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Route {
    @PrimaryKey(autoGenerate = false)
    public long routeID;
    public String routeName;
    public int spendTime;

    public List<Long> stopIds;
}
