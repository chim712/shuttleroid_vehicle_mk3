package com.shuttleroid.vehicle.domain;

import com.shuttleroid.vehicle.service.GpsManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DriveExecutors {
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public enum Mode { WAITING, RUNNING }

    private static volatile Mode currentMode = Mode.WAITING;
    private static volatile boolean isActive = false;

    private static LocalDateTime departTime;
    private static GpsManager gpsManager;

    // 초기화
    public static void init() {
        gpsManager = GpsManager.getInstance();
    }

    // 운행 시작 시간 설정
    public static void setDepartTime(LocalDateTime time) {
        departTime = time;
    }

    // 실행 시작
    public static void start() {
        if (isActive) return;
        isActive = true;

        scheduler.scheduleWithFixedDelay(() -> {
            switch (currentMode) {
                case WAITING:
                    handleWaiting();
                    break;
                case RUNNING:
                    handleRunning();
                    break;
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    // 실행 중지
    public static void stop() {
        isActive = false;
        scheduler.shutdownNow();
    }

    // 모드 전환
    public static void setMode(Mode mode) {
        currentMode = mode;
    }

    // 수동 활성화
    public static void manualActivate() {
        setMode(Mode.RUNNING);
    }

    // ===== Handle Mode =====

    private static void handleWaiting() {
        if (departTime == null) return;

        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(now, departTime).toMinutes() <= 3) {
            setMode(Mode.RUNNING);
        }
    }

    private static void handleRunning() {
        if (gpsManager == null) return;

        double lat = gpsManager.getLatitude();
        double lon = gpsManager.getLongitude();
        StopProcessor.updateLocation(lat, lon);
    }
}
