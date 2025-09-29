package com.shuttleroid.vehicle.domain;

public class DriveManager {



    //Singleton Constructor
    private static volatile DriveManager instance;
    private DriveManager(){
        // empty
    }
    public static DriveManager getInstance() {
        if (instance == null)
            instance = new DriveManager();
        return instance;
    }
}
