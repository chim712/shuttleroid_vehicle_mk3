package com.shuttleroid.vehicle.ui.login;

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
import com.shuttleroid.vehicle.app.SessionStore;
import com.shuttleroid.vehicle.util.Toasts;

public class LoginFragment extends Fragment {

    public interface OnLoginSuccessListener {
        void onLoginSuccess();
    }
    private OnLoginSuccessListener callback;

    private LoginViewModel vm;
    private EditText edtDriverId, edtPw;
    private Button btnLogin;

    @Override public void onAttach(@NonNull Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof OnLoginSuccessListener) callback = (OnLoginSuccessListener) ctx;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        edtDriverId = v.findViewById(R.id.edtDriverId);
        edtPw = v.findViewById(R.id.edtPassword);
        btnLogin = v.findViewById(R.id.btnLogin);

        vm.loginOk.observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok) && callback!=null) {
                callback.onLoginSuccess();
            }
        });

        btnLogin.setOnClickListener(v1 -> {
            String sDriver = edtDriverId.getText().toString().trim();
            String pw = edtPw.getText().toString();
            if (TextUtils.isEmpty(sDriver) || TextUtils.isEmpty(pw)){
                Toasts.throttled(requireContext(),"사원ID/비밀번호를 입력하세요");
                return;
            }
            Long orgId = SessionStore.getInstance().getOrgId();
            if (orgId == null){
                Toasts.throttled(requireContext(),"기관 확인을 먼저 진행하세요");
                return;
            }
            try {
                Long driverId = Long.parseLong(sDriver);
                vm.login(orgId, driverId, pw);
            } catch (NumberFormatException e){
                Toasts.throttled(requireContext(),"사원ID 형식이 올바르지 않습니다");
            }
        });
    }
}
