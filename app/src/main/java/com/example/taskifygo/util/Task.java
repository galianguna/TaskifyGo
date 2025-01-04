package com.example.taskifygo.util;

import com.google.gson.annotations.SerializedName;

public class Task {

    @SerializedName("sno")
    private int sno;

    @SerializedName("userName")
    private String userName;

    @SerializedName("date")
    private String date;

    @SerializedName("taskDetails")
    private String taskDetails;

    @SerializedName("timeTime")
    private String timeTime;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("status")
    private String status;

    @SerializedName("uniqueId")
    private String uniqueId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public String getTimeTime() {
        return timeTime;
    }

    public void setTimeTime(String timeTime) {
        this.timeTime = timeTime;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
