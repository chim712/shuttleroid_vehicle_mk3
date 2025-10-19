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
import com.shuttleroid.vehicle.controller.DriveController;
import com.shuttleroid.vehicle.domain.StopEngine;
import com.shuttleroid.vehicle.util.TimeUtil;

import java.time.LocalTime;
import java.time.ZonedDateTime;

public class OperationFragment extends Fragment {

    private OperationViewModel vm;
    private TextView clock, info1, info2, info3;
    private Button btnAnnounce, btnSettings, btnEmergency, btnManualStart;

    private Long routeId;
    private String departTime; // "HH:mm"

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
        info2.setText("출발지: (첫 정류장)"); // TODO DB에서 조회
        info3.setText(departTime + " 출발");

        DriveController c = vm.ensureController();

        c.startWaiting(depart, getViewLifecycleOwner(), new DriveController.Callbacks() {
            @Override public void onAutoStart() {
                // TODO /route/start (flag=true)
            }
            @Override public void onLocationEvent(StopEngine.Event e) {
                // TODO /location + AnnounceManager
            }
            @Override public void onReachedTerminal() {
                // TODO /route/terminate
            }
            @Override public void onTick(ZonedDateTime now) {
                clock.setText(now.toLocalTime().toString());
            }
        });

        c.observeRunning(getViewLifecycleOwner(), new DriveController.Callbacks() {
            @Override public void onAutoStart() {}
            @Override public void onLocationEvent(StopEngine.Event e) { /* TODO */ }
            @Override public void onReachedTerminal() { /* TODO */ }
            @Override public void onTick(ZonedDateTime now) {
                clock.setText(now.toLocalTime().toString());
            }
        });

        btnManualStart.setOnClickListener(v1 -> c.forceStart(new DriveController.Callbacks() {
            @Override public void onAutoStart() { /* TODO /route/start */ }
            @Override public void onLocationEvent(StopEngine.Event e) {}
            @Override public void onReachedTerminal() {}
            @Override public void onTick(ZonedDateTime now) {}
        }));

        // TODO: 방송/설정/긴급 핸들러
    }
}
