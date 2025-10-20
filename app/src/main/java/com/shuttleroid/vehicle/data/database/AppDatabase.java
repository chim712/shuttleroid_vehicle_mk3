// app/src/main/java/com/shuttleroid/vehicle/data/database/AppDatabase.java
package com.shuttleroid.vehicle.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.entity.*;

@Database(
        entities = { BusStop.class, Route.class, RouteStopCrossRef.class },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract IntegratedDao integratedDao();

    public static AppDatabase get(Context ctx){
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "vehicle.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
