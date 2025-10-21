package com.shuttleroid.vehicle.ui.update;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;
import com.shuttleroid.vehicle.network.api.SyncService;
import com.shuttleroid.vehicle.network.client.RetrofitProvider;
import com.shuttleroid.vehicle.network.dto.ScheduleItem;
import com.shuttleroid.vehicle.network.dto.UpdateSnapshot;
import com.shuttleroid.vehicle.util.TimeUtil;
import com.shuttleroid.vehicle.util.Toasts;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateViewModel extends AndroidViewModel {

    // ===== Public state =====
    public final MutableLiveData<Boolean> updateDone = new MutableLiveData<>(false);
    public final MutableLiveData<List<ScheduleItem>> scheduleLive = new MutableLiveData<>();

    // ===== Deps =====
    private final SyncService api;
    private final IntegratedRepository repo;

    // ===== Threads =====
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();

    // ===== Retry policy =====
    private static final int RETRY_MAX = 30;
    private static final long RETRY_DELAY_SEC = 5L;
    private static final long TOAST_INTERVAL_SEC = 10L;

    public UpdateViewModel(@NonNull Application app) {
        super(app);
        api = RetrofitProvider.get().create(SyncService.class);
        repo = new IntegratedRepository(app);
    }

    // ------------------------------------------------------------------------
    // UPDATE (data.json)
    // ------------------------------------------------------------------------
    public void doUpdate(long dataVer){
        final Long org = SessionStore.getInstance().getOrgId();
        if (org == null) {
            Toasts.throttled(getApplication(), "orgID 없음");
            return;
        }
        attemptUpdate(org, dataVer, 1, 0);
    }

    private void attemptUpdate(Long org, long dataVer, int attempt, long lastToastSec){
        Call<UpdateSnapshot> call = api.update(org, dataVer);
        call.enqueue(new Callback<UpdateSnapshot>() {
            @Override
            public void onResponse(Call<UpdateSnapshot> c, Response<UpdateSnapshot> resp) {
                if (resp.code() == 204) {
                    Toasts.throttled(getApplication(), "최신버전입니다");
                    updateDone.postValue(true);
                    return;
                }
                if (resp.isSuccessful() && resp.body() != null) {
                    UpdateSnapshot snap = resp.body();
                    io.execute(() -> {
                        try {
                            repo.replaceAll(snap); // Room 저장
                            updateDone.postValue(true);
                            Toasts.throttled(getApplication(),
                                    "데이터 적재 완료 (정류장=" + repo.busStopCount() + ")");
                        } catch (Throwable t) {
                            updateDone.postValue(false);
                            Toasts.throttled(getApplication(), "DB 저장 실패: " + t.getMessage());
                        }
                    });
                } else {
                    retryUpdateMaybe(org, dataVer, attempt, lastToastSec);
                }
            }
            @Override
            public void onFailure(Call<UpdateSnapshot> c, Throwable t) {
                retryUpdateMaybe(org, dataVer, attempt, lastToastSec);
            }
        });
    }

    private void retryUpdateMaybe(Long org, long dataVer, int attempt, long lastToastSec){
        if (attempt >= RETRY_MAX) {
            updateDone.postValue(false);
            Toasts.throttled(getApplication(), "서버 연결 불가");
            return;
        }
        int nextAttempt = attempt + 1;
        long nextDelay = RETRY_DELAY_SEC;

        long nextToastSec = lastToastSec + RETRY_DELAY_SEC;
        if (nextToastSec % TOAST_INTERVAL_SEC == 0) {
            Toasts.throttled(getApplication(),
                    "업데이트 재시도 중... (" + nextAttempt + "/" + RETRY_MAX + ")");
        }

        sched.schedule(() ->
                        attemptUpdate(org, dataVer, nextAttempt, nextToastSec),
                nextDelay, TimeUnit.SECONDS);
    }

    // ------------------------------------------------------------------------
    // SCHEDULE (오늘자) — RouteID는 Long 가정
    // ------------------------------------------------------------------------
    public void loadTodaySchedule(){
        SessionStore s = SessionStore.getInstance();
        final Long org = s.getOrgId();
        final Long driver = s.getDriverId();
        if (org == null || driver == null) {
            Toasts.throttled(getApplication(), "세션 누락");
            return;
        }
        attemptSchedule(org, driver, 1, 0);
    }

    private void attemptSchedule(Long org, Long driver, int attempt, long lastToastSec){
        Call<List<ScheduleItem>> call = api.schedule(org, driver);
        call.enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(Call<List<ScheduleItem>> c, Response<List<ScheduleItem>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    List<ScheduleItem> list = resp.body();
                    scheduleLive.postValue(list);

                    // 현재 시각 이후의 첫 코스 → 세션 주입
                    io.execute(() -> pickAndSetCurrentCourse(list));
                } else {
                    retryScheduleMaybe(org, driver, attempt, lastToastSec);
                }
            }
            @Override
            public void onFailure(Call<List<ScheduleItem>> c, Throwable t) {
                retryScheduleMaybe(org, driver, attempt, lastToastSec);
            }
        });
    }

    /** RouteID가 Long 전제: ScheduleItem#getRouteID() → Long */
    private void pickAndSetCurrentCourse(List<ScheduleItem> list){
        if (list == null || list.isEmpty()) return;

        LocalTime now = LocalTime.now(); // Asia/Seoul 전제(앱 공통)

        for (ScheduleItem it : list) {
            LocalTime hm = TimeUtil.parseHm(it.getDepartTime()); // "HH:mm"
            if (hm == null) continue;
            if (hm.isBefore(now)) continue; // 과거는 무시

            Long rid = it.getRouteID(); // ← Long
            if (rid == null || rid <= 0) continue;
//
//            String courseId = null;
//            try { courseId = it.getCourseId(); } catch (Throwable ignore) { /* optional */ }

            SessionStore.getInstance().setCurrentCourse("101", rid, it.getDepartTime());
            return; // 오름차순 보장 → 첫 코스만 선택
        }
        // 남은 코스 없음 → 세션 미설정 (WAITING에서 "당일 종료" 안내)
    }

    private void retryScheduleMaybe(Long org, Long driver, int attempt, long lastToastSec){
        if (attempt >= RETRY_MAX) {
            Toasts.throttled(getApplication(), "서버 연결 불가");
            return;
        }
        int nextAttempt = attempt + 1;
        long nextDelay = RETRY_DELAY_SEC;

        long nextToastSec = lastToastSec + RETRY_DELAY_SEC;
        if (nextToastSec % TOAST_INTERVAL_SEC == 0) {
            Toasts.throttled(getApplication(),
                    "스케줄 재시도 중... (" + nextAttempt + "/" + 30 + ")");
        }

        sched.schedule(() ->
                        attemptSchedule(org, driver, nextAttempt, nextToastSec),
                nextDelay, TimeUnit.SECONDS);
    }

    // ------------------------------------------------------------------------
    @Override
    protected void onCleared() {
        super.onCleared();
        io.shutdownNow();
        sched.shutdownNow();
    }
}
