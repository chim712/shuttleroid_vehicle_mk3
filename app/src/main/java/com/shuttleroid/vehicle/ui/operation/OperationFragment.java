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
import com.shuttleroid.vehicle.data.entity.BusStop;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;
import com.shuttleroid.vehicle.util.TimeUtil;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OperationFragment extends Fragment {

    private OperationViewModel vm;
    private TextView clock, info1, info2, info3;
    private Button btnAnnounce, btnSettings, btnEmergency, btnManualStart;

    private Long routeId;
    private String departTime; // "HH:mm"
    private String routeNameArg;     // args에 노선명이 들어오면 사용
    private String originNameArg;    // args에 출발지명이 들어오면 우선 사용

    // DB에서 읽은 정류장 목록 & 로컬 인덱스(Controller와 동일 규칙 적용: DEPART에서 증가)
    private List<BusStop> stops;
    private static int idx = 0;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operation, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        clock = v.findViewById(R.id.txtClock);
        info1 = v.findViewById(R.id.txtInfo1);
        info2 = v.findViewById(R.id.txtInfo2);
        info3 = v.findViewById(R.id.txtInfo3);
        btnAnnounce   = v.findViewById(R.id.btnAnnounce);
        btnSettings   = v.findViewById(R.id.btnSettings);
        btnEmergency  = v.findViewById(R.id.btnEmergency);
        btnManualStart= v.findViewById(R.id.btnManualStart);

        Bundle args = getArguments();
        if (args != null){
            routeId       = args.getLong("routeID", -1);
            departTime    = args.getString("departTime", "00:00");
            routeNameArg  = args.getString("routeName", null);
            originNameArg = args.getString("originStopName", null);
        }

        vm = new ViewModelProvider(this).get(OperationViewModel.class);
        vm.startService();

        // 상단 기본 정보(대기모드 기본 UI)
        LocalTime depart = TimeUtil.parseHm(departTime);
        info1.setText(routeNameArg != null ? routeNameArg : ("노선 ID: " + routeId));
        info2.setText(originNameArg != null ? ("출발지: " + originNameArg) : "출발지: (조회 중)");
        info3.setText(departTime + " 출발");

        // 정류장 목록 로드 (로컬에서 이름 갱신용)
        if (routeId != null && routeId > 0) {
            io.execute(() -> {
                IntegratedRepository repo = new IntegratedRepository(requireContext().getApplicationContext());
                List<BusStop> list = repo.getOrderedStops(routeId);
                stops = list;
                // 출발지명이 args에 없으면 첫 정류장으로 채워넣기
                if (originNameArg == null && list != null && !list.isEmpty()) {
                    String first = safeStopName(list.get(0));
                    requireActivity().runOnUiThread(() -> info2.setText("출발지: " + first));
                }
            });
        }

        DriveController c = vm.ensureController();

        // 운행 컨텍스트 세팅
        SessionStore ss = SessionStore.getInstance();
        String vehicleId = ss.getVehicleId();
        String courseId  = ss.getCurrentCourseId(); // null 허용
        c.setRouteContext(routeId != null ? routeId : -1L, departTime, courseId, vehicleId);

        // 자동 대기 → 출발 (T-3분)
        c.startWaiting(depart, getViewLifecycleOwner(), new DriveController.Callbacks() {
            @Override public void onAutoStart() {
                info3.setText("운행 중");
                // 운행모드 진입 시 현재 정류소 표시(처음엔 출발지)
                updateCurrentStopLabel(/*no event*/ null);
            }
            @Override public void onLocationEvent(DriveController.Event e) {
                // 이벤트별 현재 정류소+상태 표기
                updateCurrentStopLabel(e);
            }
            @Override public void onReachedTerminal() {
                info3.setText("오늘 운행 종료");
                btnManualStart.setEnabled(false);
                info2.setText("당일 운행 종료");
            }
            @Override public void onTick(ZonedDateTime now) {
                vm.updateClock(now);
            }
        });

        // 라이브데이터 관찰
        vm.clockTextLive().observe(getViewLifecycleOwner(),
                t -> clock.setText(t != null ? t : "--:--:--"));

        vm.runningLive().observe(getViewLifecycleOwner(), running -> {
            boolean r = Boolean.TRUE.equals(running);
            btnManualStart.setEnabled(!r);
            // 대기 ↔ 운행 전환에 맞춰 상단 보조 문구
            info3.setText(r ? "운행 중" : (departTime + " 출발"));
            // 대기모드로 돌아올 일은 없음(오늘 종료 외). 필요시 여기서도 info2 재세팅 가능.
        });

        // 수동 출발
        btnManualStart.setOnClickListener(v1 ->
                c.forceStart(new DriveController.Callbacks() {
                    @Override public void onAutoStart() { info3.setText("운행 중 (수동)"); updateCurrentStopLabel(null); }
                    @Override public void onTick(ZonedDateTime now) { vm.updateClock(now); }
                })
        );

        // TODO: 방송/설정/긴급 버튼 핸들러
    }

    /** 이벤트에 따른 현재 정류소 라벨 갱신
     * Controller도 DEPART에서만 index++ 하므로 동일 규칙을 로컬에서도 적용한다. */

    public static void increaseIdx(){idx++;}
    private void updateCurrentStopLabel(@Nullable DriveController.Event e){
        if (stops == null || stops.isEmpty()) return;

//        if (e == DriveController.Event.DEPART) {
//            idx++;
//            if (idx >= stops.size()) idx = stops.size() - 1;
//        }

        String name = safeStopName(stops.get(idx));
        String status = statusKo(e);
        // 운행모드 표기: "현재 정류소: XXX (접근/도착/출발)"
        if (status == null) {
            info2.setText("현재 정류소: " + name);
        } else {
            info2.setText("현재 정류소: " + name + " (" + status + ")");
        }
    }

    private @NonNull String safeStopName(BusStop s){
        try {
            if (s.stopName != null && !s.stopName.isEmpty()) return s.stopName;
            // 게터만 있는 구현 대비
            java.lang.reflect.Method m = s.getClass().getMethod("getStopName");
            Object v = m.invoke(s);
            return v != null ? String.valueOf(v) : "-";
        } catch (Throwable ignored) {
            return "-";
        }
    }

    private @Nullable String statusKo(@Nullable DriveController.Event e){
        if (e == null) return null;
        switch (e){
            case APPROACH: return "접근";
            case ARRIVAL:   return "도착";
            case DEPARTURE:   return "출발";
            default:       return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        io.shutdownNow();
        if (vm != null) vm.stopService();
    }
}
