// app/src/main/java/com/shuttleroid/vehicle/data/entity/Route.java
package com.shuttleroid.vehicle.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "route")
public class Route {
    @PrimaryKey public long routeID;          // Long 통일
    @NonNull public String routeName = "";
    public String spendTime;                  // 분(문자열 보존)
}
