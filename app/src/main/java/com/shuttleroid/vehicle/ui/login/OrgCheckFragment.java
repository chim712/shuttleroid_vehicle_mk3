package com.shuttleroid.vehicle.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shuttleroid.vehicle.R;
import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.util.Toasts;

public class OrgCheckFragment extends Fragment {

    private LoginViewModel vm;
    private EditText edtOrgId;
    private TextView txtOrgName;
    private Button btnCheck, btnNext;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orgcheck, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        edtOrgId = v.findViewById(R.id.edtOrgId);
        txtOrgName = v.findViewById(R.id.txtOrgName);
        btnCheck = v.findViewById(R.id.btnCheckOrg);
        btnNext = v.findViewById(R.id.btnGoLogin);

        vm.orgName.observe(getViewLifecycleOwner(), name -> txtOrgName.setText(name!=null?name:""));

        btnCheck.setOnClickListener(v1 -> {
            String sOrg = edtOrgId.getText().toString().trim();
            if (TextUtils.isEmpty(sOrg)) { Toasts.throttled(requireContext(),"기관ID를 입력하세요"); return; }
            try {
                Long orgId = Long.parseLong(sOrg);
                vm.checkOrg(orgId);
                // 성공 시점은 콜백에서 받지만, 사용자가 '다음'을 누른다면 orgId를 SessionStore에 미리 저장
                SessionStore.getInstance().setOrgId(orgId);
            } catch (NumberFormatException e){
                Toasts.throttled(requireContext(),"기관ID 형식이 올바르지 않습니다");
            }
        });

        btnNext.setOnClickListener(v12 -> {
            if (TextUtils.isEmpty(txtOrgName.getText())) {
                Toasts.throttled(requireContext(),"기관 확인을 먼저 진행하세요");
                return;
            }
            // 이동: LoginFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
