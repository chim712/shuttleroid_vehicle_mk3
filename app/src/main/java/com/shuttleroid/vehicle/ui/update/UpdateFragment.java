package com.shuttleroid.vehicle.ui.update;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.network.dto.ScheduleItem;
import com.shuttleroid.vehicle.ui.operation.OperationFragment;
import com.shuttleroid.vehicle.util.Toasts;

import java.util.List;

public class UpdateFragment extends Fragment {

    public interface OnGoToOperationListener { void onGoToOperation(); }
    private OnGoToOperationListener callback;

    private UpdateViewModel vm;
    private EditText edtDataVer;
    private Button btnUpdate, btnLoad, btnGoOperation;

    private List<ScheduleItem> schedule;

    @Override public void onAttach(@NonNull Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof OnGoToOperationListener) callback = (OnGoToOperationListener) ctx;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(this).get(UpdateViewModel.class);

        edtDataVer = v.findViewById(R.id.edtDataVer);
        btnUpdate = v.findViewById(R.id.btnDoUpdate);
        btnLoad = v.findViewById(R.id.btnLoadSchedule);
        btnGoOperation = v.findViewById(R.id.btnGoOperation);

        vm.updateDone.observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) Toasts.throttled(requireContext(),"업데이트 완료/최신");
        });

        vm.scheduleLive.observe(getViewLifecycleOwner(), list -> {
            schedule = list;
            if (list!=null && !list.isEmpty()){
                Toasts.throttled(requireContext(),"스케줄 수신: " + list.size() + "건");
            }
        });

        btnUpdate.setOnClickListener(v1 -> {
            String sVer = edtDataVer.getText().toString().trim();
            if (TextUtils.isEmpty(sVer)){ Toasts.throttled(requireContext(),"dataVer를 입력하세요"); return; }
            try { vm.doUpdate(Long.parseLong(sVer)); }
            catch (NumberFormatException e){ Toasts.throttled(requireContext(),"dataVer 형식이 올바르지 않습니다"); }
        });

        btnLoad.setOnClickListener(v12 -> vm.loadTodaySchedule());

        btnGoOperation.setOnClickListener(v13 -> {
            if (schedule==null || schedule.isEmpty()){
                Toasts.throttled(requireContext(),"스케줄이 없습니다");
                return;
            }
            ScheduleItem first = schedule.get(0);
            Bundle args = new Bundle();
            args.putLong("routeID", first.routeID);
            args.putString("departTime", first.departureTime);
            OperationFragment f = new OperationFragment();
            f.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, f)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
