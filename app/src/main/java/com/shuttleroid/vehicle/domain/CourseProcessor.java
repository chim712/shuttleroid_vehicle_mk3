package com.shuttleroid.vehicle.domain;

// Schedule Manage + Change Route

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Course;
import com.shuttleroid.vehicle.data.entity.Route;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;
import com.shuttleroid.vehicle.service.AnnounceManager;
import com.shuttleroid.vehicle.ui.operation.OperationViewModel;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Static Util Class
public class CourseProcessor {
    // Daily Schedule List
    static Queue<Course> schedule = new LinkedList<>();
    static IntegratedRepository repository = IntegratedRepository.getInstance(null);


    // Logic Data Field =============================================================
    static final MutableLiveData<String> currentRouteLive = new MutableLiveData<>();
    static Route currentRoute;
    static LocalTime departureTime;
    static int stopIndex;


    public static LiveData<String> getCurrentRouteLive(){      //for MVVM
        return currentRouteLive;
    }
    public static Route getCurrentRoute(){
        return currentRoute;
    }


    // Daily Schedule Update Method =============================================================
    // Course 리스트를 받아오면 schedule Queue에 삽입
    // 호출 순서: addCourses { clear -> add(for loop) }
    public static void addCourse(Course c){
        schedule.add(c);
    }
    public static void addCourses(List<Course> list){
        clearSchedule();
        for(Course c:list){
            addCourse(c);
        }

        // insert first route
        changeRoute();

        // for debug
//        for(Course c: schedule)
//            Log.d("Course_Test", c.toString());
    }
    public static void clearSchedule(){
        schedule.clear();
    }



    // Drive Method =============================================================
    public static void nextStop(){
        stopIndex++;
        repository.getBusStopAsync(currentRoute.stopIds.get(stopIndex),
                busStop -> {StopProcessor.updateCurrentStop(busStop);});
        repository.getBusStopAsync(currentRoute.stopIds.get(stopIndex+1),
                busStop -> {StopProcessor.updateNextStop(busStop);});
    }
    public static boolean isTerminal(){
        return false;
    }

    public static void changeRoute(){
        // if end of route, request next route
        if(!schedule.isEmpty()){
            Course c = schedule.poll();
            repository.getRouteAsync(c.getRouteID(), route -> {
                currentRoute = route;
                currentRouteLive.postValue(currentRoute.routeName);

                //init StopProcessor
                repository.getBusStopAsync(currentRoute.stopIds.get(0),
                        busStop -> {StopProcessor.updateCurrentStop(busStop);});
                repository.getBusStopAsync(currentRoute.stopIds.get(1),
                        busStop -> {StopProcessor.updateNextStop(busStop);});
            });
            departureTime = c.getDepartureTime();
            stopIndex = 0;


        } else {
            // TODO : App Close (daily schedule ended)
        }

    }


    // Route Terminated
    public static void handleRouteEnd(Context context){

    }
}
