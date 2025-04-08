package com.example.startxplanify.Notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.startxplanify.R;

public class NotificationHelper {

    private Context context;
    private static final String CHANNEL_ID = "task_notifications";

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Notifications for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Planifier la notification à 1 minute avant
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleReminder1mNotification(String taskTitle, long triggerTime) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("notification_type", "reminder_1m");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }


    // Planifier la notification à 24 heures avant
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleReminder24hNotification(String taskTitle, long triggerTime) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("notification_type", "reminder_24h");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    // Planifier la notification à 1 heure avant
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleReminder1hNotification(String taskTitle, long triggerTime) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("notification_type", "reminder_1h");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    // Envoyer une notification lorsque la tâche est terminée
    public void sendCompletedNotification(String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Completed!")
                .setContentText("The task '" + taskTitle + "' has been completed. Congratulations!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    // Envoyer un rappel 24 heures avant
    public void sendReminder24hNotification(String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Reminder: Task Deadline Approaching!")
                .setContentText("There are only 24 hours left to complete the task '" + taskTitle + "'. Don't forget to finish it!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Envoyer un rappel 1 heure avant
    public void sendReminder1hNotification(String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Last Chance: Task Ending Soon!")
                .setContentText("Only 1 hour left to finish the task '" + taskTitle + "'. Hurry up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    public void sendNotification(String taskTitle, String notificationType) {
        if (notificationType.equals("completed")) {
            sendCompletedNotification(taskTitle);
        } else if (notificationType.equals("reminder_24h")) {
            sendReminder24hNotification(taskTitle);
        } else if (notificationType.equals("reminder_1h")) {
            sendReminder1hNotification(taskTitle);
        }
    }

}
