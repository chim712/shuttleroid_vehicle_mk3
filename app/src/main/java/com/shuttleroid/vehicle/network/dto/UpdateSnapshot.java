package com.shuttleroid.vehicle.network.dto;

import java.util.List;

/**
 * GET /update (data.json) 매핑 DTO (예시 스키마)
 * - 서버 data.json 구조에 맞춰 필드명을 실제와 동일하게 맞추세요.
 * - 아래는 일반적인 형태 예시이며, 필요 시 필드/타입 변경하세요.
 */
public class UpdateSnapshot {
    public List<RouteDto> routes;
    public List<BusStopDto> busStops;
    public Long dataVer; // 선택: 서버가 내려줄 경우

    public static class RouteDto {
        public Long routeID;
        public String routeName;
        public String spendTime;      // 선택
        public List<Long> stopIds;    // 경유 정류장 ID 목록
    }

    public static class BusStopDto {
        public Long stopID;
        public String stopName;
        public double latitude;
        public double longitude;

        // 반경 파라미터: null일 수 있음(그 경우 기본 500/30/50 사용)
        public String approach; // "500"
        public String arrival;  // "30"
        public String leave;    // "50"
    }
}
