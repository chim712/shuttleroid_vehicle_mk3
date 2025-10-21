package com.shuttleroid.vehicle.controller;

import static com.shuttleroid.vehicle.controller.DriveController.Event.DEPART;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationServices;

import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;
import com.shuttleroid.vehicle.location.Geomath;
import com.shuttleroid.vehicle.network.api.SyncService;
import com.shuttleroid.vehicle.network.client.RetrofitProvider;
import com.shuttleroid.vehicle.network.dto.LocationEvent;
import com.shuttleroid.vehicle.network.dto.RouteReport;
import com.shuttleroid.vehicle.service.AnnounceManager;
import com.shuttleroid.vehicle.ui.operation.OperationFragment;
import com.shuttleroid.vehicle.util.Toasts;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 운행 제어기 (WAITING -> RUNNING -> TERMINATED)
 *
 * 상호작용 규약:
 * - Repository: IntegratedRepository#getOrderedStops(routeId)
 * - StopEngine: radiiOf(BusStop), judge(lat,lon,stop,radii)
 * - TTS: AnnounceManager (Approach 시점만 방송)
 * - 네트워크: SyncService (routeStart/terminate/location)
 * - 세션(Optional): SessionStore (vehicleID / 현재 코스 저장에 사용 가능)
 */
public class DriveController {

    public enum State { WAITING, RUNNING, TERMINATED}
    public enum Event { NONE, APPROACH, ARRIVE, DEPART }

    public interface Callbacks {
        default void onAutoStart() {}
        default void onLocationEvent(Event e) {}
        default void onReachedTerminal() {}
        default void onTick(ZonedDateTime now) {}
    }

    // ===== Deps =====
    private final Context app;
    private final IntegratedRepository repo;
    private final SyncService api;
    private final FusedLocationProviderClient fused;

    // ===== Runtime State =====
    private volatile State state = State.WAITING;
    private final MutableLiveData<Boolean> runningLive = new MutableLiveData<>(false);

    private List<BusStop> stops = new ArrayList<>();
    private int index = 0;

    private long routeId = -1L;          // 필수
    private String departTime = null;     // "HH:mm" (필수)
    private String courseId = null;       // 서버 스케줄 ID(선택/있으면 보고에 포함)
    private String vehicleId = null;      // 필수

    private Callbacks callbacks;

    // ===== Exec =====
    private final ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    // ===== Retry Policy =====
    private static final int RETRY_MAX = 30;
    private static final long RETRY_DELAY_SEC = 5L;

    public DriveController(@NonNull Context appCtx) {
        this.app = appCtx.getApplicationContext();
        this.repo = new IntegratedRepository(app);
        this.api = RetrofitProvider.get().create(SyncService.class);
        this.fused = LocationServices.getFusedLocationProviderClient(app);
    }

    /** 외부에서 관찰용 */
    public MutableLiveData<Boolean> running() { return runningLive; }

    /** OperationFragment 진입 시점에 route/depart/course/vehicle을 세팅 */
    public void setRouteContext(long routeId, @NonNull String departTime,
                                String courseId, @NonNull String vehicleId) {
        this.routeId = routeId;
        this.departTime = departTime;
        this.courseId = courseId;
        this.vehicleId = vehicleId;
        // 세션에도 저장해 두면 재시작 복원에 유용
        SessionStore s = SessionStore.getInstance();
        s.setCurrentRouteId(routeId);
        s.setCurrentDepartTime(departTime);
        s.setCurrentCourseId(courseId);
        s.setVehicleId(vehicleId);
    }

    // =========================================================
    // WAITING
    // =========================================================

    /** 자동 출발 예약(T-3분) + 1초마다 틱 콜백 */
    @MainThread
    public void startWaiting(@NonNull LocalTime depart,
                             @NonNull LifecycleOwner owner,
                             @NonNull Callbacks cb) {
        this.callbacks = cb;
        this.state = State.WAITING;
        runningLive.postValue(false);

        // 틱
        sched.scheduleWithFixedDelay(() -> {
            if (callbacks != null) callbacks.onTick(ZonedDateTime.now());
        }, 0, 1, TimeUnit.SECONDS);

        // 자동시작 예약
        long delayMs = computeDelayTo(depart.minusMinutes(3));
        sched.schedule(this::transitionToRunning, Math.max(0, delayMs), TimeUnit.MILLISECONDS);
    }

