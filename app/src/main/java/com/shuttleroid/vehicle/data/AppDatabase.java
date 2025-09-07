package com.shuttleroid.vehicle.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.entity.RouteConverter;

@Database(entities = {Route.class, BusStop.class}, version = 1)
@TypeConverters(RouteConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract IntegratedDao integratedDao();
}
