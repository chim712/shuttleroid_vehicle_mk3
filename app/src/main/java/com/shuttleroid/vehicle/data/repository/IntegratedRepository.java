package com.shuttleroid.vehicle.data.repository;

import android.content.Context;

import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.database.AppDatabase;
import com.shuttleroid.vehicle.data.dto.DataInfoDto;
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.mapper.IntegratedMapper;

import java.util.List;
import java.util.concurrent.Executors;

public class IntegratedRepository {
    // field -----------------------------
    private static volatile IntegratedRepository instance;
    private final IntegratedDao integratedDao;
    // constructor -----------------------
    private IntegratedRepository(IntegratedDao integratedDao){
        this.integratedDao = integratedDao;
    }
    public static IntegratedRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (IntegratedRepository.class) {
                if (instance == null) {
                    AppDatabase database = AppDatabase.getInstance(context);
                    instance = new IntegratedRepository(database.integratedDao());
                }
            }
        }
        return instance;

    }
    // DB logic ---------------------------
    public void replaceAll(List<Route> routes, List<BusStop> stops) {
        integratedDao.replaceAll(routes, stops);
    }

    public void replaceAllFromDto(DataInfoDto dto) {
        IntegratedMapper.DataBundle bundle = IntegratedMapper.fromDto(dto);
        if (bundle != null) {
            AppExecutors.diskIO().execute(() -> {
                integratedDao.replaceAll(bundle.routes, bundle.stops);
            });
        }
    }
}