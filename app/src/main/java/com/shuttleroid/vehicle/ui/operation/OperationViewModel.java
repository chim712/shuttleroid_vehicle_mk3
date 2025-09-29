package com.shuttleroid.vehicle.ui.operation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OperationViewModel extends ViewModel {
    private final MutableLiveData<String> bottomText1 = new MutableLiveData<>();
    private final MutableLiveData<String> bottomText2 = new MutableLiveData<>();

    public OperationViewModel() {
        // 초기 값 설정 (원하면 Fragment에서 바꿔도 됨)
        bottomText1.setValue("Route Name");
        bottomText2.setValue("Busstop Name");
    }

    public LiveData<String> getBottomText1() {
        return bottomText1;
    }

    public LiveData<String> getBottomText2() {
        return bottomText2;
    }

    public void setRoute(String s){
        bottomText1.setValue(s);
    }
    public void setStop(String s){
        bottomText2.setValue(s);
    }

}
