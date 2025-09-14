package com.shuttleroid.vehicle.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
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

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            )
                            .build();
                }
            }
        }
        return instance;
    }
}
