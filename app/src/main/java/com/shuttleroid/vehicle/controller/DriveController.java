package com.shuttleroid.vehicle.controller;

import android.content.Context;
import android.location.Location;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.domain.StopEngine;
import com.shuttleroid.vehicle.network.api.SyncService;
import com.shuttleroid.vehicle.util.TimeUtil;

import java.time.LocalTime;
import java.time.ZonedDateTime;

public class DriveController {

    public enum Mode { WAITING, RUNNING }

    private final Context app;
    private final LiveData<Location> locLive;
    private final MutableLiveData<Mode> mode = new MutableLiveData<>(Mode.WAITING);

    private LocalTime departTimeHm; // 해당 코스 출발시각
    private boolean insideStop = false;

    public interface Callbacks {
        void onAutoStart();                  // 3분 전 자동 시작
        void onLocationEvent(StopEngine.Event e); // Approach/Arrive/Depart 발생
        void onReachedTerminal();            // 종점 도달
        void onTick(ZonedDateTime now);      // 1초 tick (시계 UI)
    }

    public DriveController(Context app, LiveData<Location> locLive){
        this.app = app.getApplicationContext();
        this.locLive = locLive;
    }

    public LiveData<Mode> mode(){ return mode; }

    public void startWaiting(LocalTime departHm, LifecycleOwner owner, Callbacks cb){
        this.departTimeHm = departHm;
        mode.postValue(Mode.WAITING);

        locLive.observe(owner, l -> {
            // tick for clock
            cb.onTick(TimeUtil.nowKst());

            // WAITING -> RUNNING (3분 전 자동)
            LocalTime now = TimeUtil.nowKst().toLocalTime();
            if (now.plusMinutes(0).isAfter(departHm.minusMinutes(3))) {
                if (mode.getValue() == Mode.WAITING) {
                    mode.postValue(Mode.RUNNING);
                    cb.onAutoStart();
                }
            }
        });
    }

    public void forceStart(Callbacks cb){
        mode.postValue(Mode.RUNNING);
        cb.onAutoStart();
    }

    public void observeRunning(LifecycleOwner owner, Callbacks cb){
        locLive.observe(owner, l -> {
            cb.onTick(TimeUtil.nowKst());
            if (l == null) return;
            // TODO: 현재 코스의 "다음 대상 정류장"을 가져와 StopEngine.judge(...) 호출
            // 아래는 개략:
            // StopEngine.Event ev = StopEngine.judge(nextStop, l, insideStop);
            // switch(ev){ case APPROACH: ...; case ARRIVE: insideStop=true; ...; case DEPART: insideStop=false; ...}
            // 종점 도달 판단 시 cb.onReachedTerminal();
        });
    }

    public void stop(){
        // no-op (관찰 해제는 owner lifecycle이 담당)
    }
}
