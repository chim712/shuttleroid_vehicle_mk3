package com.shuttleroid.vehicle.data.repository;

import android.content.Context;

import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.database.AppDatabase;
import com.shuttleroid.vehicle.data.dto.CourseDto;
import com.shuttleroid.vehicle.data.dto.DataInfoDto;
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.mapper.IntegratedMapper;
import com.shuttleroid.vehicle.domain.CourseProcessor;

import java.util.List;
import java.util.function.Consumer;

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

    // DB Search (Get)
    public BusStop getBusStop(long stopID){
        return integratedDao.getBusStop(stopID);
    }

    public Route getRoute(long RouteID){
        return integratedDao.getRoute(RouteID);
    }

    public void getRouteAsync(long routeID, Consumer<Route> callback) {
        RoomExecutors.diskIO().execute(() -> {
            Route route = integratedDao.getRoute(routeID);
            RoomExecutors.mainThread().execute(() -> callback.accept(route));
        });
    }

    public void getBusStopAsync(long stopID, Consumer<BusStop> callback) {
        RoomExecutors.diskIO().execute(() -> {
            BusStop stop = integratedDao.getBusStop(stopID);
            RoomExecutors.mainThread().execute(() -> callback.accept(stop));
        });
    }


    // DB logic ---------------------------
    public void replaceAll(List<Route> routes, List<BusStop> stops) {
        integratedDao.replaceAll(routes, stops);
    }

    public void replaceAllFromDto(DataInfoDto dto) {
        IntegratedMapper.DataBundle bundle = IntegratedMapper.fromDto(dto);
        if (bundle != null) {
            RoomExecutors.diskIO().execute(() -> {
                integratedDao.replaceAll(bundle.routes, bundle.stops);
            });
        }
    }

    public void replaceSchedules(List<CourseDto> dto){
        RoomExecutors.diskIO().execute(() -> {
            CourseProcessor.addCourses(IntegratedMapper.fromDtoCourses(dto));
        });
    }
}