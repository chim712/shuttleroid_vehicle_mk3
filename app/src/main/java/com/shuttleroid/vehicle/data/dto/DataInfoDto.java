package com.shuttleroid.vehicle.data.dto;

import java.util.List;

// Original DB Data from Server
public class DataInfoDto {
    public int organizationId;
    public long updateVersion;
    public List<BusStopDto> stopList;
    public List<RouteDto> routeList;
}
