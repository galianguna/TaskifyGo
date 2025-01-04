package com.example.taskifygo.util;

import com.google.gson.annotations.SerializedName;

public class UserData {
    @SerializedName("sno")
    private int sno;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userType")
    private String userType;



    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String date) {
        this.userType = userType;
    }


}
