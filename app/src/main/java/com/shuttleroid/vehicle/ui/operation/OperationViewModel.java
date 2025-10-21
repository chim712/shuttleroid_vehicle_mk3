package com.shuttleroid.vehicle.ui.operation;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.controller.DriveController;
import com.shuttleroid.vehicle.location.GpsProvider;
import com.shuttleroid.vehicle.service.OperationService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 운행 뷰모델
 * - ForegroundService 및 GPS 관리
 * - DriveController 제어
 * - UI용 LiveData 제공 (시계, 상태 등)
 */
public class OperationViewModel extends AndroidViewModel {

    private final GpsProvider gps;
    private DriveController controller;

    // ===== LiveData for UI =====
    private final MutableLiveData<String> clockText = new MutableLiveData<>("--:--:--");
    private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);

    private final DateTimeFormatter clockFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public OperationViewModel(@NonNull Application app) {
        super(app);
        gps = new GpsProvider(app);
    }

    // region === 기본 Service 제어 ===
    public void startService(){
        getApplication().startService(new Intent(getApplication(), OperationService.class));
        gps.start();
    }

    public void stopService(){
        gps.stop();
        getApplication().stopService(new Intent(getApplication(), OperationService.class));
    }
    // endregion

    // region === Controller 관리 ===
    public DriveController ensureController(){
        if (controller == null) {
            controller = new DriveController(getApplication());
            controller.running().observeForever(isRunning::postValue);
        }
        return controller;
    }

    public LiveData<Boolean> runningLive() { return isRunning; }

    public void updateClock(ZonedDateTime now) {
        clockText.postValue(now.toLocalTime().format(clockFmt));
    }

    public LiveData<String> clockTextLive() { return clockText; }

    @Override protected void onCleared() {
        super.onCleared();
        stopService();
    }
}
