package com.example.taskifygo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.taskifygo.remind.NetworkChangeReceiver;
import com.example.taskifygo.remind.ReminderReceiver;
import com.example.taskifygo.remind.XlAccess;
import com.example.taskifygo.util.Task;
import com.example.taskifygo.util.TaskResponse;
import com.example.taskifygo.util.UserData;
import com.example.taskifygo.util.UserListResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import pl.droidsonroids.gif.GifImageView;

public class HomeActivity extends AppCompatActivity {
    public static String Tag = "HomeActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    public static String url = "https://script.google.com/macros/s/AKfycbwY3c6SFJ_7qodV4ntYmNF5LxvLbAosyhXrPrVZQsemzSXZayJjiuinIXoa7GWvbln_/exec";
    private Dialog loaderDialog;
    private NetworkChangeReceiver networkChangeReceiver;
    XlAccess networkOperation = new XlAccess();
    int selectedYearG,selectedMonthG,selectedDayG,selectedHourG,selectedMinuteG;
    List<Task> tasks = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        // Dynamically register the receiver for network change
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister receiver when the activity is no longer visible
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    //Home
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.Home) {
                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }else if (itemId == R.id.Notes) {
                    replaceFragment(new NotesFragment());
                    return true;
//                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    return true;
                }
                return false;
            }
        });

        if(getIntent().hasExtra("fromNote")){
            String data = getIntent().getStringExtra("fromNote");
            System.out.println("notifyCLick:"+data);
            if(("Y").equals(data)){
                bottomNavigationView.setSelectedItemId(R.id.Notes);
                replaceFragment(new NotesFragment());
            }

        }else{

            showLoaderPopup();
            Log.v("HOMEHOME","HOMEHOME");
            //password check
            SharedPreferences sharedPreferences = getSharedPreferences("TaskifyGo@001", MODE_PRIVATE);
            String storedUser = sharedPreferences.getString("AppUser", null);
            String storedPass = sharedPreferences.getString("AppPass", null);
            checkCredentials(storedUser,storedPass);

            this.getWindow().setStatusBarColor(this.getResources().getColor(R.color.white));
            toggleReceiver(true);

            TableLayout tableLayout = findViewById(R.id.tableLayout);//Homeview
            networkOperation.getDataFromServer(url, new XlAccess.DataCallback() {
                @Override
                public void onSuccess(String response) {
                    // Handle the response and update the UI (on the main thread)
                    runOnUiThread(() -> {
                        try {
                            dismissLoaderPopup();
                            addRows(tableLayout,response);
                        } catch (Exception e) {
                            Log.e(Tag,e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle error (on the main thread)
                    runOnUiThread(() -> {
                        try {
                            addRows(tableLayout,"fetching Error");
                        } catch (Exception ex) {
                            Log.e(Tag,e.getMessage());
                        }

                    });
                }
            });


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSION);
                }
                // Permission already granted
                //else Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            }

            Log.i(Tag,"Home started");



        }

        FloatingActionButton floatButton = findViewById(R.id.ovalButton);
        floatButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.i(Tag,"Home started2");
                return logout();
            }
        });
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedItemId = bottomNavigationView.getSelectedItemId();
                if(R.id.Home == selectedItemId){
                    reminderDialogBox("", "", "", "", 0);
                }else if(R.id.Notes == selectedItemId){
                    //NotesFragment.notesDialogBox();
                    NotesFragment notesFragment = new NotesFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("click", true);
                    notesFragment.setArguments(bundle);
                    replaceFragment(notesFragment);
                }
                Log.e(Tag,"selectedItemId: "+selectedItemId);
            }
        });
    }


    private boolean logout() {
        // Show a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Application logout")
                .setMessage("bye bye ?")
                .setIcon(android.R.drawable.ic_dialog_alert)  // Optional: Add an icon to the dialog
                .setCancelable(false)  // Prevent the dialog from closing on outside touch
                .setPositiveButton("logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences("TaskifyGo@001", MODE_PRIVATE);
                        String storedUser = sharedPreferences.getString("AppUser", null);

                        if(storedUser != null){
                            sharedPreferences.edit().clear().apply();
                            if (tasks.isEmpty()) {
                                System.out.println("No tasks to process.");
                            } else {
                                tasks.stream().filter(data->data.getStatus().equals("Y")).forEach(data->cancelReminder(data.getUniqueId(),data.getTaskDetails(),data.getUserName(),false));
                            }
                        }
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Log.v(Tag,"logout successFull.! "+storedUser);
                    }
                })
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = 750;  // Set width to 1000px, adjust as necessary
        dialog.getWindow().setAttributes(params);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);

        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(getResources().getColor(R.color.black));  // Set your desired color
        }
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(getResources().getColor(R.color.black));  // Set your desired color
        }
        return true; // Indicate that the long click was handled
    }

    private void checkCredentials(String storedUser,String storedPass) {
        if(storedUser != null && storedPass != null){
            networkOperation.validCredentials(HomeActivity.url,storedUser,storedPass, new XlAccess.DataCallback() {
                @Override
                public void onSuccess(String response) {
                    // Handle the response and update the UI (on the main thread)
                    runOnUiThread(() -> {
                        try {
                            Log.v(Tag,response);
                            if(!response.contains("Success")){
                                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("validMessage",response);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            Log.v(Tag,"Error:"+e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle error (on the main thread)
                    runOnUiThread(() -> {
                        try {
                            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("validMessage","Please contact admin !");
                            startActivity(intent);
                        } catch (Exception ex) {
                            Log.v(Tag,"Error:"+e.getMessage());
                        }

                    });
                }
            });
        }else{
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("validMessage","Enter credentials !");
            startActivity(intent);
        }
    }

    public void replaceFragment(NotesFragment notesFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,notesFragment);
        fragmentTransaction.commit();
    }

    private void addRows(TableLayout tableLayout, String response) {

        SharedPreferences sharedPreferences = getSharedPreferences("TaskifyGo@001", MODE_PRIVATE);
        String storedUser = sharedPreferences.getString("AppUser", null);
        Log.d(Tag, "AddTask");
        Gson gson = new Gson();
        Log.v("Guna",response);
        if(!response.contains("Error")){
            TaskResponse responseObj = gson.fromJson(response, TaskResponse.class);
            tasks = responseObj.getTasks();
            try {
                tasks = tasks.stream().filter(data->data.getUserName().equals(storedUser)).collect(Collectors.toList());
            }catch (Exception e){Log.d(Tag, "Error in filter user task");}
        }
        if(!tasks.isEmpty()){
            for (int i = 0; i < tasks.size(); i++) {  // Add 5 rows (can change the number)

                TableRow tableRow = new TableRow(this);
                tableRow.setPadding(0, 0, 0, 30);  // 20px padding at the bottom of each row
                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                LinearLayout verticalLinearLayout = new LinearLayout(this);
                verticalLinearLayout.setOrientation(LinearLayout.VERTICAL); // Vertical orientation for the first layout
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,6); // Width 100px for first layout
                verticalLinearLayout.setLayoutParams(params);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);  // You can also use oval, ring, etc.
                drawable.setCornerRadius(20);  // Set corner radius (e.g., 20px for rounded corners)
                drawable.setColor(getResources().getColor(R.color.transparent));
                drawable.setStroke(4, getResources().getColor(R.color.ic_taskifygo_background)); // Border width and color
                verticalLinearLayout.setBackground(drawable);

                String uniqueId = tasks.get(i).getUniqueId();
                String taskDetail = tasks.get(i).getTaskDetails();
                String username = tasks.get(i).getUserName();
                String time = tasks.get(i).getTimeTime();
                String frequency = tasks.get(i).getFrequency();
                String status = tasks.get(i).getStatus();

                SimpleDateFormat dateForma = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                dateForma.setTimeZone(TimeZone.getTimeZone("UTC")); // Parse the input as UTC
                Date setAlarmDat = null;
                try {
                    setAlarmDat = dateForma.parse(time);
                } catch (ParseException e) {
                    Log.v(Tag,"Error in conversion:"+e.getMessage());
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                if (setAlarmDat != null) {
                    calendar.setTime(setAlarmDat);
                }

                Log.e(Tag,calendar.getTimeInMillis()+","+System.currentTimeMillis());
                if(calendar.getTimeInMillis()<System.currentTimeMillis() && status.equals("Y")){
                    networkOperation.changeStatusToServer(url,uniqueId,username,taskDetail,"N");
                    status = "N";
                }
                System.out.println("GET - SwitchOtherTimeCalender: "+calendar.getTime());
                System.out.println("TaskDetail:"+taskDetail);

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy h:mm a");
                outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                String formattedTime = outputFormat.format(setAlarmDat);

                LinearLayout horizontalLayout1 = new LinearLayout(this);
                horizontalLayout1.setOrientation(LinearLayout.HORIZONTAL); // Horizontal orientation for the second layout
                TableRow.LayoutParams param1 = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT); // Fill remaining space for horizontal layout
                horizontalLayout1.setLayoutParams(param1);

                GradientDrawable drawabl = new GradientDrawable();
                drawabl.setShape(GradientDrawable.RECTANGLE);  // You can also use oval, ring, etc.
                drawabl.setCornerRadius(20);  // Set corner radius (e.g., 20px for rounded corners)
                //drawabl.setColor(getResources().getColor(R.color.red));
                horizontalLayout1.setBackground(drawabl);

                TextView textView = new TextView(this);
                TextView textView2 = new TextView(this);
                TextView textView3 = new TextView(this);

                textView.setText("# " +taskDetail); // Dynamically set text
                textView.setTextColor(getResources().getColor(R.color.black));  // Text color
                textView.setPadding(30, 10, 10, 20); // Padding
                textView.setTextSize(25); // Text size14
                //textView.setHeight(200);
                TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,7); // Weight = 4
                textView.setLayoutParams(params1);
                textView.setGravity(Gravity.LEFT);

                GradientDrawable drawable2 = new GradientDrawable();
                drawable2.setShape(GradientDrawable.RECTANGLE);  // You can also use oval, ring, etc.
                drawable2.setCornerRadius(20);  // Set corner radius (e.g., 20px for rounded corners)
                drawable2.setColor(getResources().getColor(R.color.green));
                //textView.setBackground(drawable2);
                horizontalLayout1.addView(textView);

                SwitchMaterial switchMaterial = new SwitchMaterial(this);
                switchMaterial.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.lavender)));
                switchMaterial.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black))); // Change to your desired color
                TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1); // Weight = 4
                switchMaterial.setLayoutParams(params2);
                switchMaterial.setPadding(10, 10, 10, 0); // Padding
                switchMaterial.setId(View.generateViewId());  // Generates a unique ID for the TextView

                if(status != null && status.equals("Y")){
                    switchMaterial.setChecked(true);
                    switchMaterial.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.lightBlue))); // Change to your desired color
                    drawable.setColor(getResources().getColor(R.color.bgBottomBar));
                    drawable.setStroke(4, getResources().getColor(R.color.transparent)); // Border width and color
                    textView2.setTextColor(getResources().getColor(R.color.white));
                    textView3.setTextColor(getResources().getColor(R.color.white));  // Text color
                    verticalLinearLayout.setBackground(drawable);
                }
                switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // The switch is checked.
                            // You can add your logic here when the switch is on (checked)
                            Log.d("Switch State", "Switch is ON");

                            reminderDialogBox(uniqueId,username,taskDetail,frequency,switchMaterial.getId());

                            /*if(frequency.equals("Once")){
                                dialogBox(username,taskDetail,frequency,switchMaterial.getId());
                            }else{

                                if(calendar.getTimeInMillis()>System.currentTimeMillis()){
                                    setReminder(username,taskDetail,calendar,frequency);
                                    networkOperation.changeStatusToServer(url,username,taskDetail,"Y");
                                    drawable.setColor(getResources().getColor(R.color.lavender));
                                    textView.setTextColor(getResources().getColor(R.color.white));
                                    textView2.setTextColor(getResources().getColor(R.color.white));
                                    textView3.setTextColor(getResources().getColor(R.color.white));  // Text color
                                    verticalLinearLayout.setBackground(drawable);
                                }else{
                                    dialogBox(username,taskDetail,frequency,switchMaterial.getId());
                                }
                            }*/
                        } else {
                            cancelReminder(uniqueId,taskDetail,username,false);
                            drawable.setStroke(4, getResources().getColor(R.color.ic_taskifygo_background)); // Border width and color
                            switchMaterial.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(HomeActivity.this,R.color.black))); // Change to your desired color
                            drawable.setColor(getResources().getColor(R.color.transparent));
                            textView.setTextColor(getResources().getColor(R.color.black));
                            textView2.setTextColor(getResources().getColor(R.color.black));
                            textView3.setTextColor(getResources().getColor(R.color.black));  // Text color
                            verticalLinearLayout.setBackground(drawable);
                            // The switch isn't checked.
                            // You can add your logic here when the switch is off (unchecked)
                            Log.d("Switch State", "Switch is OFF");
                        }
                    }
                });
                horizontalLayout1.addView(switchMaterial);
                verticalLinearLayout.addView(horizontalLayout1);


                LinearLayout horizontalLayout = new LinearLayout(this);
                horizontalLayout.setOrientation(LinearLayout.HORIZONTAL); // Horizontal orientation for the second layout
                TableRow.LayoutParams param = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT); // Fill remaining space for horizontal layout
                horizontalLayout.setLayoutParams(param);

                textView2.setText("Remind : "+frequency); // Dynamically set text
                textView2.setTextColor(getResources().getColor(R.color.black));  // Text color
                textView2.setPadding(30, 10, 10, 10); // Padding
                textView2.setTextSize(15); // Text size14
                textView2.setHeight(160);
                TableRow.LayoutParams param2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1); // Weight = 4
                textView2.setLayoutParams(param2);
                textView2.setGravity(Gravity.LEFT);
                horizontalLayout.addView(textView2);

                textView3.setText(formattedTime); // Dynamically set text
                textView3.setTextColor(getResources().getColor(R.color.black));  // Text color
                textView3.setPadding(10, 10, 10, 10); // Padding
                textView3.setTextSize(16); // Text size14
                textView3.setHeight(160);
                TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1); // Weight = 4
                textView3.setLayoutParams(params3);
                textView3.setGravity(Gravity.CENTER);
                horizontalLayout.addView(textView3);
                //horizontalLayout.setBackgroundColor(getColor(R.color.columnGreen));

                verticalLinearLayout.addView(horizontalLayout);
                tableRow.addView(verticalLinearLayout);

                int sno = tasks.get(i).getSno();
                String user = tasks.get(i).getUserName();
                tableRow.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // Show a confirmation dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Delete Task")
                                .setMessage("Do you want to delete this task?")
                                .setIcon(android.R.drawable.ic_dialog_alert)  // Optional: Add an icon to the dialog
                                .setCancelable(false)  // Prevent the dialog from closing on outside touch
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Remove the TableRow from the TableLayout
                                        finish();
                                        startActivity(getIntent());//Reload
                                        tableLayout.removeView(tableRow);
                                        cancelReminder(uniqueId,taskDetail,username,true);
                                        networkOperation.deleteDataFromServer(url,user,sno,new XlAccess.DataCallback() {
                                            @Override
                                            public void onSuccess(String response) {
                                                // Handle the response and update the UI (on the main thread)
                                                runOnUiThread(() -> {
                                                    try {
                                                        Toast.makeText(HomeActivity.this, response, Toast.LENGTH_SHORT).show();

                                                    } catch (Exception e) {
                                                        Log.d(Tag,"Error:"+e.getMessage());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                // Handle error (on the main thread)
                                                runOnUiThread(() -> {
                                                    try {
                                                        Toast.makeText(HomeActivity.this, response, Toast.LENGTH_SHORT).show();
                                                    } catch (Exception ex) {
                                                        Log.d(Tag,"Error:"+e.getMessage());
                                                    }

                                                });
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        Objects.requireNonNull(dialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = 750;  // Set width to 1000px, adjust as necessary
                        dialog.getWindow().setAttributes(params);
                        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);

                        TextView titleView = dialog.findViewById(android.R.id.title);
                        if (titleView != null) {
                            titleView.setTextColor(getResources().getColor(R.color.black));  // Set your desired color
                        }
                        TextView messageView = dialog.findViewById(android.R.id.message);
                        if (messageView != null) {
                            messageView.setTextColor(getResources().getColor(R.color.black));  // Set your desired color
                        }
                        return true; // Indicate that the long click was handled
                    }
                });

                tableLayout.addView(tableRow);
            }

            //scroll bottom space
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(0, 0, 0, 30);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            TextView textView3 = new TextView(this);
            textView3.setHeight(500);
            tableRow.addView(textView3);
            tableLayout.addView(tableRow);

        }

    }

    private void reminderDialogBox(String uniqueId,String user,String taskDetails,String frequency,int switchId){
        Log.i(Tag,"Popup started");

        ConstraintLayout layout = findViewById(R.id.addTaskPopupLayout);
        View v = LayoutInflater.from(HomeActivity.this).inflate(R.layout.addtask_popup,layout);

        TextView adminInfo = v.findViewById(R.id.textView);
        EditText usernameEditText = v.findViewById(R.id.editTextText);
        EditText  taskDetailsEditText = v.findViewById(R.id.editText2);
        TimePicker timePicker = v.findViewById(R.id.timePicker);
        Spinner reminderFrequencySpinner = v.findViewById(R.id.reminderFrequency);
        TextView resultTextView = v.findViewById(R.id.textView);
        Button button = v.findViewById(R.id.successDone);
        DatePicker datePicker = v.findViewById(R.id.datePicker);
        datePicker.setVisibility(View.INVISIBLE);


        adminInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminInfo.setText(getString(R.string.adminInfo));
            }
        });

        boolean isNightMode = (this.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        /*if (!isNightMode) {
            usernameEditText.setTextColor(Color.BLACK);
            taskDetailsEditText.setTextColor(Color.BLACK);
        }*/

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.reminder_frequencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderFrequencySpinner.setAdapter(adapter);

        //default user - login user
        SharedPreferences sharedPreferences = getSharedPreferences("TaskifyGo@001", MODE_PRIVATE);
        String storedUser = sharedPreferences.getString("AppUser", null);
        if(storedUser!=null){
            usernameEditText.setText(storedUser);
            usernameEditText.setEnabled(false);
        }

        if(switchId!=0){
            usernameEditText.setText(user);
            usernameEditText.setEnabled(false);
            taskDetailsEditText.setText(taskDetails);
            taskDetailsEditText.setEnabled(false);
            ArrayAdapter<String> adapterNew = (ArrayAdapter<String>) reminderFrequencySpinner.getAdapter();
            int position = adapterNew.getPosition(frequency); // Get the position of the value in the adapter
            reminderFrequencySpinner.setSelection(position);
        }

        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));;
        int onceYear = calendar.get(Calendar.YEAR);
        int onceMonth = calendar.get(Calendar.MONTH);
        int onceDay = calendar.get(Calendar.DAY_OF_MONTH);
        int onceHour = calendar.get(Calendar.HOUR_OF_DAY);
        int onceMinute = calendar.get(Calendar.MINUTE);

        reminderFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String reminderFrequency = reminderFrequencySpinner.getSelectedItem().toString();

                // Check if "Once" is selected
                if (reminderFrequency.equals("Once")) {
                    timePicker.setVisibility(View.GONE);
                    datePicker.setVisibility(View.GONE);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            HomeActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                    // Create and show the TimePickerDialog
                                    selectedYearG = selectedYear;
                                    selectedMonthG = selectedMonth;
                                    selectedDayG = selectedDay;
                                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                                            HomeActivity.this,
                                            new TimePickerDialog.OnTimeSetListener() {
                                                @Override
                                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                                    // Handle the selected time
                                                    selectedHourG = selectedHour;
                                                    selectedMinuteG = selectedMinute;
                                                    String selectedTime = selectedHour + ":" + selectedMinute;
                                                    Toast.makeText(HomeActivity.this, selectedTime, Toast.LENGTH_SHORT).show();

                                                }
                                            },
                                            onceHour, onceMinute, false
                                    );
                                    timePickerDialog.setTitle("Select Time");
                                    timePickerDialog.show();
                                }
                            },
                            onceYear, onceMonth, onceDay
                    );
                    datePickerDialog.setTitle("Select Date");
                    datePickerDialog.show();
                    if(switchId!=0) {
                        reminderFrequencySpinner.setEnabled(false);
                    }
                } else if(!reminderFrequency.equals("When you need")){
                    if(switchId!=0) {
                        reminderFrequencySpinner.setEnabled(false);
                    }
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.VISIBLE);
                } else {
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Toast.makeText(HomeActivity.this, "nothing.", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setView(v);
        final AlertDialog alertDialog = builder.create();

        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UnsafeIntentLaunch")
            @Override
            public void onClick(View view) {
                String reminderFrequency = reminderFrequencySpinner.getSelectedItem().toString();

                Log.i(Tag,"task submit button");
                String username = usernameEditText.getText().toString().trim();
                String taskDetails = taskDetailsEditText.getText().toString().trim();

                // Validation checks
                if (username.isEmpty() || taskDetails.isEmpty() || reminderFrequency.equals("When you need")) {
                    Toast.makeText(HomeActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                } else {

                    int hour = 0;
                    int minute = 0;
                    if (!reminderFrequency.equals("Once")) {
                        hour = timePicker.getCurrentHour();
                        minute = timePicker.getCurrentMinute();
                        System.out.println("OnceTime"+hour+":"+minute);
                    }else{
                        hour = selectedHourG;
                        minute = selectedMinuteG;
                        System.out.println("OtherTime"+hour+":"+minute);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                            Toast.makeText(HomeActivity.this, "Exact alarm permission required. Please enable in settings.", Toast.LENGTH_LONG).show();
                            requestExactAlarmPermission();
                            return;
                        }
                    }

                    if (reminderFrequency.equals("Once")) {

                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                        System.out.println("Selected Time: " + selectedHourG+":"+selectedMinuteG);
                        selectedDate.set(selectedYearG, selectedMonthG, selectedDayG, selectedHourG, selectedMinuteG, 0);
                        System.out.println("OnceTimeCalender"+selectedDate.getTime());

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        String formattedSelectedTime = sdf.format(selectedDate.getTime());
                        System.out.println("Selected Time: " + formattedSelectedTime);

                        Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
                        long currentTimeInMillis = currentCalendar.getTimeInMillis();

                        if (selectedDate.getTimeInMillis() < currentTimeInMillis) {
                            Toast.makeText(HomeActivity.this, "Selected time is in the past. Please choose a future time.", Toast.LENGTH_SHORT).show();
                            reminderFrequencySpinner.setSelection(0);  // 0 is the index of the first item in the spinner
                            return;
                        } else if (selectedDate.getTimeInMillis() > currentTimeInMillis) {
                            System.out.println("The selected time is in the future.");
                        }

                        setReminder(uniqueId,username,taskDetails, selectedDate, reminderFrequency);

                    }else {

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);
                        System.out.println("OtherTimeCalender"+calendar.getTime());

                        // Check if the time is in the past, adjust to the next day
                        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                        }

                        setReminder(uniqueId,username,taskDetails, calendar, reminderFrequency);

                    }
                    // Display the collected data (For demonstration purposes)
                    String reminderTime = hour + ":" + (minute < 10 ? "0" + minute : minute);
                    String result = "Username: " + username + "\n" +
                            "Task: " + taskDetails + "\n" +
                            "Reminder Time: " + reminderTime + "\n" +
                            "Frequency: " + reminderFrequency;

                    resultTextView.setText(result);
                    alertDialog.dismiss();
                    finish();
                    startActivity(getIntent());//Reload
                }
            }
        });

        if(alertDialog.getWindow()!=null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        // Set on cancel listener (if needed)
        alertDialog.setOnCancelListener(dialogInterface -> {
            // The dialog was canceled (e.g., back press)
            Log.d("DialogStatus", "Dialog canceled.");
            if(switchId!=0){
                SwitchMaterial switchMaterial = findViewById(switchId);
                switchMaterial.setChecked(false);
            }
        });
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can send notifications
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showLoaderPopup() {
        // Create the dialog
        if (null == loaderDialog || !loaderDialog.isShowing()) {
            loaderDialog = new Dialog(this);
            loaderDialog.setContentView(R.layout.popup_loader);
            LinearLayout reloadValuesLayout = loaderDialog.findViewById(R.id.loadValues);
            reloadValuesLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    startActivity(getIntent());//Reload
                    //Toast.makeText(HomeActivity.this, "Reload clicked!", Toast.LENGTH_SHORT).show();
                }
            });

            Objects.requireNonNull(loaderDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loaderDialog.setCancelable(false);
            if (!isFinishing() && !isDestroyed()) {
                try{
                    loaderDialog.show();
                } catch (Exception e) {
                    Log.v(Tag,"DialogLoaderError"+e.getMessage());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
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

    private void toggleReceiver(boolean enable) {
        ComponentName receiver = new ComponentName(this, ReminderReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void setReminder(String uniqueId,String username,String taskDetails, Calendar calendar, String frequency) {
        // Create a calendar object for the reminder time
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("taskDetails", taskDetails);
        intent.putExtra("frequency", frequency); // Pass frequency to the receiver
        intent.putExtra("username", username); // Pass frequency to the receiver
        intent.putExtra("uniqueId", uniqueId);

        Date nextTriggerDate = new Date(calendar.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String formattedDateTime = sdf.format(nextTriggerDate);

        XlAccess networkOperation = new XlAccess();
        networkOperation.sendDataToServer(url,uniqueId,username,taskDetails,formattedDateTime,frequency);

        int requestCode = taskDetails.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            //alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            //Toast.makeText(this, "Reminder", Toast.LENGTH_SHORT).show();
            Log.i(Tag, "Alarm scheduled: " + formattedDateTime);
        }
    }
    private void cancelReminder(String uniqueId,String taskDetails,String username,Boolean isdelete) {
        if(!isdelete){//if pause task
            networkOperation.changeStatusToServer(url,uniqueId,username,taskDetails,"N");
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Create an intent with the same taskDetails and frequency as the original
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("taskDetails", taskDetails);

        // Use the same request code as when setting the alarm
        int requestCode = taskDetails.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            // Cancel the alarm
            alarmManager.cancel(pendingIntent);
            Log.i("Reminder", "Alarm canceled for task: " + taskDetails);
        }
    }
}

