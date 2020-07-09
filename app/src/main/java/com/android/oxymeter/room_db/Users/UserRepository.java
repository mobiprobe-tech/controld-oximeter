package com.android.oxymeter.room_db.Users;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.android.oxymeter.room_db.OxymeterDatabase;

import java.util.List;

public class UserRepository {


    private UserDao mUserDao;
    private LiveData<List<UserTable>> mAllSubUsers;
    private LiveData<List<UserTable>> mAllUsers;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public UserRepository(Application application) {
        OxymeterDatabase db = OxymeterDatabase.getDatabase(application);
        mUserDao = db.userDao();
        mAllSubUsers = mUserDao.getSubUsers();
        mAllUsers = mUserDao.getAllUsers();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<UserTable>> getAllSubUsers() {
        return mAllSubUsers;
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<UserTable>> getAllUsers() {
        return mAllUsers;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void addUser(UserTable user) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mUserDao.addUser(user));
    }

    public void deleteAllSubUsers() {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mUserDao.deleteAllSubUsers());

    }

    public void addUsersList(List<UserTable> users) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mUserDao.addUsersList(users));
    }

    public void deleteUser(String mUserID) {
        OxymeterDatabase.databaseWriteExecutor.execute(() -> mUserDao.deleteById(mUserID));
    }

}
