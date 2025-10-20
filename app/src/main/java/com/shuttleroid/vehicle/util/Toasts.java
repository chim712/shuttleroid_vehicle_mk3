package com.shuttleroid.vehicle.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

public class Toasts {
    private static long lastShown = 0L;
    private static final long INTERVAL_MS = 10_000L; // 10초

    public static void throttled(Context ctx, String msg){
        long now = SystemClock.elapsedRealtime();
        if (now - lastShown >= INTERVAL_MS) {
            Handler main = new Handler(Looper.getMainLooper());
            main.post(() -> {
                Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            });
            //Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            //lastShown = now;
        }
    }
}
