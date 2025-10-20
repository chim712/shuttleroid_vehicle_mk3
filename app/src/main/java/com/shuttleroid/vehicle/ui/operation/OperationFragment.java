package com.shuttleroid.vehicle.ui.operation;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.controller.DriveController;
import com.shuttleroid.vehicle.domain.StopEngine;
import com.shuttleroid.vehicle.util.TimeUtil;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class OperationFragment extends Fragment {

    private OperationViewModel vm;
    private TextView clock, info1, info2, info3;
    private Button btnAnnounce, btnSettings, btnEmergency, btnManualStart;

    private Long routeId;
    private String departTime; // "HH:mm"
    private final DateTimeFormatter clockFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operation, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        clock = v.findViewById(R.id.txtClock);
        info1 = v.findViewById(R.id.txtInfo1);
        info2 = v.findViewById(R.id.txtInfo2);
        info3 = v.findViewById(R.id.txtInfo3);
        btnAnnounce = v.findViewById(R.id.btnAnnounce);
        btnSettings = v.findViewById(R.id.btnSettings);
        btnEmergency = v.findViewById(R.id.btnEmergency);
        btnManualStart = v.findViewById(R.id.btnManualStart);

        Bundle args = getArguments();
        if (args != null){
            routeId = args.getLong("routeID", -1);
            departTime = args.getString("departTime", "00:00");
        }

        vm = new ViewModelProvider(this).get(OperationViewModel.class);
        vm.startService();

        LocalTime depart = TimeUtil.parseHm(departTime);
        info1.setText("노선ID: " + routeId);
        info2.setText("출발지: (첫 정류장)"); // TODO DB에서 조회하여 대체
        info3.setText(departTime + " 출발");

        DriveController c = vm.ensureController();

        // ▶︎ 운행 컨텍스트 세팅 (SessionStore 보조)
        SessionStore ss = SessionStore.getInstance();
        String vehicleId = ss.getVehicleId();
        String courseId = ss.getCurrentCourseId(); // 없으면 null 허용
        c.setRouteContext(routeId != null ? routeId : -1L, departTime, courseId, vehicleId);

        // ▶︎ 자동 대기 → 출발 (T-3분)
        c.startWaiting(depart, getViewLifecycleOwner(), new DriveController.Callbacks() {
            @Override public void onAutoStart() {
                // 필요 시 Foreground 알림 문구 업데이트 등
            }
            @Override public void onLocationEvent(StopEngine.Event e) { /* UI 갱신 필요 시 */ }
            @Override public void onReachedTerminal() { /* 다음 코스 안내/종료 UI */ }
            @Override public void onTick(ZonedDateTime now) {
                clock.setText(now.toLocalTime().format(clockFmt));
            }
        });

        // ▶︎ RUNNING 상태 관찰 (observeRunning → running().observe로 수정)
        c.running().observe(getViewLifecycleOwner(), isRunning -> {
            // 필요 시 버튼 활성/비활성 등 UI 반영
            // WAITING일 땐 수동출발 버튼 활성, RUNNING일 땐 비활성 등
            btnManualStart.setEnabled(Boolean.FALSE.equals(isRunning));
        });

        // ▶︎ 수동 출발
        btnManualStart.setOnClickListener(v1 ->
                c.forceStart(new DriveController.Callbacks() {
                    @Override public void onAutoStart() { /* 알림 문구 업데이트 등 */ }
                    @Override public void onTick(ZonedDateTime now) {
                        clock.setText(now.toLocalTime().format(clockFmt));
                    }
                })
        );

        // TODO: 방송/설정/긴급 버튼 핸들러 연결
    }
}
