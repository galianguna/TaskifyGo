package com.example.taskifygo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskifygo.remind.XlAccess;
import com.example.taskifygo.util.GridSpacingItemDecoration;
import com.example.taskifygo.util.Notes;
import com.example.taskifygo.util.NotesAdapter;
import com.example.taskifygo.util.NotesResponse;
import com.example.taskifygo.util.TaskResponse;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment{

    public static String Tag = "NotesFragment";
    View view;

    RecyclerView recyclerView;
    NotesResponse notesList = new NotesResponse();

    NotesAdapter notesAdapter;

    XlAccess xlAccess = new XlAccess();

    List<Notes> notes = new ArrayList<Notes>();
    private Dialog loaderDialog;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Tag,"Notes fragment start");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notes, container, false);


        recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        //recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(),2));
        //recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 7, true)); // 2 columns, 16px spacing
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.app_local_Store), Context.MODE_PRIVATE);
        String storedUser = sharedPreferences.getString("AppUser", null);
        showLoaderPopup();
        xlAccess.getNotes(HomeActivity.url, new XlAccess.DataCallback() {
            @Override
            public void onSuccess(String response) {
                dismissLoaderPopup();

                // Handle the response and update the UI (on the main thread)
                Log.i(Tag,response.toString());
                Gson gson = new Gson();
                notesList = gson.fromJson(response, NotesResponse.class);
                notes = notesList.getNotes();

                notes =  notes.stream().filter(data->data.getUserName().equals(storedUser))
                        .sorted(Comparator.comparingInt(Notes::getSno))        // Sort by sno
                        .collect(Collectors.toList());

                if(!notes.isEmpty()){
                    // Update the UI on the main thread
                    requireActivity().runOnUiThread(() -> {
                        notesAdapter = new NotesAdapter(notes, new NotesAdapter.OnNoteClickListener() {
                            // Handle note click here, for example, show an edit dialog
                            @Override
                            public void onNoteClick(int position) {
                                // Handle click
                                editNoteDialog(position);
                            }
                        }, new NotesAdapter.OnNoteLongClickListener() {
                            @Override
                            public void onNoteLongClick(int position) {
                                // Handle long-click
                                Log.e(Tag,"GGGGGGGGGG");
                            }
                        });
                        recyclerView.setAdapter(notesAdapter);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error (on the main thread)
                dismissLoaderPopup();
                recyclerView.setAdapter(notesAdapter);
                Log.v(Tag,"Error"+e.getMessage());
            }
        });

        //float button click
        if (getArguments() != null) {
            boolean isClicked = getArguments().getBoolean("click", false);
            if(isClicked){
                notesDialogBox(view);  // Set the LayoutManager
            }
        }

        return view;
    }


    public void notesDialogBox(View view) {
        Log.i(Tag, "start");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.addnotes_popup, null);
        dialogBuilder.setView(dialogView);

        // Initialize UI elements from dialog layout
        EditText textNote = dialogView.findViewById(R.id.notesText);
        EditText userTextNote = dialogView.findViewById(R.id.userText);
        EditText titleText = dialogView.findViewById(R.id.titleText);
        Button btnSaveNote = dialogView.findViewById(R.id.inputDone);

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.app_local_Store), Context.MODE_PRIVATE);
        String storedUser = sharedPreferences.getString("AppUser", null);
        userTextNote.setText(storedUser);
        userTextNote.setEnabled(false);
        Log.v(Tag,"storedUser"+storedUser);

        // Set up the dialog and show it
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        // Set a click listener for the Save button
        btnSaveNote.setOnClickListener(v -> {
            String user = userTextNote.getText().toString();
            String note = textNote.getText().toString();
            String title = titleText.getText().toString();
            if (user != null && note != null && title != null) {
                String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
                System.out.println("Generated Unique ID: " + timeStamp);
                String uniqueIdNew = timeStamp + storedUser;
                dialog.dismiss();
                showLoaderPopup();
                xlAccess.addOrUpdateNotes(HomeActivity.url,user,note,uniqueIdNew,title,new XlAccess.DataCallback() {
                    @Override
                    public void onSuccess(String response) {
                        //notesAdapter.notifyItemRemoved(position);
                        // Handle the response and update the UI (on the main thread)
                        Log.d(Tag,"Success:");
                        dismissLoaderPopup();

                        Intent intent = new Intent(view.getContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("fromNote","Y");
                        startActivity(intent);
                        Toast.makeText(getContext(), "Note added", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle error (on the main thread)
                        dismissLoaderPopup();
                        Log.d(Tag,"Fail:"+e.getMessage());
                    }
                });
            }else{
                Toast.makeText(getActivity(), "Note cannot be empty!", Toast.LENGTH_SHORT).show();
            }
            //dialog.dismiss();
        });
    }



    private void editNoteDialog(int position) {
        Notes note = notes.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.update_notes_popup, null);
        builder.setView(dialogView);

        EditText titleEditText = dialogView.findViewById(R.id.titleUpdate);
        titleEditText.setText(note.getTitle());
        EditText noteEditText = dialogView.findViewById(R.id.notesUpdate);
        noteEditText.setText(note.getNotes());
        TextView adminInfo = dialogView.findViewById(R.id.adminView);

        adminInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminInfo.setText(getString(R.string.adminInfo));
            }
        });

        AlertDialog dialog = builder.create();
        Button deleteDone = dialogView.findViewById(R.id.deleteDone);
        deleteDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showLoaderPopup();
                xlAccess.deletenotes(HomeActivity.url, note.getUserName(), note.getUniqueId(), new XlAccess.DataCallback() {
                    @Override
                    public void onSuccess(String response) {
                        notesAdapter.notifyItemRemoved(position);
                        // Handle the response and update the UI (on the main thread)
                        Log.d(Tag,"Success:");
                        dismissLoaderPopup();

                        Intent intent = new Intent(getContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("fromNote","Y");
                        startActivity(intent);
                        Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle error (on the main thread)
                        dismissLoaderPopup();
                        Log.d(Tag,"Fail:"+e.getMessage());
                    }
                });
            }
        });
        Button updateDone = dialogView.findViewById(R.id.updateDone);
        updateDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showLoaderPopup();
                String updatedContent = noteEditText.getText().toString();
                note.setNotes(updatedContent);
                String updatedTitle = titleEditText.getText().toString();
                note.setTitle(updatedTitle);

                // Update the server or shared preferences
                xlAccess.addOrUpdateNotes(HomeActivity.url, note.getUserName(), updatedContent, note.getUniqueId(),note.getTitle(),new XlAccess.DataCallback() {
                    @Override
                    public void onSuccess(String response) {
                        notesAdapter.notifyItemRemoved(position);
                        // Handle the response and update the UI (on the main thread)
                        Log.d(Tag,"Success:");
                        dismissLoaderPopup();

                        Intent intent = new Intent(getContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("fromNote","Y");
                        startActivity(intent);
                        Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle error (on the main thread)
                        dismissLoaderPopup();
                        Log.d(Tag,"Fail:"+e.getMessage());
                    }
                });
            }
        });
        dialog.show();
       // builder.show();
    }


    private void showLoaderPopup() {
        // Create the dialog
        if (null == loaderDialog || !loaderDialog.isShowing()) {
            loaderDialog = new Dialog(view.getContext());
            loaderDialog.setContentView(R.layout.popup_loader);
            LinearLayout reloadValuesLayout = loaderDialog.findViewById(R.id.loadValues);
            reloadValuesLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(view.getContext(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("fromNote","Y");
                    startActivity(intent);
                }
            });

            Objects.requireNonNull(loaderDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loaderDialog.setCancelable(false);
            try{
                loaderDialog.show();
            } catch (Exception e) {
                Log.v(Tag,"DialogLoaderError"+e.getMessage());
            }
        }
    }

    public void onDestroy() {
        if (loaderDialog != null && loaderDialog.isShowing()) {
            loaderDialog.dismiss();
        }
        super.onDestroy();
    }

    private void dismissLoaderPopup() {
        if (loaderDialog != null && loaderDialog.isShowing()) {
            try{
                loaderDialog.dismiss();
            } catch (Exception e) {
                Log.v(Tag,"DialogLoaderError"+e);
            }
        }
    }


    public void pageReload(){
        Intent intent = new Intent(view.getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("fromNote","Y");
        startActivity(intent);
    }

}