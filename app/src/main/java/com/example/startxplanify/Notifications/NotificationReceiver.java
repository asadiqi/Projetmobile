package com.example.startxplanify.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.FirebaseAuth;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        String notificationType = intent.getStringExtra("notification_type");
        String taskUserId = intent.getStringExtra("task_user_id"); // Ajout de l'ID utilisateur de la tâche

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && taskUserId != null && taskUserId.equals(user.getUid())) {
            NotificationHelper helper = new NotificationHelper(context);
            helper.sendNotification(taskTitle, notificationType);
        }
    }
}
