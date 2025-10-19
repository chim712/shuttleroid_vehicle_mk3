package com.shuttleroid.vehicle.app;

import com.shuttleroid.vehicle.BuildConfig;

public class AppConfig {
    private AppConfig(){}

    public static String baseUrl(){ return BuildConfig.BASE_URL; }

    public static final int RETRY_MAX = 30;
    public static final long RETRY_DELAY_MS = 5_000L;
}
