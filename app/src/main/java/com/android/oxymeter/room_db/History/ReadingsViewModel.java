package com.android.oxymeter.room_db.History;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ReadingsViewModel extends AndroidViewModel {

    private ReadingsRepository mRepository;

    private LiveData<List<ReadingsTable>> mAllReadings;

    public ReadingsViewModel(@NonNull Application application) {
        super(application);

        mRepository = new ReadingsRepository(application);
    }

    public LiveData<List<ReadingsTable>> getAllReadings(long startTimestamp) {
        return mRepository.getAllReadings(startTimestamp);
    }

    public void addReading(ReadingsTable readings) {
        mRepository.addReading(readings);
    }

    public void deleteAllReadings() {
        mRepository.deleteAll();
    }
}
