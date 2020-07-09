package com.android.oxymeter.fragments.ui.data_tab;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DataViewModel extends ViewModel {


    /**
     * Live Data Instance
     */
    private MutableLiveData<Integer> mSpO2 = new MutableLiveData<>();
    private MutableLiveData<Integer> mPulseRate = new MutableLiveData<>();
    private MutableLiveData<Integer> mAmp = new MutableLiveData<>();
    private MutableLiveData<Double> mPi = new MutableLiveData<>();
    private MutableLiveData<String> mTimeElapsed = new MutableLiveData<>();

    public void setSpo2(Integer SpO2) {
        mSpO2.setValue(SpO2);
    }

    public MutableLiveData<Integer> getSpO2() {
        return mSpO2;
    }

    public void setPulseRate(Integer pulseRate) {
        mPulseRate.setValue(pulseRate);
    }

    public MutableLiveData<Integer> getPulseRate() {
        return mPulseRate;
    }

    public void setAmp(Integer amp) {
        mAmp.setValue(amp);
    }

    public MutableLiveData<Integer> getAmp() {
        return mAmp;
    }

    public void setPi(Double pi) {
        mPi.setValue(pi);
    }

    public MutableLiveData<Double> getPi() {
        return mPi;
    }

    public void setTimeElapsed(String elapsedTime) {
        mTimeElapsed.setValue(elapsedTime);
    }

    public MutableLiveData<String> getTimeElapsed() {
        return mTimeElapsed;
    }


}