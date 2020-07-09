package com.android.oxymeter.room_db.History;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.android.oxymeter.room_db.OxymeterDatabase;

import java.util.List;

public class ReadingsRepository {

    private ReadingsDao mReadingsDao;
    private LiveData<List<ReadingsTable>> mAllReadings;

    public ReadingsRepository(Application application) {
        OxymeterDatabase db = OxymeterDatabase.getDatabase(application);
        mReadingsDao = db.readingsDao();
    }

    public void addReading(ReadingsTable readings) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mReadingsDao.insert(readings));
    }

    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<ReadingsTable>> getAllReadings(long startTimeStamp) {
        return mReadingsDao.getReadings(startTimeStamp);
    }

    public void deleteAll() {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mReadingsDao.deleteAll());

    }
}
