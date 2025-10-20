// app/src/main/java/com/shuttleroid/vehicle/data/dao/IntegratedDao.java
package com.shuttleroid.vehicle.data.dao;

import androidx.room.*;
import com.shuttleroid.vehicle.data.entity.*;

import java.util.List;

@Dao
public interface IntegratedDao {
    // BusStop
    @Query("DELETE FROM busstop") void clearStops();
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertStops(List<BusStop> stops);
    @Query("SELECT COUNT(*) FROM busstop") int stopCount();

    // Route
    @Query("DELETE FROM route") void clearRoutes();
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertRoutes(List<Route> routes);

    // CrossRef
    @Query("DELETE FROM route_stop_ref") void clearRefs();
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertRefs(List<RouteStopCrossRef> refs);

    @Transaction
    default void replaceAll(List<Route> routes, List<BusStop> stops, List<RouteStopCrossRef> refs){
        clearRefs();
        clearRoutes();
        clearStops();
        if (routes!=null && !routes.isEmpty()) upsertRoutes(routes);
        if (stops!=null && !stops.isEmpty()) upsertStops(stops);
        if (refs!=null && !refs.isEmpty()) upsertRefs(refs);
    }
}
