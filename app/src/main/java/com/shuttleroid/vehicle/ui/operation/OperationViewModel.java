package com.shuttleroid.vehicle.ui.operation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shuttleroid.vehicle.domain.CourseProcessor;
import com.shuttleroid.vehicle.domain.StopProcessor;

public class OperationViewModel extends ViewModel {
    private final LiveData<String> bottomText1 = CourseProcessor.getCurrentRouteLive();
    private final LiveData<String> bottomText2 = StopProcessor.getCurrentStopLive();

    public LiveData<String> getBottomText1() {
        return bottomText1;
    }

    public LiveData<String> getBottomText2() {
        return bottomText2;
    }

}
