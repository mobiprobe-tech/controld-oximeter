package com.android.oxymeter.room_db.History;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
//,foreignKeys = @ForeignKey(entity = SessionTable.class, parentColumns = "start_time", childColumns = "start_time", onDelete = ForeignKey.CASCADE)

//foreignKeys = [ForeignKey(
//        entity = ListCategory::class,
//        parentColumns = ["id"],
//        childColumns = ["list_category_id"],
//        onDelete = CASCADE)]
@Entity(tableName = "readings_table")
@ForeignKey(entity = SessionTable.class, parentColumns = "start_time", childColumns = "start_time", onDelete = ForeignKey.CASCADE)
public class ReadingsTable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "start_time")
    long mStartTime;

    @ColumnInfo(name = "pulse")
    int mPulse;

    @ColumnInfo(name = "spo2")
    int mSpO2;

    @ColumnInfo(name = "pi_data")
    double mPI;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getmStartTime() {
        return mStartTime;
    }

    public void setmStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public int getmPulse() {
        return mPulse;
    }

    public void setmPulse(int mPulse) {
        this.mPulse = mPulse;
    }

    public int getmSpO2() {
        return mSpO2;
    }

    public void setmSpO2(int mSpO2) {
        this.mSpO2 = mSpO2;
    }

    public double getmPI() {
        return mPI;
    }

    public void setmPI(double mPI) {
        this.mPI = mPI;
    }
}
