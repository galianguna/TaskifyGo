package com.example.taskifygo.remind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.view.Gravity;
import android.widget.Toast;

import com.example.taskifygo.HomeActivity;
import com.example.taskifygo.MainActivity;
import com.example.taskifygo.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static boolean isNetworkToastShown = false;
    private List<Boolean> netwokChange = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check the current network state
        if (NetworkUtil.isNetworkAvailable(context)) {
            // If network is available, reset the flag
            isNetworkToastShown = false;
            if(!netwokChange.contains(true)){
                netwokChange.add(true);
            }
        } else {
            // If network is not available, show the toast only if it's not already shown
            if (!isNetworkToastShown) {
                Toast t = Toast.makeText(context, "Check network connectivity", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, -200); // Gravity.CENTER positions it in the center; -200 moves it up
                t.show();
                isNetworkToastShown = true;
            }

            if(!netwokChange.contains(false)) {
                netwokChange.add(false);
            }
        }

        if(netwokChange.contains(true) && netwokChange.contains(false)){
            netwokChange.clear();
            Intent homeIntent = new Intent(context, MainActivity.class);
            context.startActivity(homeIntent);
        }
    }
}
