package com.shuttleroid.vehicle.service;

import com.shuttleroid.vehicle.data.dto.DataInfoDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 서버 API 요청 정의 인터페이스
 * - Retrofit이 이 인터페이스를 구현체로 변환해줌
 */
public interface ApiService {
    // data.json 파일을 GET 방식으로 가져오기
    @GET("update")   // <-- 실제 서버 경로 맞게 수정 필요
    Call<DataInfoDto> getUpdateData(@Query("orgID") long orgID);
    // => example.com/update?orgID=123456
}

