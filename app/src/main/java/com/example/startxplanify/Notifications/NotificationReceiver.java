package com.example.startxplanify.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        String notificationType = intent.getStringExtra("notification_type");

        // Création d'une instance de NotificationHelper pour gérer les notifications
        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (taskTitle != null && notificationType != null) {
            // Affichage de la notification selon le type
            switch (notificationType) {
                case "completed":
                    notificationHelper.sendCompletedNotification(taskTitle);
                    break;
                case "reminder_24h":
                    notificationHelper.sendReminder24hNotification(taskTitle);
                    break;
                case "reminder_1h":
                    notificationHelper.sendReminder1hNotification(taskTitle);
                    break;
                case "reminder_1m":
                    notificationHelper.sendReminder1hNotification(taskTitle); // Nouveau cas pour 1 minute avant
                    break;
                default:
                    break;
            }
        }
    }
}
