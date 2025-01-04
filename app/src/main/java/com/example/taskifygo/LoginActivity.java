package com.example.taskifygo;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskifygo.remind.XlAccess;
import com.example.taskifygo.util.UserData;
import com.example.taskifygo.util.UserListResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    public static String Tag = "LoginActivity";
    XlAccess networkOperation = new XlAccess();
    List<UserData> userData = new ArrayList<>();
    private Dialog loaderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_local_Store), MODE_PRIVATE);
        EditText userInput = findViewById(R.id.userInput);
        EditText passInput = findViewById(R.id.passwordInput);
        TextView textView = findViewById(R.id.vValidate);

        String validMessage = getIntent().getStringExtra("validMessage");
        if (validMessage != null) {
            textView.setText(validMessage);
        }

        TextView button = findViewById(R.id.toRegister);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInputValue = userInput.getText().toString().trim();
                String passInputValue = passInput.getText().toString().trim();
                showLoaderPopup();
                Log.v(Tag,userInputValue+"-"+passInputValue);
                if(!userInputValue.isBlank() && !passInputValue.isBlank()){
                    networkOperation.validCredentials(HomeActivity.url,userInputValue,passInputValue, new XlAccess.DataCallback() {
                        @Override
                        public void onSuccess(String response) {
                            // Handle the response and update the UI (on the main thread)
                            runOnUiThread(() -> {
                                try {
                                    dismissLoaderPopup();
                                    Log.v(Tag,response);
                                    if(response.contains("Success")){
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("AppUser", userInputValue);
                                        editor.putString("AppPass", passInputValue); // Store a token instead of a password
                                        editor.apply();
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }else if(response.contains("-")){
                                        textView.setText(response);
                                    }else{
                                        textView.setText("Please enter valid credentials!");
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
                                    dismissLoaderPopup();
                                    textView.setText("Please contact admin !");
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }

                            });
                        }
                    });

                }else{
                    dismissLoaderPopup();
                    Toast.makeText(LoginActivity.this,"Please fill all fields.",Toast.LENGTH_SHORT).show();
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