    /** 수동 출발 */
    @MainThread
    public void forceStart(@NonNull Callbacks cb) {
        this.callbacks = cb;
        transitionToRunning();
    }

    private long computeDelayTo(LocalTime trigger) {
        Duration d = Duration.between(LocalTime.now(), trigger);
        return d.toMillis();
    }

    // =========================================================
    // RUNNING
    // =========================================================

    private void transitionToRunning() {
        if (state == State.RUNNING) return;

        // 필수 값 보정 (혹시 외부 setRouteContext를 안 했을 경우 SessionStore에서 백업)
        SessionStore ss = SessionStore.getInstance();
        if (routeId <= 0) routeId = ss.getCurrentRouteId() != null ? ss.getCurrentRouteId() : -1L;
        if (departTime == null) departTime = ss.getCurrentDepartTime();
        if (vehicleId == null) vehicleId = ss.getVehicleId();
        if (courseId == null) courseId = ss.getCurrentCourseId();

        if (routeId <= 0 || departTime == null || vehicleId == null) {
            // UI 작업은 메인 스레드
            main.post(() -> Toasts.throttled(app, "운행 정보를 확인하세요(route/depart/vehicle)"));
            return;
        }

        // === DB 접근은 반드시 IO 스레드에서 ===
        io.submit(() -> {
            List<BusStop> s;
            try {
                s = repo.getOrderedStops(routeId);
            } catch (Exception ex) {
                main.post(() -> Toasts.throttled(app, "정류장 조회 실패: " + ex.getMessage()));
                return;
            }

            if (s == null || s.isEmpty()) {
                main.post(() -> Toasts.throttled(app, "정류장 정보가 없습니다"));
                return;
            }

            // 런타임 상태 갱신
            stops = s;
            index = 0;

            // 네트워크/로케이션/LiveData/콜백 등은 메인 스레드로
            main.post(() -> {
                sendRouteReport(true);
                startLocationUpdates();

                state = State.RUNNING;
                runningLive.setValue(true); // 메인에서 setValue

                if (callbacks != null) callbacks.onAutoStart();
            });
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest req = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 2000L) // 2s
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(0f)
                .build();

        fused.requestLocationUpdates(req, locationCallback,
                (Build.VERSION.SDK_INT >= 28) ? app.getMainLooper() : android.os.Looper.getMainLooper());
    }

    private Event currentState = DEPART;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override public void onLocationResult(@NonNull LocationResult result) {
            Location loc = result.getLastLocation();
            if (loc == null) return;
            if (state != State.RUNNING) return;
            if (stops == null || stops.isEmpty()) return;
            if (index < 0 || index >= stops.size()) return;

            BusStop target = stops.get(index);

            double distance = Geomath.distanceMeters(loc.getLatitude(), loc.getLongitude(), target.latitude, target.longitude);
            // judge logic
            switch(currentState){
                case DEPART:
                    if(distance <= target.approach){
                        currentState = Event.APPROACH;
                        AnnounceManager am = AnnounceManager.getInstance(app);
                        //boolean hasNext = (index + 1 < stops.size());

                        sendLocationEvent(currentState, target);

                        // ignore waypoint announce
                        int key = (int)(target.stopID / 1000 % 10);
                        if(key == 9) break;

                        String curr = target.stopName;
                        String next = null;
                        BusStop tmp;

                        for(int i=1; index + i < stops.size(); i++){
                            tmp = stops.get(index + i);
                            key = (int)(tmp.stopID / 1000 % 10);
                            if(key != 9) {
                                next = tmp.stopName;
                                break;
                            }
                        }

                         //next = hasNext ? stops.get(index + 1).stopName : null;
                        if (next != null) am.typeA(curr, next);      // normal stop
                        else         am.typeB(curr);            // terminal stop

                    }
                    break;
                case APPROACH:
                    if(distance <= target.arrival){
                        currentState = Event.ARRIVE;
                        sendLocationEvent(currentState, target);

                        // is Terminal?
                        boolean atLast = (index == stops.size() - 1);
                        if (atLast) {
                            reachTerminal();
                        }
                    }
                    break;
                case ARRIVE:
                    if(distance >= target.depart){
                        currentState = DEPART;

                        index++;
                        OperationFragment.increaseIdx();
                        if (index >= stops.size()) index = stops.size() - 1; // safety

                        sendLocationEvent(currentState, target);
                    }
                    break;
            }

            if (callbacks != null) callbacks.onLocationEvent(currentState);
        }
    };

