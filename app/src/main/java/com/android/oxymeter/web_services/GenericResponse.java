package com.android.oxymeter.web_services;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenericResponse {

    @SerializedName("response_code")
    @Expose
    private int responseCode;
    @SerializedName("response_message")
    @Expose
    private String responseMessage;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

}
