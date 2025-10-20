// app/src/main/java/com/shuttleroid/vehicle/network/dto/UpdateSnapshot.java
package com.shuttleroid.vehicle.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** /update (data.json) 실제 스키마 매핑 */
public class UpdateSnapshot {
    @SerializedName("organizationId") public Long organizationId; // 선택
    @SerializedName("updateVersion")  public Long updateVersion;
    @SerializedName("stopList")       public List<BusStopDto> stopList;
    @SerializedName("routeList")      public List<RouteDto> routeList;

    public static class BusStopDto {
        @SerializedName("stopID")    public String stopID;    // "1010001"
        @SerializedName("stopName")  public String stopName;
        @SerializedName("latitude")  public String latitude;  // "36.77279"
        @SerializedName("longitude") public String longitude; // "126.93384"
        @SerializedName("approach")  public String approach;  // "150"
        @SerializedName("arrival")   public String arrival;   // "20"
        @SerializedName("leave")     public String leave;     // "30"
    }

    public static class RouteDto {
        @SerializedName("routeID")   public String routeID;   // "102"
        @SerializedName("routeName") public String routeName;
        @SerializedName("spendTime") public String spendTime; // "8"
        @SerializedName("stopIds")   public List<Long> stopIds; // [1010001, ...] (숫자)
    }
}
