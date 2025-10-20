package com.shuttleroid.vehicle.domain;

import android.location.Location;
import com.shuttleroid.vehicle.location.Geomath;
import com.shuttleroid.vehicle.data.entity.BusStop;

// 순수 판정 엔진: 반경 값은 BusStop에 있으면 우선 사용, 없으면 기본(500/30/50)
public class StopEngine {

    public enum Event { NONE, APPROACH, ARRIVE, DEPART }

    public static class Radii {
        public final int approachM;
        public final int arrivalM;
        public final int leaveM;

        public Radii(int a, int b, int c) {
            approachM = a;
            arrivalM = b;
            leaveM = c;
        }

        public static Radii defaults() {
            return new Radii(500, 30, 50);
        }
    }

    public static Radii radiiOf(BusStop s) {
        int a = s.getApproach();
        int b = s.getArrival();
        int c = s.getDepart();

        if (a > 0 && b > 0 && c > 0)
            return new Radii(a, b, c);

        return Radii.defaults();
    }

    public static Event judge(BusStop stop, Location loc, boolean wasInside) {
        Radii r = radiiOf(stop);
        double d = Geomath.distanceMeters(
                loc.getLatitude(), loc.getLongitude(),
                stop.getLatitude(), stop.getLongitude()
        );

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