    private void reachTerminal() {
        // 보고: /route/terminate
        sendRouteReport(false);

        // 위치 구독 해제
        fused.removeLocationUpdates(locationCallback);

        state = State.TERMINATED;
        runningLive.postValue(false);
        if (callbacks != null) callbacks.onReachedTerminal();
    }

    // =========================================================
    // 네트워크 보고 (공통 재시도)
    // =========================================================

    private void sendRouteReport(boolean flag) {
        //RouteReport r = RouteReport.of(courseId, routeId, departTime, vehicleId, flag);
        RouteReport r = RouteReport.of(101001L, routeId, departTime, vehicleId, flag);
        postWithRetry(() -> api.routeStart(r), () -> api.routeTerminate(r), flag ? "routeStart" : "routeTerminate");
    }

    private void sendLocationEvent(Event ev, BusStop target) {
        String status;
        switch (ev) {
            case APPROACH: status = "Approach"; break;
            case ARRIVE:   status = "Arrive";   break;
            case DEPART:   status = "Depart";   break;
            default: return;
        }
        LocationEvent e = LocationEvent.of(vehicleId, target.stopID, status);
        postWithRetry(() -> api.postLocation(e), null, "location:" + status);
    }

    /**
     * postWithRetry:
     *  - 일반 보고: callFactoryOnly 사용 (success/4xx/5xx 재시도)
     *  - start/terminate처럼 두 엔드포인트가 다른 경우: startFactory / terminateFactory로 분기
     */
    private void postWithRetry(Callable<Call<ResponseBody>> callFactoryOnly,
                               String tag) {
        attempt(callFactoryOnly, tag, 1);
    }

    private void postWithRetry(Callable<Call<ResponseBody>> startFactory,
                               Callable<Call<ResponseBody>> terminateFactory,
                               String tag) {
        // start/terminate 구분 없는 단일 호출일 경우 terminateFactory가 null일 수 있음
        attempt(startFactory, tag, 1);
    }

    private void attempt(Callable<Call<ResponseBody>> factory, String tag, int attempt) {
        try {
            Call<ResponseBody> call = factory.call();
            call.enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> resp) {
                    if (!resp.isSuccessful()) retry();
                }
                @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                    retry();
                }
                private void retry() {
                    if (attempt >= RETRY_MAX) {
                        main.post(() -> Toasts.throttled(app, "보고 실패: " + tag));
                        return;
                    }
                    sched.schedule(() -> attempt(factory, tag, attempt + 1),
                            RETRY_DELAY_SEC, TimeUnit.SECONDS);
                }
            });
        } catch (Exception ex) {
            if (attempt >= RETRY_MAX) {
                main.post(() -> Toasts.throttled(app, "보고 예외: " + tag + " / " + ex.getMessage()));
            } else {
                sched.schedule(() -> attempt(factory, tag, attempt + 1),
                        RETRY_DELAY_SEC, TimeUnit.SECONDS);
            }
        }
    }

    // =========================================================
    // 종료/정리
    // =========================================================
    public void shutdown() {
        fused.removeLocationUpdates(locationCallback);
        sched.shutdownNow();
        io.shutdownNow();
    }
}
