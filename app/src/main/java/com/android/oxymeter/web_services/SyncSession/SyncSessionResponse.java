package com.android.oxymeter.web_services.SyncSession;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SyncSessionResponse {

    @SerializedName("response_code")
    @Expose
    private Integer responseCode;
    @SerializedName("response_message")
    @Expose
    private String responseMessage;

    @SerializedName("id")
    @Expose
    private String localSessionId;

    @SerializedName("session_id")
    @Expose
    private String serverSessionId;

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getLocalSessionId() {
        return localSessionId;
    }

    public void setLocalSessionId(String localSessionId) {
        this.localSessionId = localSessionId;
    }

    public String getServerSessionId() {
        return serverSessionId;
    }

    public void setServerSessionId(String serverSessionId) {
        this.serverSessionId = serverSessionId;
    }
}
