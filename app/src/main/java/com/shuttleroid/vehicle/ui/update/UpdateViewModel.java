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
import com.shuttleroid.vehicle.util.Toasts;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateViewModel extends AndroidViewModel {

    // ====== Public state ======
    public final MutableLiveData<Boolean> updateDone = new MutableLiveData<>(false);
    public final MutableLiveData<List<ScheduleItem>> scheduleLive = new MutableLiveData<>();

    // ====== Deps ======
    private final SyncService api;
    private final IntegratedRepository repo;

    // ====== Threading ======
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();

    // ====== Retry policy ======
    private static final int RETRY_MAX = 30;          // 최대 30회
    private static final long RETRY_DELAY_SEC = 5L;   // 5초 간격
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
                // 204: 최신
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
                            // DB 오류는 재시도 의미 없음 → 실패 처리
                            updateDone.postValue(false);
                            Toasts.throttled(getApplication(), "DB 저장 실패: " + t.getMessage());
                        }
                    });
                } else {
                    // 400/500 포함 비성공 응답 → 재시도
                    retryUpdateMaybe(org, dataVer, attempt, lastToastSec);
                }
            }
            @Override
            public void onFailure(Call<UpdateSnapshot> c, Throwable t) {
                // 통신 실패 → 재시도
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
        long nextAttempt = attempt + 1;
        long nextDelay = RETRY_DELAY_SEC;

        // 10초마다 한 번만 토스트 (과다 표시 방지)
        long nextToastSec = lastToastSec + RETRY_DELAY_SEC;
        if (nextToastSec % TOAST_INTERVAL_SEC == 0) {
            Toasts.throttled(getApplication(),
                    "업데이트 재시도 중... (" + nextAttempt + "/" + RETRY_MAX + ")");
        }

        sched.schedule(() ->
                        attemptUpdate(org, dataVer, (int) nextAttempt, nextToastSec),
                nextDelay, TimeUnit.SECONDS);
    }

    // ------------------------------------------------------------------------
    // SCHEDULE (오늘자)
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
                    scheduleLive.postValue(resp.body());
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
                    "스케줄 재시도 중... (" + nextAttempt + "/" + RETRY_MAX + ")");
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
