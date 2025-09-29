package com.shuttleroid.vehicle.service;

import com.shuttleroid.vehicle.data.dto.CourseDto;
import com.shuttleroid.vehicle.data.dto.DataInfoDto;
import com.shuttleroid.vehicle.data.dto.LocationReportDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 서버 API 요청 정의 인터페이스
 * - Retrofit이 이 인터페이스를 구현체로 변환해줌
 */
public interface SyncService {
    // update check flag
    @GET("update/check")
    Call<Boolean> getUpdateCheck(@Query("orgID") long orgID, @Query("version") long ver);


    // data.json 파일을 GET 방식으로 가져오기
    @GET("update")   // <-- 실제 서버 경로 맞게 수정 필요
    Call<DataInfoDto> getUpdateData(@Query("orgID") long orgID);
    // => example.com/update?orgID=123456

    @GET("schedule")
    Call<List<CourseDto>> getScheduleData(@Query("driverID") long driverID);

    //TODO: @POST("location")       : 차량 위치 전송
    // Temp Post Logic
    @POST("ingest")
    Call<Void> sendLocationReport(@Body LocationReportDto report);


    //TODO: @POST("route/start")    : 노선 운행 시작
    //TODO: @POST("route/end")      : 노선 운행 종료
    //TODO: @POST("vehicle/start")  : 차량 일일 일정 시작
    //TODO: @POST("vehicle/end")    : 차량 일일 일정 종료
}

