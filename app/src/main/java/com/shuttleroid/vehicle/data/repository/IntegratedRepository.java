package com.shuttleroid.vehicle.data.repository;

import android.content.Context;

import androidx.annotation.WorkerThread;

import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.database.AppDatabase;
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.entity.RouteStopCrossRef;
import com.shuttleroid.vehicle.data.mapper.UpdateSnapshotMapper;
import com.shuttleroid.vehicle.network.dto.UpdateSnapshot;

import java.util.Collections;
import java.util.List;

public class IntegratedRepository {
    private final AppDatabase db;
    private final IntegratedDao dao;

    public IntegratedRepository(Context ctx) {
        this.db = AppDatabase.get(ctx);
        this.dao = db.integratedDao();
    }

    // /update 반영 (기존 구현 있으면 그대로 두셔도 됨)
    @WorkerThread
    public void replaceAll(UpdateSnapshot snap) {
        List<BusStop> stops = UpdateSnapshotMapper.toStops(snap);
        List<Route> routes = UpdateSnapshotMapper.toRoutes(snap);
        List<RouteStopCrossRef> refs = UpdateSnapshotMapper.toRefs(snap);
        dao.replaceAll(routes, stops, refs);
    }

    // --- 운행에 필요한 조회 ---
    @WorkerThread
    public List<BusStop> getOrderedStops(long routeId) {
        List<BusStop> res = dao.getStopsForRoute(routeId);
        return res == null ? Collections.emptyList() : res;
    }

    @WorkerThread
    public String findFirstStopName(long routeId) {
        String name = dao.findFirstStopName(routeId);
        return name == null ? "" : name;
    }

    public int busStopCount() { return dao.stopCount(); }
}
