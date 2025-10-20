// app/src/main/java/com/shuttleroid/vehicle/data/dao/IntegratedDao.java
package com.shuttleroid.vehicle.data.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.entity.RouteStopCrossRef;

import java.util.List;

@Dao
public interface IntegratedDao {
    // BusStop
    @Query("DELETE FROM busstop") void clearStops();
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertStops(List<BusStop> stops);
    @Query("SELECT COUNT(*) FROM busstop") int stopCount(); // for count

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



    // ---- 👇 운행에 필요한 조회 ----
    /** 해당 route의 정류장을 seq 오름차순으로 반환 */
    @Query("SELECT bs.* FROM route_stop_ref r " +
            "JOIN busstop bs ON bs.stopID = r.stopID " +
            "WHERE r.routeID = :routeId " +
            "ORDER BY r.seq ASC")
    List<BusStop> getStopsForRoute(long routeId);

    /** 첫 정류장명 (UI 표기용) */
    @Query("SELECT bs.stopName FROM route_stop_ref r " +
            "JOIN busstop bs ON bs.stopID = r.stopID " +
            "WHERE r.routeID = :routeId " +
            "ORDER BY r.seq ASC LIMIT 1")
    String findFirstStopName(long routeId);
}
