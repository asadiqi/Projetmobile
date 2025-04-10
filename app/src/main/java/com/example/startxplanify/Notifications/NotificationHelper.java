package com.example.startxplanify.Notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.startxplanify.R;

public class NotificationHelper {

    private final Context context;
    private static final String CHANNEL_ID = "task_notifications";
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Notifications for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleReminderNotification(String taskTitle, long triggerTime, String type, String userId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("notification_type", type);
        intent.putExtra("task_user_id", userId); // Ajout de l'ID utilisateur

        int requestCode = (taskTitle + type + triggerTime).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }


    public void sendNotification(String taskTitle, String notificationType) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification1)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        switch (notificationType) {
            case "completed":
                builder.setContentTitle("Task Completed! 🎉")
                        .setContentText("Congratulations! The task '" + taskTitle + "' has been successfully completed. Well done!");
                break;
            case "reminder_24h":
                builder.setContentTitle("⏳ 24-Hour Reminder")
                        .setContentText("Just a friendly reminder: Only 24 hours left to complete the task '" + taskTitle + "'. Time to wrap it up!");
                break;
            case "reminder_1h":
                builder.setContentTitle("⏰ 1-Hour Reminder")
                        .setContentText("You’re almost there! Only 1 hour left to finish '" + taskTitle + "'. Let’s do this!");
                break;
            case "reminder_1m":
                builder.setContentTitle("⚡ 1-Minute Reminder!")
                        .setContentText("Final countdown! Just 1 minute left to complete '" + taskTitle + "'. Hurry up!");
                break;
            default:
                return;
        }


        int notificationId = (taskTitle + notificationType).hashCode();
        notificationManager.notify(notificationId, builder.build());
    }
}
