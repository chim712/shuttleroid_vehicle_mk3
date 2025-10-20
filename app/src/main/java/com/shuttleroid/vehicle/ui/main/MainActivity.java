package com.shuttleroid.vehicle.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.ui.login.OrgCheckFragment;
import com.shuttleroid.vehicle.ui.login.LoginFragment;
import com.shuttleroid.vehicle.ui.update.UpdateFragment;
import com.shuttleroid.vehicle.ui.operation.OperationFragment;
import com.shuttleroid.vehicle.util.PermissionHelper;

/**
 * 앱 전체 프래그먼트 컨테이너.
 * 순서: OrgCheck → Login → Update → Operation
 */
public class MainActivity extends AppCompatActivity
        implements
        LoginFragment.OnLoginSuccessListener,
        UpdateFragment.OnGoToOperationListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 상태바/내비게이션 패딩
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                });

        // 위치 권한 요청 + 배터리 최적화 예외 유도
        PermissionHelper.requestIfNeeded(this,
                new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                () -> PermissionHelper.requestBatteryOptimizationExemption(this),
                () -> {
                    // 권한 거부 시 앱 종료
                    finishAndRemoveTask();
                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, new OrgCheckFragment())
                    .commit();
        }

        // Android 13+에서 알림 권한 런타임 요청
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        2002
                );
            }
        }
    }

    // 로그인 성공 → 업데이트로
    @Override public void onLoginSuccess() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new UpdateFragment())
                .addToBackStack(null)
                .commit();
    }

    // 업데이트 완료 → 운행으로
    @Override public void onGoToOperation() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new OperationFragment())
                .addToBackStack(null)
                .commit();
    }
}
