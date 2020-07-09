package com.android.oxymeter.web_services.SyncSession;

public interface SyncSessionCallback {
    void onSuccess(long localSessionId, String serverSessionId);

    void onFailure(String errorMessage);
}
