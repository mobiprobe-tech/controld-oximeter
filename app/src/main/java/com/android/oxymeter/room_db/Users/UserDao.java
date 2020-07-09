package com.android.oxymeter.room_db.Users;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(UserTable userTable);

    @Query("DELETE FROM user_table WHERE user_id=:mUserID")
    void deleteById(String mUserID);


    @Query("DELETE FROM user_table WHERE user_type = 'sub'")
    void deleteAllSubUsers();

    @Query("SELECT * from user_table WHERE user_type ='sub'")
    LiveData<List<UserTable>> getSubUsers();

    @Query("SELECT * from user_table")
    LiveData<List<UserTable>> getAllUsers();

    @Query(("SELECT * from user_table WHERE user_id = :mUserID"))
    UserTable getUserDetails(String mUserID);

    @Insert
    void addUsersList(List<UserTable> users);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addUser(UserTable user);
}
