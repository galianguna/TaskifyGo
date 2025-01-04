package com.example.taskifygo.remind;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.taskifygo.HomeActivity;
import com.example.taskifygo.MainActivity;
import com.example.taskifygo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    XlAccess networkOperation = new XlAccess();

    @Override
    public void onReceive(Context context, Intent intent) {

        //if (null != intent.getAction() && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        long millis = System.currentTimeMillis();
        String taskDetails = intent.getStringExtra("taskDetails");
        String frequency = intent.getStringExtra("frequency");
        String userName = intent.getStringExtra("username");
        String uniqueId = intent.getStringExtra("uniqueId");

        if (taskDetails == null) {
            Log.e(TAG, "Task details missing. Cannot show notification.");
            return;
        }

        Intent i = new Intent(context,MainActivity.class);
        i.putExtra("notify","send");
        PendingIntent pi = PendingIntent.getActivity(context,78,i,PendingIntent.FLAG_IMMUTABLE);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("reminderChannel", "Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, "reminderChannel")
                .setContentTitle("Dalo Reminder")
                .setContentText(taskDetails)
                .setSmallIcon(R.drawable.baseline_adb_24)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(taskDetails.hashCode(), notification);

        // Reschedule the alarm for the next occurrence
        if (frequency != null) {
            long interval = getIntervalForFrequency(frequency);

            if (interval > 0) {
                Log.v("Alarm","showing");
                rescheduleAlarm(context,uniqueId, userName, taskDetails, frequency, interval, millis);
            }else {
                networkOperation.changeStatusToServer(HomeActivity.url, uniqueId, userName, taskDetails, "N");
            }
            try{
                Intent launchIntent = new Intent(context, HomeActivity.class); // Replace with your main activity
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(launchIntent);
                Log.i(TAG, "App reloaded successfully.");
            }catch (Exception e){Log.v(TAG,"Error in reload."+e.getMessage());}
        }
    }

    private long getIntervalForFrequency(String frequency) {
        switch (frequency) {
            case "Daily":
                return AlarmManager.INTERVAL_DAY;
            case "Weekly":
                return AlarmManager.INTERVAL_DAY * 7;
            case "Monthly":
                return AlarmManager.INTERVAL_DAY * 30; // Approximation
            case "Yearly":
                return AlarmManager.INTERVAL_DAY * 365; // Approximation
            default:
                Log.e(TAG, "Unknown frequency: " + frequency);
                return 0;
        }
    }

    private void rescheduleAlarm(Context context,String uniqueId,String userName, String taskDetails, String frequency, long interval,long millis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
            }
        }

        Intent newIntent = new Intent(context, ReminderReceiver.class);
        newIntent.putExtra("taskDetails", taskDetails);
        newIntent.putExtra("frequency", frequency);
        newIntent.putExtra("username",userName);
        newIntent.putExtra("uniqueId",uniqueId);

        int uniqueRequestCode = taskDetails.hashCode();
        PendingIntent newPendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueRequestCode,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            long time = millis + interval;
            //alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, newPendingIntent);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, newPendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, millis, interval, newPendingIntent);

            Date nextTriggerDate = new Date(millis + interval);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String formattedDateTime = sdf.format(nextTriggerDate);
            Log.i(TAG, taskDetails+"-Alarm rescheduled for: " + formattedDateTime);

            networkOperation.updateReminderTime(HomeActivity.url,uniqueId,formattedDateTime);
        }
    }
}
