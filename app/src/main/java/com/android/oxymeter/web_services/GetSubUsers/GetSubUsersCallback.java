package com.android.oxymeter.web_services.GetSubUsers;

import java.util.ArrayList;

public interface GetSubUsersCallback {
    void onSuccess(ArrayList<GetSubUsersResponse.Datum> usersList);

    void onNoRecordFound(String errorMessage);

    void onFailure(String errorMessage);
}
