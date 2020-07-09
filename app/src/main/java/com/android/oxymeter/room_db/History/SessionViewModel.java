package com.android.oxymeter.room_db.History;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SessionViewModel extends AndroidViewModel {

    private SessionRepository mRepository;

    private LiveData<List<SessionTable>> mAllSessions;

    private SessionID mSessionID;
    private SessionTable mSessionTable;

    public SessionViewModel(@NonNull Application application) {
        super(application);

        mRepository = new SessionRepository(application);
    }


    public LiveData<List<SessionTable>> getAllSessions(String userID) {
        return mRepository.getAllSessions(userID);
    }

    public void insertSession(SessionTable sessionTable) {
        mRepository.insertSession(sessionTable);
    }

    public long getSessionLocalIdByTimeStamp(long startTimeStamp) {
        return mRepository.getSessionLocalIdByTimeStamp(startTimeStamp);
    }

    public void endCurrentSession(long endTimeStamp, long totalDuration, double avgPulse, double avgSpO2, double avgPI, long localSessionID) {
        mRepository.endCurrentSession(endTimeStamp, totalDuration, avgPulse, avgSpO2, avgPI, localSessionID);
    }

    public void updateSyncStatus(int isSynced, String remoteSessionID, long localSessionID) {
        mRepository.updateSyncStatus(isSynced, remoteSessionID, localSessionID);
    }

    public SessionTable getSessionDetails(String remoteSessionID, long localSessionID) {
        return mRepository.getSessionDetails(remoteSessionID, localSessionID);
    }

    public LiveData<List<SessionTable>> getSessionDetailsByTimeStamp(long startTimestamp) {
        return mRepository.getSessionDetailsByTimeStamp(startTimestamp);
    }

    public SessionTable getSessionDetailsByLocalID(long localSessionID) {
        return mRepository.getSessionDetailsByLocalID(localSessionID);
    }

    public SessionTable getSessionDetailsByRemoteID(String remoteSessionID) {
        return mRepository.getSessionDetailsByRemoteID(remoteSessionID);
    }

    public void deleteAllSessions() {
        mRepository.deleteAll();
    }

}
