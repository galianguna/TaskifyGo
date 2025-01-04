package com.example.taskifygo.util;

import com.google.gson.annotations.SerializedName;

public class Notes {

    @SerializedName("sno")
    private int sno;

    @SerializedName("userName")
    private String userName;

    @SerializedName("notes")
    private String notes;
    @SerializedName("uniqueId")
    private String uniqueId;

    @SerializedName("title")
    private String title;
    // Getters and setters

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
