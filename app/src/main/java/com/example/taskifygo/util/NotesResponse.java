package com.example.taskifygo.util;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotesResponse {
    @SerializedName("item")
    private List<Notes> notes;

    public List<Notes> getNotes() {
        return notes;
    }

    public void setNotes(List<Notes> notes) {
        this.notes = notes;
    }
}
