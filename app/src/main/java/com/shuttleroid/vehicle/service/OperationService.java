package com.shuttleroid.vehicle.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.app.VehicleApp;
import com.shuttleroid.vehicle.ui.main.MainActivity;

public class OperationService extends Service {

    public static final int NOTI_ID = 2001;

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTI_ID, buildNotification("운행 준비 중"));
        return START_STICKY;
    }

    private Notification buildNotification(String content){
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // ...
        return new NotificationCompat.Builder(this, VehicleApp.CHANNEL_OPS)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // ← 내장 아이콘로 교체
                .setContentTitle("Shuttle-roid")
                .setContentText(content)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

    }

    public void updateContent(String text){
        Notification n = buildNotification(text);
        startForeground(NOTI_ID, n);
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
