// app/src/main/java/com/shuttleroid/vehicle/data/repo/IntegratedRepository.java
package com.shuttleroid.vehicle.data.repository;

import android.content.Context;
import androidx.annotation.WorkerThread;
import com.shuttleroid.vehicle.data.database.AppDatabase;
import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.entity.*;
import com.shuttleroid.vehicle.data.mapper.UpdateSnapshotMapper;
import com.shuttleroid.vehicle.network.dto.UpdateSnapshot;

import java.util.List;

public class IntegratedRepository {
    private final IntegratedDao dao;

    public IntegratedRepository(Context ctx){
        this.dao = AppDatabase.get(ctx).integratedDao();
    }

    @WorkerThread
    public void replaceAll(UpdateSnapshot snap){
        List<BusStop> stops = UpdateSnapshotMapper.toStops(snap);
        List<Route> routes = UpdateSnapshotMapper.toRoutes(snap);
        List<RouteStopCrossRef> refs = UpdateSnapshotMapper.toRefs(snap);
        AppDatabase.get(null); // no-op; keep singleton warm
        // 트랜잭션
        dao.replaceAll(routes, stops, refs);
    }

    public int busStopCount(){ return dao.stopCount(); }
}
