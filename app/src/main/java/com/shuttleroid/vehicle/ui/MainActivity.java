package com.shuttleroid.vehicle.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.data.dao.IntegratedDao;
import com.shuttleroid.vehicle.data.database.AppDatabase;
import com.shuttleroid.vehicle.service.AnnounceManager;
import com.shuttleroid.vehicle.ui.login.LoginFragment;
import com.shuttleroid.vehicle.ui.operation.OperationFragment;
import com.shuttleroid.vehicle.ui.update.UpdateFragment;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnLoginSuccessListener, UpdateFragment.OnGoToOperationListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(savedInstanceState == null){ // App Start
            showLoginFragment();
        }
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        AnnounceManager am = AnnounceManager.getInstance(this);
        //am.startAnnouncement("현재 정류장", "다음 정류장");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AnnounceManager.getInstance(getApplicationContext())
                    .startAnnouncement("서울역", "시청");
        }, 5000);

    }


    private void showLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        replaceFragment(loginFragment, "LoginFragment");
    }

    private void showUpdateFragment() {
        UpdateFragment updateFragment = new UpdateFragment();
        replaceFragment(updateFragment, "UpdateFragment");
    }

    private void showOperationFragment() {
        OperationFragment operationFragment = new OperationFragment();
        replaceFragment(operationFragment, "OperationFragment");
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        // 필요에 따라 addToBackStack()을 추가하여 뒤로 가기 동작을 관리할 수 있습니다.
        // fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    // LoginFragment.OnLoginSuccessListener 인터페이스 구현
    @Override
    public void onLoginSuccess() {
        // 로그인 성공 시 UpdateFragment로 전환
        showUpdateFragment();
    }

    // UpdateFragment.OnGoToOperationListener 인터페이스 구현
    @Override
    public void onGoToOperation() {
        // OperationFragment로 전환
        showOperationFragment();
    }
}