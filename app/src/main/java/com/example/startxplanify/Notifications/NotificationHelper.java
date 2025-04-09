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
    public void scheduleReminderNotification(String taskTitle, long triggerTime, String type) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("notification_type", type);

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
                } else {
                    // Facultatif : alerte visuelle ou redirection vers les paramètres
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    public void sendNotification(String taskTitle, String notificationType) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        switch (notificationType) {
            case "completed":
                builder.setContentTitle("Task Completed!")
                        .setContentText("The task '" + taskTitle + "' has been completed.");
                break;
            case "reminder_24h":
                builder.setContentTitle("Reminder: 24h Left")
                        .setContentText("Only 24 hours left for '" + taskTitle + "'.");
                break;
            case "reminder_1h":
                builder.setContentTitle("1 Hour Reminder")
                        .setContentText("1 hour left to complete '" + taskTitle + "'.");
                break;
            case "reminder_1m":
                builder.setContentTitle("1 Minute Left!")
                        .setContentText("Hurry up! '" + taskTitle + "' is due in 1 minute.");
                break;
            default:
                return;
        }

        int notificationId = (taskTitle + notificationType).hashCode();
        notificationManager.notify(notificationId, builder.build());
    }
}
