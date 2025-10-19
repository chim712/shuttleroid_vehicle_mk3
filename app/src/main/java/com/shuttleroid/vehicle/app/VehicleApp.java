package com.shuttleroid.vehicle.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class VehicleApp extends Application {
    public static final String CHANNEL_OPS = "ops_channel";

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_OPS, "운행 서비스", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("운행 중 상태 알림");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
}
