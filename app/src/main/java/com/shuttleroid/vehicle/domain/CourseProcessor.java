package com.shuttleroid.vehicle.domain;

// Schedule Manage + Change Route

import android.content.Context;

import com.shuttleroid.vehicle.data.entity.Course;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Static Util Class
public class CourseProcessor {
    // Daily Schedule List
    static Queue<Course> schedule = new LinkedList<>();


    // Daily Schedule Update Method
    public static void addCourse(Course c){
        schedule.add(c);
    }
    public static void addCourses(List<Course> list){
        for(Course c:list){
            addCourse(c);
        }
    }
    public static void clearSchedule(){
        schedule.clear();
    }

    // Route Terminated
    public static void handleRouteEnd(Context context){

    }
}
