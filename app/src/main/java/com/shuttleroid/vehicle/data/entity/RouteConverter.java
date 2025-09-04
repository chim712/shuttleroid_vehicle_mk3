package com.shuttleroid.vehicle.data.entity;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RouteConverter {
    private static final Gson gson = new Gson();
    private static final Type LONG_LIST = new TypeToken<List<Long>>(){}.getType();

    @TypeConverter
    public static String fromLongList(List<Long> ids){
        if(ids==null) return "[]";
        return gson.toJson(ids, LONG_LIST);
    }
    @TypeConverter
    public static List<Long> toLongList(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        return gson.fromJson(json, LONG_LIST);
    }
}
