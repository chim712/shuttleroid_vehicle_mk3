package com.shuttleroid.vehicle.ui.operation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shuttleroid.vehicle.R;


public class OperationFragment extends Fragment {

    private OperationViewModel viewModel;
    private TextView textBottom1;
    private TextView textBottom2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_operation, container, false);

        textBottom1 = view.findViewById(R.id.textBottom1);
        textBottom2 = view.findViewById(R.id.textBottom2);

        viewModel = new ViewModelProvider(this).get(OperationViewModel.class);

        // 하단 텍스트 관찰
        viewModel.getBottomText1().observe(getViewLifecycleOwner(), value -> textBottom1.setText(value));
        viewModel.getBottomText2().observe(getViewLifecycleOwner(), value -> textBottom2.setText(value));

        // 필요 시 동적으로 값 변경
        // viewModel.setBottomTexts("새로운 문구1", "새로운 문구2");

        return view;
    }
}