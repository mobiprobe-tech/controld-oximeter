package com.android.oxymeter.fragments.ui.history_tab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HistoryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HistoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("List of recorded data");
    }

    public LiveData<String> getText() {
        return mText;
    }
}