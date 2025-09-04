// RouteDao.java
package com.shuttleroid.vehicle.data.dao;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.entity.BusStop;

@Dao
public interface IntegratedDao {

    // ====== 삽입/갱신 ======
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRoutes(List<Route> routes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStops(List<BusStop> stops);

    @Query("DELETE FROM Route")
    void clearRoutes();

    @Query("DELETE FROM BusStop")
    void clearStops();

    // ====== 조회 ======
    @Query("SELECT * FROM Route WHERE routeID = :routeId LIMIT 1")
    Route getRoute(long routeId);

    @Query("SELECT * FROM Route ORDER BY routeName ASC")
    List<Route> getAllRoutes();

    @Query("SELECT * FROM BusStop WHERE stopID IN (:ids)")
    List<BusStop> getStopsByIds(List<Long> ids);

    // ====== 스냅샷 전체 교체(원자적) ======
    @Transaction    // Integrated Function: Clear All Data & Insert New Data
    default void replaceAll(List<Route> routes, List<BusStop> stops) {
        clearRoutes();
        clearStops();
        insertStops(stops);
        insertRoutes(routes);
        Log.i("Database-DAO","Update Completed");
    }
}
