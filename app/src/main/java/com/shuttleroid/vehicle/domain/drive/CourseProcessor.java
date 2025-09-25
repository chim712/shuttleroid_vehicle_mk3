package com.shuttleroid.vehicle.domain.drive;

// Schedule Manage + Change Route

import android.content.Context;

import com.shuttleroid.vehicle.data.entity.Course;

import java.util.LinkedList;
import java.util.List;

// Static Util Class
public class CourseProcessor {
    // Daily Schedule List
    static List<Course> schedule = new LinkedList<>();


    public static void addCourse(Course c){
        schedule.add(c);
    }
    public static void clearSchedule(){
        schedule.clear();
    }

    public static void handleRouteEnd(Context context){

    }
}
