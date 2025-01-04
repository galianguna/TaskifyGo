package com.example.taskifygo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskifygo.remind.XlAccess;
import com.example.taskifygo.util.Task;
import com.example.taskifygo.util.TaskResponse;
import com.example.taskifygo.util.UserData;
import com.example.taskifygo.util.UserListResponse;
import com.google.gson.Gson;

import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    public static String Tag = "RegisterActivity";
    XlAccess networkOperation = new XlAccess();
    List<UserData> userData = new ArrayList<>();

    private Dialog loaderDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        showLoaderPopup();

        TextView button = findViewById(R.id.toLogin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        ImageView imageView = findViewById(R.id.arrowToLogin);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        EditText userInput = findViewById(R.id.inputUser);
        EditText passInput = findViewById(R.id.inputPassword);
        EditText emailInput = findViewById(R.id.inputEmailAddress);
        TextView textView = findViewById(R.id.viewValidate);

        networkOperation.getUserData(HomeActivity.url, new XlAccess.DataCallback() {
            @Override
            public void onSuccess(String response) {
                // Handle the response and update the UI (on the main thread)
                runOnUiThread(() -> {
                    try {
                        dismissLoaderPopup();
                        Gson gson = new Gson();
                        // Convert the string to JsonObject
                        Log.v("Guna",response);
                        if(!response.contains("Error")){
                            UserListResponse responseObj = gson.fromJson(response, UserListResponse.class);
                            userData = responseObj.getUserList();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error (on the main thread)
                runOnUiThread(() -> {
                    try {
                        //addRows(tableLayout,"fetching Error");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

                });
            }
        });

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInputValue = userInput.getText().toString().trim();
                String passInputValue = passInput.getText().toString().trim();
                String emailInputValue = emailInput.getText().toString().trim();

                Log.v(Tag,userInputValue+"-"+passInputValue+"-"+emailInputValue);
                if(!userInputValue.isBlank() && !passInputValue.isBlank() && !emailInputValue.isBlank() && emailInputValue.contains("@gmail.com")){
                    Boolean exist = false;
                    if(!userData.isEmpty()) {
                        exist = userData.stream().anyMatch(Data -> Data.getUserName().equals(userInputValue));
                    }
                    if(exist){
                        textView.setText(userInputValue+" - User already exist");
                    }else{
                        networkOperation.setUserData(HomeActivity.url,userInputValue,passInputValue,emailInputValue,"U");
                        textView.setText("Successfully created!\n"+userInputValue+"\n"+passInputValue+"\n"+emailInputValue);
                        findViewById(R.id.registerButton).setEnabled(false);
                    }
                }else{
                    Toast.makeText(RegisterActivity.this,"Please fill all fields.",Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                    Log.v(Tag,"DialogLoaderError"+e);
                }
            }
        }
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
}