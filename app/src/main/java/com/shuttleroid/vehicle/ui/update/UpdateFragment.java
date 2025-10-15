package com.shuttleroid.vehicle.ui.update;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.data.repository.IntegratedRepository;
import com.shuttleroid.vehicle.service.SyncManager;

public class UpdateFragment extends Fragment {

    public interface OnGoToOperationListener {
        void onGoToOperation();
    }

    private OnGoToOperationListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnGoToOperationListener) {
            listener = (OnGoToOperationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGoToOperationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update, container, false);
        Button btnGoToOperation = view.findViewById(R.id.btnGoToOperation);
        btnGoToOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    SyncManager.getInstance(getContext()).updateRequest(101);
                    SyncManager.getInstance(getContext()).scheduleRequest(101001);
                    listener.onGoToOperation();
                }
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
