package com.shuttleroid.vehicle.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.app.VehicleApp;
import com.shuttleroid.vehicle.ui.main.MainActivity;

public class OperationService extends Service {

    public static final int NOTI_ID = 2001;

    @Override public void onCreate() {
        super.onCreate();
        // 채널이 없으면 여기서도 방어적으로 생성(중복 생성 안전)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(VehicleApp.CHANNEL_OPS) == null) {
                NotificationChannel ch = new NotificationChannel(
                        VehicleApp.CHANNEL_OPS, "운행 상태", NotificationManager.IMPORTANCE_LOW);
                nm.createNotificationChannel(ch);
            }
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTI_ID, buildNotification("운행 대기 중"));
        // TODO: 이후 위치 업데이트/상태에 따라 알림 내용 update
        return START_STICKY;
    }

    private Notification buildNotification(String content){
        // 유효한 pendingIntent
        Intent open = new Intent(this, com.shuttleroid.vehicle.ui.main.MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, VehicleApp.CHANNEL_OPS)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // 반드시 존재하는 아이콘
                .setContentTitle("Shuttle-roid 운행")
                .setContentText(content != null ? content : "운행 상태 표시")
                .setContentIntent(pi)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void updateContent(String text){
        Notification n = buildNotification(text);
        startForeground(NOTI_ID, n);
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
