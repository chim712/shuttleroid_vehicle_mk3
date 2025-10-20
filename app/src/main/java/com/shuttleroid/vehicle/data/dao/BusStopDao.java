// app/src/main/java/com/shuttleroid/vehicle/data/dao/BusStopDao.java
package com.shuttleroid.vehicle.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.shuttleroid.vehicle.data.entity.BusStop;
import java.util.List;

@Dao
public interface BusStopDao {
    @Query("DELETE FROM busstop") void clear();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<BusStop> stops);

    @Query("SELECT COUNT(*) FROM busstop") int count();
}
