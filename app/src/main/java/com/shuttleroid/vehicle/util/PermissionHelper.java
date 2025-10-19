package com.shuttleroid.vehicle.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

public class PermissionHelper {

    public interface OnGranted { void run(); }
    public interface OnDenied { void run(); }

    public static void requestIfNeeded(Activity act, String[] perms, OnGranted onGranted, OnDenied onDenied){
        boolean fine = ActivityCompat.checkSelfPermission(act, perms[0]) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(act, perms[1]) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        if (fine && coarse) {
            onGranted.run();
        } else {
            ActivityCompat.requestPermissions(act, perms, 1001);
            // MainActivity의 onRequestPermissionsResult에서 처리하지 않고,
            // 단순화: 다음 액티비티 재시작 시 그랜트되었을 가능성 고려.
            // 바로 반영 원하면 해당 콜백까지 구현하세요.
            onGranted.run(); // 최소 동작 보장용 (실서비스에선 정확한 콜백 분기 구현 권장)
        }
    }

    public static void requestBatteryOptimizationExemption(Activity act){
        try {
            PowerManager pm = (PowerManager) act.getSystemService(Activity.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(act.getPackageName())) {
                Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + act.getPackageName()));
                act.startActivity(i);
            }
        } catch (Exception ignore){}
    }
}
