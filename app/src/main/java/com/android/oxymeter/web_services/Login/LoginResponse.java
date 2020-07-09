package com.android.oxymeter.web_services.Login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResponse {

    @SerializedName("response_code")
    @Expose
    private Integer responseCode;
    @SerializedName("response_message")
    @Expose
    private String responseMessage;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

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

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }


    public class Datum {

        @SerializedName("User_token")
        @Expose
        private String userToken;
        @SerializedName("Name")
        @Expose
        private String name;
        @SerializedName("Email")
        @Expose
        private String email;
        @SerializedName("Phone")
        @Expose
        private String phone;
        @SerializedName("Gender")
        @Expose
        private String gender;
        @SerializedName("DOB")
        @Expose
        private String dob;
        @SerializedName("Height")
        @Expose
        private String height;
        @SerializedName("Weight")
        @Expose
        private String weight;
        @SerializedName("Blood_group")
        @Expose
        private String bloodGroup;

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            this.userToken = userToken;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getBloodGroup() {
            return bloodGroup;
        }

        public void setBloodGroup(String bloodGroup) {
            this.bloodGroup = bloodGroup;
        }

    }

}
