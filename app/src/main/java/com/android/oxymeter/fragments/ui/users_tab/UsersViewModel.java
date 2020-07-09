package com.android.oxymeter.fragments.ui.users_tab;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.oxymeter.room_db.Users.UserRepository;
import com.android.oxymeter.room_db.Users.UserTable;

import java.util.List;

public class UsersViewModel extends AndroidViewModel {

    private UserRepository mRepository;

    private LiveData<List<UserTable>> mAllSubUsers;
    private LiveData<List<UserTable>> mAllUsers;

    public UsersViewModel(Application application) {
        super(application);
        mRepository = new UserRepository(application);
        mAllSubUsers = mRepository.getAllSubUsers();
        mAllUsers = mRepository.getAllUsers();
    }

    public LiveData<List<UserTable>> getAllSubUsers() {
        return mAllSubUsers;
    }

    public LiveData<List<UserTable>> getAllUsers() {
        return mAllUsers;
    }

    public void deleteAllSubUsers() {
        mRepository.deleteAllSubUsers();
    }

    public void deleteUser(String mUserId) {
        mRepository.deleteUser(mUserId);
    }

    public void addAllUsers(List<UserTable> users) {
        mRepository.addUsersList(users);
    }

    public void addUser(UserTable user) {
        mRepository.addUser(user);
    }
}