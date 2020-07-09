package com.android.oxymeter.room_db.History;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.android.oxymeter.room_db.OxymeterDatabase;
import com.android.oxymeter.utilities.CommonUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SessionRepository {

    private SessionDao mSessionDao;
    private LiveData<List<SessionTable>> mAllSessions;
    private SessionTable mSessionTable;
    private LiveData<List<SessionTable>> sessionTableLiveData;
    private long mSessionLocalID;

    public SessionRepository(Application application) {
        OxymeterDatabase db = OxymeterDatabase.getDatabase(application);
        mSessionDao = db.sessionDao();
    }


    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<SessionTable>> getAllSessions(String userID) {

        if (CommonUtils.checkData(userID).isEmpty()) {
            return mSessionDao.getLoggedUserSessions();
        } else {
            return mSessionDao.getUserSessions(userID);
        }
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insertSession(SessionTable sessionTable) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> {
            mSessionDao.insertSession(sessionTable);
        });
    }

    public long getSessionLocalIdByTimeStamp(long startTimestamp){
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mSessionLocalID = mSessionDao.getSessionLocalIdByTimeStamp(startTimestamp));

        return mSessionLocalID;
    }

    public void endCurrentSession(long endTimeStamp, long totalDuration, double avgPulse, double avgSpO2, double avgPI, long localSessionID) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mSessionDao.endCurrentSession(endTimeStamp, totalDuration, avgPulse, avgSpO2, avgPI, localSessionID));
    }

    public void updateSyncStatus(int isSynced, String remoteSessionID, long localSessionID) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mSessionDao.updateSyncStatus(isSynced, remoteSessionID, localSessionID));
    }

    public SessionTable getSessionDetails(String remoteSessionID, long localSessionID) {

        OxymeterDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mSessionTable = mSessionDao.getSessionDetails(remoteSessionID, localSessionID);
            }
        });

        return mSessionTable;
    }

    public LiveData<List<SessionTable>> getSessionDetailsByTimeStamp(long startTimestamp) {

        OxymeterDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sessionTableLiveData = mSessionDao.getSessionDetailsByTimeStamp(startTimestamp);
            }
        });

        return sessionTableLiveData;
    }

    public SessionTable getSessionDetailsByLocalID(long localSessionID) {

        /*OxymeterDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mSessionTable = mSessionDao.getSessionDetailsByLocalID(localSessionID);
            }
        });

        return mSessionTable;*/


        try {
            return new GetSessionDetailsByLocalIDAsyncTask().execute(localSessionID).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SessionTable getSessionDetailsByRemoteID(String remoteSessionID) {

        OxymeterDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mSessionTable = mSessionDao.getSessionDetailsByRemoteID(remoteSessionID);
            }
        });

        return mSessionTable;
    }

    public void deleteAll() {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mSessionDao.deleteAll());

    }

    private class GetSessionDetailsByLocalIDAsyncTask extends AsyncTask<Long, Void,SessionTable>
    {
        @Override
        protected SessionTable doInBackground(Long... longs) {
            return mSessionDao.getSessionDetailsByLocalID(longs[0]);
        }
    }
}
