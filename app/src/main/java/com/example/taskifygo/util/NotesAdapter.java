package com.example.taskifygo.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskifygo.R;

import java.util.List;
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Notes> notes;
    private OnNoteClickListener onNoteClickListener;

    private OnNoteLongClickListener onNoteLongClickListener;

    public NotesAdapter(List<Notes> notes, OnNoteClickListener onNoteClickListener,OnNoteLongClickListener onNoteLongClickListener) {
        this.notes = notes;
        this.onNoteClickListener = onNoteClickListener;
        this.onNoteLongClickListener = onNoteLongClickListener;

    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Notes note = notes.get(position);
        holder.noteTitle.setText(note.getTitle()); // Assuming note is just a string for now
        holder.noteTitle.setTextSize(20);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.noteTitle.getLayoutParams();
        layoutParams.setMargins(10, 0, 0, 10); // Set left, top, right, bottom margins (in pixels)
        holder.noteTitle.setLayoutParams(layoutParams);
        holder.noteContent.setText(note.getNotes());
        holder.noteContent.setTextSize(17);

        holder.itemView.setOnClickListener(v -> {
            if (onNoteClickListener != null) {
                onNoteClickListener.onNoteClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onNoteLongClickListener != null) {
                onNoteLongClickListener.onNoteLongClick(position);
                String titleToCopy = note.getTitle();
                String textToCopy = note.getNotes();

                ClipboardManager clipboard = (ClipboardManager) holder.itemView.getContext().getSystemService(holder.itemView.getContext().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("TaskifyGo",titleToCopy+"\n"+textToCopy);

                // Set the text to the clipboard
                clipboard.setPrimaryClip(clip);

                return true; // Return true to indicate the event was handled
            }
            return false; // Return false to allow other listeners to be triggered
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent;

        public NoteViewHolder(View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(R.id.noteContent);
        }
    }

    public interface OnNoteClickListener {
        void onNoteClick(int position);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(int position);
    }
}
