package com.shuttleroid.vehicle.domain;

import android.location.Location;
import com.shuttleroid.vehicle.location.Geomath;
import com.shuttleroid.vehicle.data.entity.BusStop;

// 순수 판정 엔진: 반경 값은 BusStop에 있으면 우선 사용, 없으면 기본(500/30/50)
public class StopEngine {

    public enum Event { NONE, APPROACH, ARRIVE, DEPART }

    public static class Radii {
        public final double approachM; public final double arrivalM; public final double leaveM;
        public Radii(double a, double b, double c){ approachM=a; arrivalM=b; leaveM=c; }
        public static Radii defaults(){ return new Radii(500,30,50); }
    }

    public static Radii radiiOf(BusStop s){
        // TODO: BusStop에 approach/arrival/leave 필드(String/Double) 있으면 파싱
        try {
            Double a = s.getApproach() != null ? Double.parseDouble(s.getApproach()) : null;
            Double b = s.getArrival()  != null ? Double.parseDouble(s.getArrival())  : null;
            Double c = s.getLeave()    != null ? Double.parseDouble(s.getLeave())    : null;
            if (a!=null && b!=null && c!=null) return new Radii(a,b,c);
        } catch (Throwable ignore){}
        return Radii.defaults();
    }

    public static Event judge(BusStop stop, Location loc, boolean wasInside){
        Radii r = radiiOf(stop);
        double d = Geomath.distanceMeters(
                loc.getLatitude(), loc.getLongitude(),
                stop.getLatitude(), stop.getLongitude());

        if (d <= r.arrivalM) {
            return Event.ARRIVE;
        } else if (d <= r.approachM) {
            return Event.APPROACH;
        } else if (d >= r.leaveM && wasInside) {
            return Event.DEPART;
        }
        return Event.NONE;
    }
}
