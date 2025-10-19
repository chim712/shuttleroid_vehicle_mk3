package com.shuttleroid.vehicle.network.dto;

/** GET /schedule 응답의 원소 (당일)
 *  - routeID(Long)
 *  - departureTime("HH:mm")
 */
public class ScheduleItem {
    public Long routeID;
    public String departureTime; // "HH:mm"
}
