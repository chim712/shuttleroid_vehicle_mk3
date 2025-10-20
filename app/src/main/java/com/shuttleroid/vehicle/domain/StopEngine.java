package com.shuttleroid.vehicle.domain;

import android.location.Location;

import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.location.Geomath;

/**
 * 순수 판정 엔진
 * - 반경 우선순위: BusStop(>0 값 존재) → 기본값(500/30/50)
 * - 이벤트 방출 규칙:
 *   APPROACH:   거리 ≤ approachM
 *   ARRIVE:     거리 ≤ arrivalM
 *   DEPART:     (이전에 approach/arrival 안에 있었고) 거리 ≥ leaveM
 *
 * 권장 사용:
 *   StopEngine.Tracker tracker = new StopEngine.Tracker();
 *   Event ev = tracker.update(lat, lon, stop);
 *   if (ev == DEPART) tracker.reset(); // 다음 정류장으로 넘어갈 때 새 Tracker 사용도 OK
 */
public final class StopEngine {

    private StopEngine() {}

    public enum Event { NONE, APPROACH, ARRIVE, DEPART }

    public static class Radii {
        public final int approachM;
        public final int arrivalM;
        public final int leaveM;
        public Radii(int a, int b, int c) { approachM = a; arrivalM = b; leaveM = c; }
        public static Radii defaults() { return new Radii(500, 30, 50); }
    }

    /** BusStop의 반경 값(>0) 우선 사용, 아니면 기본값 */
    public static Radii radiiOf(BusStop s) {
        int a = safe(s.getApproach());
        int b = safe(s.getArrival());
        int c = safe(s.getDepart());
        if (a > 0 && b > 0 && c > 0) return new Radii(a, b, c);
        return Radii.defaults();
    }

    private static int safe(Integer v) { return v == null ? 0 : v; }
    private static int safe(int v) { return v; }

    /** 순수 거리 계산 */
    public static double distanceMeters(double lat, double lon, BusStop stop) {
        return Geomath.distanceMeters(lat, lon, stop.getLatitude(), stop.getLongitude());
    }

    // ----------------------------------------------------------------------
    // 상태 추적 트래커 (권장)
    // ----------------------------------------------------------------------

    /**
     * 각 정류장별로 하나의 Tracker를 사용하세요.
     * - 내부 상태: insideApproach/insideArrival
     * - update() 호출마다 현재 거리로 이벤트를 결정합니다.
     * - Depart가 발생하면 보통 다음 정류장으로 넘어가기 때문에,
     *   다음 정류장에서는 새 Tracker를 쓰거나 reset()을 호출하세요.
     */
    public static final class Tracker {
        private boolean insideApproach = false;
        private boolean insideArrival = false;

        /** 업데이트 → 이벤트 반환 */
        public Event update(double lat, double lon, BusStop stop) {
            Radii r = radiiOf(stop);
            double d = distanceMeters(lat, lon, stop);

            // ARRIVE가 최우선
            if (d <= r.arrivalM) {
                insideArrival = true;
                insideApproach = true;
                return Event.ARRIVE;
            }

            // APPROACH (도달) – 한번 들어오면 insideApproach = true
            if (d <= r.approachM) {
                if (!insideApproach) insideApproach = true;
                insideArrival = false;
                return Event.APPROACH;
            }

            // DEPART – 이전에 안쪽(approach/arrival)에 있었고, leaveM 바깥으로 나갔을 때
            if ((insideApproach || insideArrival) && d >= r.leaveM) {
                insideApproach = false;
                insideArrival = false;
                return Event.DEPART;
            }

            return Event.NONE;
        }

        /** 다음 정류장으로 넘어갈 때 호출(또는 새 Tracker를 만들어도 OK) */
        public void reset() {
            insideApproach = false;
            insideArrival = false;
        }
    }

    // ----------------------------------------------------------------------
    // 하위호환 오버로드 (필요 시 유지)
    // ----------------------------------------------------------------------

    /** 기존 시그니처: wasInside를 전달받는 버전 (단순화) */
//    public static Event judge(BusStop stop, Location loc, boolean wasInside) {
//        Radii r = radiiOf(stop);
//        double d = Geomath.distanceMeters(
//                loc.getLatitude(), loc.getLongitude(),
//                stop.getLatitude(), stop.getLongitude()
//        );
//
//        if (d <= r.arrivalM) {
//            return Event.ARRIVE;
//        } else if (d <= r.approachM) {
//            return Event.APPROACH;
//        } else if (d >= r.leaveM && wasInside) {
//            return Event.DEPART;
//        }
//        return Event.NONE;
//    }

    /** 새 시그니처(무상태) – 단독 사용 시 시작 직후 바로 DEPART가 날 수 있으니 비추천 */
    public static Event judge(double lat, double lon, BusStop stop, Radii r) {
        double d = distanceMeters(lat, lon, stop);
        if (d <= r.arrivalM) return Event.ARRIVE;
        if (d <= r.approachM) return Event.APPROACH;
        // 무상태에서는 DEPART를 신뢰할 수 없음 → NONE 반환 (트래커 사용 권장)
        return Event.NONE;
    }
}
