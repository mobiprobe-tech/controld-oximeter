package com.android.oxymeter.web_services.Login;

public interface LoginCallback {

    void onSuccess(String successMessage);

    void onFailure(String errorMessage);

}
