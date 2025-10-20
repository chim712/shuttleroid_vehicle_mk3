// app/src/main/java/com/shuttleroid/vehicle/data/mapper/UpdateSnapshotMapper.java
package com.shuttleroid.vehicle.data.mapper;

import com.shuttleroid.vehicle.data.entity.*;
import com.shuttleroid.vehicle.network.dto.UpdateSnapshot;

import java.util.ArrayList;
import java.util.List;

public final class UpdateSnapshotMapper {
    private UpdateSnapshotMapper(){}

    public static List<BusStop> toStops(UpdateSnapshot s){
        List<BusStop> out = new ArrayList<>();
        if (s==null || s.stopList==null) return out;
        for (UpdateSnapshot.BusStopDto d : s.stopList){
            BusStop b = new BusStop();
            b.stopID = parseLong(d.stopID);
            b.stopName = nz(d.stopName);
            b.latitude = parseDouble(d.latitude);
            b.longitude= parseDouble(d.longitude);
            b.approach = parseInt(d.approach);
            b.arrival  = parseInt(d.arrival);
            b.depart    = parseInt(d.leave);
            out.add(b);
        }
        return out;
    }

    public static List<Route> toRoutes(UpdateSnapshot s){
        List<Route> out = new ArrayList<>();
        if (s==null || s.routeList==null) return out;
        for (UpdateSnapshot.RouteDto d : s.routeList){
            Route r = new Route();
            r.routeID = parseLong(d.routeID);
            r.routeName = nz(d.routeName);
            r.spendTime = d.spendTime;
            out.add(r);
        }
        return out;
    }

    public static List<RouteStopCrossRef> toRefs(UpdateSnapshot s){
        List<RouteStopCrossRef> out = new ArrayList<>();
        if (s==null || s.routeList==null) return out;
        for (UpdateSnapshot.RouteDto d : s.routeList){
            long routeId = parseLong(d.routeID);
            if (d.stopIds == null) continue;
            for (int i=0;i<d.stopIds.size();i++){
                RouteStopCrossRef ref = new RouteStopCrossRef();
                ref.routeID = routeId;
                ref.stopID = d.stopIds.get(i);
                ref.seq = i;
                out.add(ref);
            }
        }
        return out;
    }

    private static String nz(String s){ return s==null ? "" : s; }
    private static long parseLong(String s){ try { return Long.parseLong(s); } catch(Exception e){ return 0L; } }
    private static double parseDouble(String s){ try { return Double.parseDouble(s); } catch(Exception e){ return 0.0; } }
    private static int parseInt(String s){ try { return Integer.parseInt(s); } catch(Exception e){ return 0; } }
}
