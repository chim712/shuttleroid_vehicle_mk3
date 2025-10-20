package com.shuttleroid.vehicle.data.mapper;

import com.shuttleroid.vehicle.data.dto.BusStopDto;
import com.shuttleroid.vehicle.data.dto.CourseDto;
import com.shuttleroid.vehicle.data.dto.DataInfoDto;
import com.shuttleroid.vehicle.data.dto.RouteDto;
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.entity.Course;
import com.shuttleroid.vehicle.data.entity.Route;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IntegratedMapper {
    // One Object Mapper
    public static BusStop fromDto(BusStopDto dto) {
        if (dto == null) return null;
        return new BusStop(
                dto.stopID,
                dto.stopName,
                dto.latitude,
                dto.longitude,
                dto.approach,
                dto.arrival,
                dto.leave,
                dto.announce
        );
    }

//    public static Route fromDto(RouteDto dto) {
//        if (dto == null) return null;
//        return new Route(
//                dto.routeID,
//                dto.routeName,
//                dto.spendTime,
//                dto.stopIds
//        );
//    }

    public static Course fromDto(CourseDto dto){
        if(dto==null) return null;

        LocalTime departure = null;
        if(dto.departureTime!=null && !dto.departureTime.isBlank()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            departure = LocalTime.parse(dto.departureTime, formatter);
        }
        return new Course(dto.routeID, departure);
    }

    // List Mapper
    public static List<BusStop> fromDtoBusStops(List<BusStopDto> dtos) {
        if (dtos == null) return null;
        return Collections.unmodifiableList(
                dtos.stream()
                        .map(IntegratedMapper::fromDto)
                        .collect(Collectors.toList())
        );
    }

//    public static List<Route> fromDtoRoutes(List<RouteDto> dtos) {
//        if (dtos == null) return null;
//        return Collections.unmodifiableList(
//                dtos.stream()
//                        .map(IntegratedMapper::fromDto)
//                        .collect(Collectors.toList())
//        );
//    }

    public static List<Course> fromDtoCourses(List<CourseDto> dtos) {
        if (dtos == null) return null;
        return Collections.unmodifiableList(
                dtos.stream()
                        .map(IntegratedMapper::fromDto)
                        .collect(Collectors.toList())
        );
    }

    // Integrated Mapper
    public static class DataBundle {
        public final List<BusStop> stops;
        public final List<Route> routes;

        public DataBundle(List<BusStop> stops, List<Route> routes) {
            this.stops = stops;
            this.routes = routes;
        }
    }

//    public static DataBundle fromDto(DataInfoDto dto) {
//        if (dto == null) return null;
//        List<BusStop> stops = fromDtoBusStops(dto.stopList);
//        List<Route> routes = fromDtoRoutes(dto.routeList);
//        return new DataBundle(stops, routes);
//    }
}
