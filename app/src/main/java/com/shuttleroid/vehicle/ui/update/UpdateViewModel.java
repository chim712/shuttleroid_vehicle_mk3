package com.shuttleroid.vehicle.ui.update;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.network.api.SyncService;
import com.shuttleroid.vehicle.network.client.RetrofitProvider;
import com.shuttleroid.vehicle.network.dto.UpdateSnapshot;
import com.shuttleroid.vehicle.network.dto.ScheduleItem;
import com.shuttleroid.vehicle.util.Toasts;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateViewModel extends AndroidViewModel {

    private final SyncService api;
    public final MutableLiveData<Boolean> updateDone = new MutableLiveData<>(false);
    public final MutableLiveData<List<ScheduleItem>> scheduleLive = new MutableLiveData<>();

    public UpdateViewModel(@NonNull Application app) {
        super(app);
        api = RetrofitProvider.get().create(SyncService.class);
    }

    public void doUpdate(long dataVer){
        SessionStore s = SessionStore.getInstance();
        Long org = s.getOrgId();
        if (org == null){
            Toasts.throttled(getApplication(), "orgID 없음");
            return;
        }
        api.update(org, dataVer).enqueue(new Callback<UpdateSnapshot>() {
            @Override public void onResponse(Call<UpdateSnapshot> call, Response<UpdateSnapshot> resp) {
                if (resp.code()==204){
                    Toasts.throttled(getApplication(),"최신버전입니다");
                    updateDone.postValue(true);
                    return;
                }
                if (resp.isSuccessful() && resp.body()!=null){
                    UpdateSnapshot snap = resp.body();
                    // TODO: snap.routes / snap.busStops → Room replaceAll(...)
                    updateDone.postValue(true);
                } else {
                    Toasts.throttled(getApplication(),"서버 연결 불가");
                    updateDone.postValue(false);
                }
            }
            @Override public void onFailure(Call<UpdateSnapshot> call, Throwable t) {
                Toasts.throttled(getApplication(),"서버 연결 불가");
                updateDone.postValue(false);
            }
        });
    }

    public void loadTodaySchedule(){
        SessionStore s = SessionStore.getInstance();
        Long org = s.getOrgId();
        Long driver = s.getDriverId();
        if (org==null || driver==null){
            Toasts.throttled(getApplication(),"세션 누락");
            return;
        }
        api.schedule(org, driver).enqueue(new Callback<List<ScheduleItem>>() {
            @Override public void onResponse(Call<List<ScheduleItem>> call, Response<List<ScheduleItem>> resp) {
                if (resp.isSuccessful() && resp.body()!=null){
                    scheduleLive.postValue(resp.body());
                } else {
                    Toasts.throttled(getApplication(),"서버 연결 불가");
                }
            }
            @Override public void onFailure(Call<List<ScheduleItem>> call, Throwable t) {
                Toasts.throttled(getApplication(),"서버 연결 불가");
            }
        });
    }
}
