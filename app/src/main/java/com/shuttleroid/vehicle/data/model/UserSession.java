package com.shuttleroid.vehicle.data.model;

public class UserSession {
    static Long userID;
    static String userName;
    static Long vehicleNo;

    public static Long getUserID() {
        return userID;
    }

    public static void setUserID(Long userID) {
        UserSession.userID = userID;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        UserSession.userName = userName;
    }

    public static Long getVehicleNo() {
        return vehicleNo;
    }

    public static void setVehicleNo(Long vehicleNo) {
        UserSession.vehicleNo = vehicleNo;
    }
}
