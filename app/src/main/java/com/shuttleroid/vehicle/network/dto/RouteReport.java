package com.shuttleroid.vehicle.network.dto;

/** POST /route/start, /route/terminate
 *  flag: true=출발, false=도착
 */
public class RouteReport {
    public String courseID;     // 서버 제공(파일명 동일)
    public Long routeID;
    public String departTime;   // "HH:mm"
    public String vehicleID;
    public boolean flag;

    public static RouteReport of(String courseID, Long routeID, String departTime,
                                 String vehicleID, boolean flag){
        RouteReport r = new RouteReport();
        r.courseID = courseID;
        r.routeID = routeID;
        r.departTime = departTime;
        r.vehicleID = vehicleID;
        r.flag = flag;
        return r;
    }
}
