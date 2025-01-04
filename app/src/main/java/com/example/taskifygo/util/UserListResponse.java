package com.example.taskifygo.util;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserListResponse {
    @SerializedName("item")
    private List<UserData> userList;

    public List<UserData> getUserList() {
        return userList;
    }

    public void setUserList(List<UserData> tasks) {
        this.userList = userList;
    }
}
