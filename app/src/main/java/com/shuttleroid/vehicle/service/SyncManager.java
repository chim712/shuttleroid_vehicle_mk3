package com.shuttleroid.vehicle.service;

import android.content.Context;
import android.util.Log;

import com.shuttleroid.vehicle.data.dto.DataInfoDto;
import com.shuttleroid.vehicle.data.dto.LocationReportDto;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 서버와의 데이터 동기화를 전담하는 클래스
 * - Retrofit으로 서버에 요청을 보내고
 * - 응답받은 DTO를 Repository에 전달하여 DB를 갱신함
 */
public class SyncManager {
    private static volatile SyncManager instance;
    private final SyncService syncService;
    private final IntegratedRepository repository;

    // Manage Logic ==================================================

    public void updateRequest(long orgID) {
        Log.d("SyncManager", "updateRequest");
        syncService.getUpdateData(orgID).enqueue(new Callback<DataInfoDto>() {
            @Override
            public void onResponse(Call<DataInfoDto> call, Response<DataInfoDto> response) {
                Log.d("SyncManager","onResponse");
                if (response.isSuccessful() && response.body() != null) {
                    repository.replaceAllFromDto(response.body());
                }
            }

            @Override
            public void onFailure(Call<DataInfoDto> call, Throwable t) {
                //Todo: Failure process

                Log.e("Sync_Update", "onFailure: " + t.getMessage());
            }
        });
    }

    public void sendLocationReport(String vehicleNo, String route, String stopLocation){
        syncService.sendLocationReport(new LocationReportDto(vehicleNo, route, stopLocation)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response){
                Log.d("Sync_Location", "Send Location Successful");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t){

                Log.e("Sync_Location", "onFailure: " + t.getMessage());
            }
        });
    }









    // Singleton Constructor =======================================
    private SyncManager(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://host.imagine.io.kr/") // TODO: 실제 서버 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        syncService = retrofit.create(SyncService.class);
        repository = IntegratedRepository.getInstance(context);
    }

    // 싱글턴 인스턴스 획득
    public static SyncManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager(context);
                }
            }
        }
        return instance;
    }
}
