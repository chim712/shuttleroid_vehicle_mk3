package com.shuttleroid.vehicle.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static ZonedDateTime nowKst() {
        return ZonedDateTime.now(KST);
    }
    public static LocalTime parseHm(String s){
        return LocalTime.parse(s, HM);
    }
    public static String formatHm(LocalTime t){
        return t.format(HM);
    }
    public static boolean isPastToday(String hhmm){
        LocalTime t = parseHm(hhmm);
        return t.isBefore(nowKst().toLocalTime());
    }
}
