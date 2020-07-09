package com.android.oxymeter.web_services.GetSubUsers;

import com.android.oxymeter.room_db.Users.UserTable;

import java.util.ArrayList;

public interface GetSubUsers_ViewModelCallback {

    void onSuccess();

    void onNoRecordFound(String errorMessage);

    void onFailure(String errorMessage);
}
