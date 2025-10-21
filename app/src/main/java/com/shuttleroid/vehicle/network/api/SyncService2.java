package com.shuttleroid.vehicle.network.api;

import com.shuttleroid.vehicle.network.dto.LocationEvent;
import com.shuttleroid.vehicle.network.dto.LoginReq;
import com.shuttleroid.vehicle.network.dto.LoginRes;
import com.shuttleroid.vehicle.network.dto.LogoutReq;
import com.shuttleroid.vehicle.network.dto.OrgCheckReq;
import com.shuttleroid.vehicle.network.dto.OrgCheckRes;
import com.shuttleroid.vehicle.network.dto.RouteReport;
import com.shuttleroid.vehicle.network.dto.ScheduleItem;
import com.shuttleroid.vehicle.network.dto.UpdateSnapshot;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 서버 API 계약 (MVP 스펙 확정본)
 *
 * - 기관 확인:  POST /org/check           req:{orgID}                  res:{orgName}
 * - 로그인:     POST /auth/login          req:{orgID,driverID,password} res:{vehicleID}
 * - 로그아웃:   POST /auth/logout         req:{orgID,driverID}         res:200 OK
 * - 업데이트:   GET  /update?orgID&dataVer → 204=최신 / 200=data.json
 * - 스케줄:     GET  /schedule?orgID&driverID → 오늘자 [{routeID,departureTime}]
 * - 위치이벤트: POST /location            req:{vehicleID,stopID,status}
 * - 노선 시작:  POST /route/start         RouteReport
 * - 노선 종료:  POST /route/terminate     RouteReport
 */
public interface SyncService2 {

    // 1) 기관 확인
    @POST("/org/check")
    Call<OrgCheckRes> orgCheck(@Body OrgCheckReq req);

    // 2) 로그인/로그아웃
    @POST("/auth/login")
    Call<LoginRes> login(@Body LoginReq req);

    @POST("/auth/logout")
    Call<ResponseBody> logout(@Body LogoutReq req);

    // 3) 업데이트(204=최신)
    @GET("/update")
    Call<UpdateSnapshot> update(@Query("orgID") Long orgId, @Query("dataVer") Long dataVer);

    // 4) 스케줄(당일)
    @GET("/schedule")
    Call<List<ScheduleItem>> schedule(@Query("orgID") Long orgId, @Query("driverID") Long driverId);

    // 5) 위치 이벤트 단일 엔드포인트
    @POST("/location")
    Call<ResponseBody> postLocation(@Body LocationEvent ev);

    // 6) 노선 시작/종료
    @POST("/route/start")
    Call<ResponseBody> routeStart(@Body RouteReport req);

    @POST("/route/terminate")
    Call<ResponseBody> routeTerminate(@Body RouteReport req);
}
