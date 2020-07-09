package com.android.oxymeter.room_db.History;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReadingsDao {

    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ReadingsTable readings);

    @Query("DELETE FROM readings_table")
    void deleteAll();

    @Query("SELECT * from readings_table WHERE start_time = :startTime")
    LiveData<List<ReadingsTable>> getReadings(long startTime);

    @Query("SELECT * from readings_table WHERE start_time = :startTime")
    List<ReadingsTable> getReadingsByStartTimeStamp(long startTime);
}
