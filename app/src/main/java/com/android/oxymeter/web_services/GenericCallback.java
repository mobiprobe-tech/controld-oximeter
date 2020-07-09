package com.android.oxymeter.web_services;

public interface GenericCallback {

    void onSuccess(String successMessage);

    void onFailure(String errorMessage);
}
