package com.android.oxymeter.room_db.History;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertSession(SessionTable sessionTable);

    @Query("SELECT id from session_table WHERE start_time = :startTimestamp")
    long getSessionLocalIdByTimeStamp(long startTimestamp);

    @Query("UPDATE session_table SET end_time = :endTime, duration = :duration, average_pulse = :avgPulse, average_spo2 = :avgSpO2, average_pi = :avgPI WHERE id = :localSessionID")
    void endCurrentSession(long endTime, long duration, double avgPulse, double avgSpO2, double avgPI, long localSessionID);

    @Query("UPDATE session_table SET is_sync =:isSynced, session_id = :remoteSessionID WHERE id = :localSessionID")
    void updateSyncStatus(int isSynced, String remoteSessionID, long localSessionID);

    @Query("SELECT * from session_table WHERE end_time IS NOT NULL AND end_time != \"\" AND user_id = :userID")
    LiveData<List<SessionTable>> getUserSessions(String userID);

    @Query("SELECT * from session_table WHERE end_time IS NOT NULL AND end_time != \"\" AND is_self = '1'")
    LiveData<List<SessionTable>> getLoggedUserSessions();

    @Query("SELECT * FROM session_table WHERE session_id = :remoteSessionID OR id = :localSessionID")
    SessionTable getSessionDetails(String remoteSessionID, long localSessionID);

    @Query("SELECT * FROM session_table WHERE start_time = :startTimestamp")
    LiveData<List<SessionTable>> getSessionDetailsByTimeStamp(long startTimestamp);

    @Query("SELECT * FROM session_table WHERE id = :localSessionID")
    SessionTable getSessionDetailsByLocalID(long localSessionID);

    @Query("SELECT * FROM session_table WHERE session_id = :remoteSessionID")
    SessionTable getSessionDetailsByRemoteID(String remoteSessionID);

    @Query("DELETE FROM session_table")
    void deleteAll();
}
