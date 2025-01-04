package com.example.taskifygo;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskifygo.remind.NetworkChangeReceiver;
import com.example.taskifygo.util.NetworkUtil;

public class MainActivity extends AppCompatActivity {
    public static String Tag = "MainActivity";
    private NetworkChangeReceiver networkChangeReceiver;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.i(Tag,"APP started");
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_local_Store), MODE_PRIVATE);
        String storedUser = sharedPreferences.getString("AppUser", null);
        String storedPass = sharedPreferences.getString("AppPass", null);
        Log.v(Tag,storedPass+storedUser);

        if(getIntent().hasExtra("notify")){
            String data = getIntent().getStringExtra("notify");
            System.out.println("notifyCLick:"+data);
            //replaceFragment(new NotesFragment());
        }

        //sharedPreferences.edit().clear().apply();
        Intent intent;
        if (NetworkUtil.isNetworkAvailable(this)) {
            // Perform network operation
            Log.d("NetworkCheck", "Network is available");
            if (storedUser == null && storedPass == null) {
                Log.i(Tag,"Login go");
                intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Log.i(Tag,"Home go");
                intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}