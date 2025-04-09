package com.example.startxplanify.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        String notificationType = intent.getStringExtra("notification_type");

        if (taskTitle != null && notificationType != null) {
            NotificationHelper helper = new NotificationHelper(context);
            helper.sendNotification(taskTitle, notificationType);
        }
    }
}
