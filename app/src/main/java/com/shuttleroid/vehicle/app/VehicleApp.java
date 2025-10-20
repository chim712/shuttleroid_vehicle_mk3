package com.shuttleroid.vehicle.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

// app/VehicleApp.java
public class VehicleApp extends Application {
    public static final String CHANNEL_OPS = "ops_channel";

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_OPS,
                    "운행 상태",
                    NotificationManager.IMPORTANCE_LOW // 위치 서비스는 LOW 권장
            );
            ch.setDescription("운행 중 위치 추적 및 안내");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }
}
