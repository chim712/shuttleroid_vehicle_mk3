package com.shuttleroid.vehicle.domain;

import com.shuttleroid.vehicle.util.TimeUtil;
import com.shuttleroid.vehicle.network.dto.ScheduleItem;

import java.time.LocalTime;
import java.util.*;

public class CourseEngine {

    public static List<ScheduleItem> purgePast(List<ScheduleItem> all){
        List<ScheduleItem> res = new ArrayList<>();
        if (all==null) return res;
        for (ScheduleItem s : all){
            if (!TimeUtil.isPastToday(s.departureTime)) res.add(s);
        }
        return res;
    }

    public static Optional<ScheduleItem> next(List<ScheduleItem> queue){
        if (queue == null || queue.isEmpty()) return Optional.empty();
        return Optional.of(queue.get(0));
    }

    public static List<ScheduleItem> removeHead(List<ScheduleItem> queue){
        if (queue==null || queue.isEmpty()) return queue;
        List<ScheduleItem> copy = new ArrayList<>(queue);
        copy.remove(0);
        return copy;
    }

    public static LocalTime departTimeOf(ScheduleItem item){
        return TimeUtil.parseHm(item.departureTime);
    }
}
