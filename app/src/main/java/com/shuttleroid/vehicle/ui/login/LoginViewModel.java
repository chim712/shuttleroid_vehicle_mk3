package com.shuttleroid.vehicle.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.network.api.SyncService;
import com.shuttleroid.vehicle.network.client.RetrofitProvider;
import com.shuttleroid.vehicle.network.dto.OrgCheckReq;
import com.shuttleroid.vehicle.network.dto.OrgCheckRes;
import com.shuttleroid.vehicle.network.dto.LoginReq;
import com.shuttleroid.vehicle.network.dto.LoginRes;
import com.shuttleroid.vehicle.network.dto.LogoutReq;
import com.shuttleroid.vehicle.util.Toasts;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final SyncService api;
    public final MutableLiveData<String> orgName = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loginOk = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application app) {
        super(app);
        api = RetrofitProvider.get().create(SyncService.class);
    }

    public void checkOrg(Long orgId){
        OrgCheckReq req = new OrgCheckReq();
        req.orgID = orgId;

        Log.wtf("Login","Req, orgID: "+req.orgID);

        api.orgCheck(req).enqueue(new Callback<OrgCheckRes>() {
            @Override public void onResponse(Call<OrgCheckRes> call, Response<OrgCheckRes> resp) {
                if (resp.isSuccessful() && resp.body()!=null){
                    orgName.postValue(resp.body().orgName);
                } else {
                    Toasts.throttled(getApplication(),"기관 확인 실패");
                    orgName.postValue(null);
                }
            }
            @Override public void onFailure(Call<OrgCheckRes> call, Throwable t) {
                Toasts.throttled(getApplication(),"서버 연결 불가");
                orgName.postValue(null);
            }
        });
    }

    public void login(Long orgId, Long driverId, String pw){
        LoginReq req = new LoginReq();
        req.orgID = orgId; req.driverID = driverId; req.password = pw;

        api.login(req).enqueue(new Callback<LoginRes>() {
            @Override public void onResponse(Call<LoginRes> call, Response<LoginRes> resp) {
                if (resp.code()==403){
                    Toasts.throttled(getApplication(),"아이디 또는 비밀번호가 올바르지 않습니다");
                    loginOk.postValue(false);
                    return;
                }
                if (resp.isSuccessful() && resp.body()!=null){
                    SessionStore s = SessionStore.getInstance();
                    s.setOrgId(orgId);
                    s.setDriverId(driverId);
                    s.setVehicleId(resp.body().vehicleID);
                    loginOk.postValue(true);
                } else {
                    Toasts.throttled(getApplication(),"서버 연결 불가");
                    loginOk.postValue(false);
                }
            }
            @Override public void onFailure(Call<LoginRes> call, Throwable t) {
                Toasts.throttled(getApplication(),"서버 연결 불가");
                loginOk.postValue(false);
            }
        });
    }

    public void logout(){
        SessionStore s = SessionStore.getInstance();
        Long orgId = s.getOrgId();
        Long driverId = s.getDriverId();
        if (orgId==null || driverId==null) return;

        LogoutReq req = new LogoutReq();
        req.orgID = orgId; req.driverID = driverId;

        api.logout(req).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                if (resp.isSuccessful()) s.clear();
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { }
        });
    }
}
