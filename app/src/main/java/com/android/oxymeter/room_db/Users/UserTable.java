package com.android.oxymeter.room_db.Users;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class UserTable {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    String mName;
    @ColumnInfo(name = "email")
    String mEmail;
    @ColumnInfo(name = "phone")
    String mPhone;
    @ColumnInfo(name = "gender")
    String mGender;
    @ColumnInfo(name = "dob")
    String mDob;
    @ColumnInfo(name = "height")
    String mHeight;
    @ColumnInfo(name = "weight")
    String mWeight;
    @ColumnInfo(name = "bloodgroup")
    String mBloodGroup;
    @ColumnInfo(name = "user_id")
    String mUserID;
    @ColumnInfo(name = "user_type")
    String mUserType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getmGender() {
        return mGender;
    }

    public void setmGender(String mGender) {
        this.mGender = mGender;
    }

    public String getmHeight() {
        return mHeight;
    }

    public void setmHeight(String mHeight) {
        this.mHeight = mHeight;
    }

    public String getmWeight() {
        return mWeight;
    }

    public void setmWeight(String mWeight) {
        this.mWeight = mWeight;
    }

    public String getmBloodGroup() {
        return mBloodGroup;
    }

    public void setmBloodGroup(String mBloodGroup) {
        this.mBloodGroup = mBloodGroup;
    }

    public String getmUserID() {
        return mUserID;
    }

    public void setmUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public String getmDob() {
        return mDob;
    }

    public void setmDob(String mDob) {
        this.mDob = mDob;
    }

    public String getmUserType() {
        return mUserType;
    }

    public void setmUserType(String mUserType) {
        this.mUserType = mUserType;
    }
}
