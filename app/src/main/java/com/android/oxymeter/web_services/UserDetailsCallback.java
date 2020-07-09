package com.android.oxymeter.web_services;

public interface UserDetailsCallback {
    void onSuccess(UserDetailsResponse.Datum userDetails);

    void onFailure(String errorMessage);
}
