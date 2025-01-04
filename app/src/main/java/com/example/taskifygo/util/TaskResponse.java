package com.example.taskifygo.util;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TaskResponse {
    @SerializedName("item")
    private List<Task> tasks;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
