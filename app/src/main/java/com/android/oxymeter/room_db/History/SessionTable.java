package com.android.oxymeter.room_db.History;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//foreignKeys = @ForeignKey(entity = UserTable.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE)

@Entity(tableName = "session_table")
public class SessionTable {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user_id")
    String mUserId;

    @ColumnInfo(name = "is_self")
    int mSelf; //0 = false, 1 = true

    @ColumnInfo(name = "time_bound_recording")
    int mIsTimeBoundRecording; //0 = false, 1 = true

    @ColumnInfo(name = "start_time")
    long mStartTime;

    @ColumnInfo(name = "end_time")
    long mEndTime;

    @ColumnInfo(name = "duration")
    long mDuration;

    @ColumnInfo(name = "average_pulse")
    double mAveragePulse;

    @ColumnInfo(name = "average_spo2")
    double mAverageSpO2;

    @ColumnInfo(name = "average_pi")
    double mAveragePI;

    @ColumnInfo(name = "is_sync")
    int mSync; //0 = false, 1 = true

    @ColumnInfo(name = "session_id")
    String mSessionID;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public long getmStartTime() {
        return mStartTime;
    }

    public void setmStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public long getmEndTime() {
        return mEndTime;
    }

    public void setmEndTime(long mEndTime) {
        this.mEndTime = mEndTime;
    }

    public long getmDuration() {
        return mDuration;
    }

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public double getmAveragePulse() {
        return mAveragePulse;
    }

    public void setmAveragePulse(double mAveragePulse) {
        this.mAveragePulse = mAveragePulse;
    }

    public double getmAverageSpO2() {
        return mAverageSpO2;
    }

    public void setmAverageSpO2(double mAverageSpO2) {
        this.mAverageSpO2 = mAverageSpO2;
    }

    public double getmAveragePI() {
        return mAveragePI;
    }

    public void setmAveragePI(double mAveragePI) {
        this.mAveragePI = mAveragePI;
    }

    public String getmSessionID() {
        return mSessionID;
    }

    public void setmSessionID(String mSessionID) {
        this.mSessionID = mSessionID;
    }

    public int getmSelf() {
        return mSelf;
    }

    public void setmSelf(int mSelf) {
        this.mSelf = mSelf;
    }

    public int getmSync() {
        return mSync;
    }

    public void setmSync(int mSync) {
        this.mSync = mSync;
    }

    public int getmIsTimeBoundRecording() {
        return mIsTimeBoundRecording;
    }

    public void setmIsTimeBoundRecording(int mIsTimeBoundRecording) {
        this.mIsTimeBoundRecording = mIsTimeBoundRecording;
    }
}
