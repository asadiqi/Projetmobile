package com.example.startxplanify.Notes_Activity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;

import com.example.startxplanify.Models.PrivateTaskModel;
import com.example.startxplanify.Notifications.NotificationHelper;
import com.example.startxplanify.Notifications.NotificationReceiver;
import com.example.startxplanify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.PopupMenu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.app.NotificationManager;


public class Private_NoteActivity extends BaseNoteActivity {

    private Button buttonAddNote;
    private TextView textViewTaskEndDate;
    private LinearLayout taskContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initializeViews();
        checkExactAlarmPermission();
        initializeFirebase();
        setupDrawerAndNavigation();
        loadUserTasks();
        setupAddNoteButton();
        hideAddpointTexteView();
    }

    private void initializeViews() {
        buttonAddNote = findViewById(R.id.button_addNote);
        taskContainer = findViewById(R.id.taskContainer);
    }
    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    private void setupAddNoteButton() {
        buttonAddNote.setOnClickListener(v -> showAddPrivateTaskDialog());
    }

    private void hideAddpointTexteView() {
        TextView textView = findViewById(R.id.pointsTextView);
        textView.setVisibility(View.GONE);
    }

    private void loadUserTasks() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("private_tasks").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        taskContainer.removeAllViews();
                        if (queryDocumentSnapshots.isEmpty()) {
                            showToast("No tasks found.");
                        } else {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                PrivateTaskModel privateTask = doc.toObject(PrivateTaskModel.class);
                                if (privateTask != null) {
                                    boolean isCompleted = privateTask.isCompleted(); // Utilise `isCompleted`

                                    // Récupérer l'état du CheckBox dans SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("tasks_pref", MODE_PRIVATE);
                                    isCompleted = sharedPreferences.getBoolean(privateTask.getId(), isCompleted);

                                    addTaskToUI(privateTask.getTitle(), privateTask.getEndDate(), isCompleted, privateTask.getId());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error loading tasks"));
        }
    }
    private void showAddPrivateTaskDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_private_task, null);
        EditText privateTaskTitle = dialogView.findViewById(R.id.editprivateTasktitle);
        textViewTaskEndDate = dialogView.findViewById(R.id.textViewTaskEndDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Private Task")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateInput(privateTaskTitle, textViewTaskEndDate)) {
                createTask(privateTaskTitle.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        textViewTaskEndDate.setOnClickListener(v -> showDateTimePicker(textViewTaskEndDate));
    }
    private void createTask(String taskTitle) {
        String endDate = textViewTaskEndDate.getText().toString().trim();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String taskId = db.collection("private_tasks").document().getId();
            int notificationId = (int) System.currentTimeMillis();
            PrivateTaskModel task = new PrivateTaskModel(taskId, taskTitle, endDate, userId,false,notificationId);
            db.collection("private_tasks").document(taskId).set(task)
                    .addOnSuccessListener(aVoid -> {
                        View taskView = addTaskToUI(taskTitle, endDate, false,taskId);  // false pour indiquer qu'elle n'est pas terminée
                        taskView.setTag(taskId);
                        loadUserTasks();
                        showToast("Task Added");
                        // Convertir la date de fin en timestamp et planifier la notification
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        try {
                            Date date = dateFormat.parse(endDate);
                            if (date != null) {
                                long timeInMillis = date.getTime();
                                long now = System.currentTimeMillis();

                                NotificationHelper helper = new NotificationHelper(this);

                                long oneMinuteBefore = timeInMillis - 60_000;
                                long oneHourBefore = timeInMillis - 60 * 60_000;
                                long oneDayBefore = timeInMillis - 24 * 60 * 60_000;

                                if (oneDayBefore > now) {
                                    helper.scheduleReminderNotification(taskTitle, oneDayBefore, "reminder_24h", userId);
                                }
                                if (oneHourBefore > now) {
                                    helper.scheduleReminderNotification(taskTitle, oneHourBefore, "reminder_1h", userId);
                                }
                                if (oneMinuteBefore > now) {
                                    helper.scheduleReminderNotification(taskTitle, oneMinuteBefore, "reminder_1m", userId);
                                }
                            } else {
                                showToast("Erreur : date nulle");
                            }
                        } catch (ParseException e) {
                            showToast("Format de date invalide. Utilisez dd/MM/yyyy HH:mm");
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error saving task"));
        }
    }
    private boolean validateInput(EditText titleField,  TextView endDateField) {
        String title = titleField.getText().toString().trim();
        String endDateStr = endDateField.getText().toString().trim();

        if (title.isEmpty()) {
            showToast("Please enter a Title");
            return false;
        }
        if (endDateStr.isEmpty()) {
            showToast("Please select an End Date and Time");
            return false;
        }
        //return isValidDateRange(startDateStr, endDateStr);
        return true;
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void showDateTimePicker(TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                textView.setText(dateFormat.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private View addTaskToUI(String title, String endDate,boolean isCompleted, String taskId) {
        View taskView = LayoutInflater.from(this).inflate(R.layout.item_private_task, taskContainer, false);
        taskView.setBackgroundResource(isNightMode() ? R.drawable.background_dark : R.drawable.background_light);

        TextView taskTitle = taskView.findViewById(R.id.taskTitle);
        TextView taskDates = taskView.findViewById(R.id.taskDates);
        CheckBox checkBoxCompleted = taskView.findViewById(R.id.checkbox);
        ImageView optionMenu = taskView.findViewById(R.id.optionMenu);
        taskTitle.setText(title);
        taskDates.setText("Deadline: " + endDate);
        // If the task is completed, strike-through the title and dates
        if (isCompleted) {
            checkBoxCompleted.setChecked(true);
            taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskDates.setPaintFlags(taskDates.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            checkBoxCompleted.setChecked(false);
            taskTitle.setPaintFlags(taskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            taskDates.setPaintFlags(taskDates.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("tasks_pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(taskId, isChecked); // Utilise l'ID de la tâche pour garder l'état
            editor.apply();
            // Mise à jour de Firestore avec le nouvel état
            db.collection("private_tasks").document(taskId).update("isCompleted", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        // Met à jour l'UI en fonction de l'état de la tâche
                        if (isChecked) {
                            taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            taskDates.setPaintFlags(taskDates.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                            // Envoie la notification de tâche terminée
                            NotificationHelper notificationHelper = new NotificationHelper(Private_NoteActivity.this);
                            notificationHelper.sendNotification(taskTitle.getText().toString(), "completed");

                        } else {
                            taskTitle.setPaintFlags(taskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                            taskDates.setPaintFlags(taskDates.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error updating task"));
        });

        optionMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Private_NoteActivity.this, v);
            popupMenu.inflate(R.menu.menu_task_options);  // Définir un menu XML pour les options (Modifier, Supprimer)

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.optionModify) {
                    // Logique pour modifier la tâche
                    String currentTitle = taskTitle.getText().toString();
                    String currentDates = taskDates.getText().toString();
                    showEditTaskDialog(taskView, currentTitle, currentDates);  // Passe maintenant les 3 arguments nécessaires
                } else if (item.getItemId() == R.id.optionDelete) {
                    // Récupérer l'ID de la tâche
                    confirmAndDeleteTask(taskView);
                } else {
                    return false;
                }
                return true;
            });

            popupMenu.show();
        });
        taskView.setTag(taskId);
        taskContainer.addView(taskView, 0);
        return taskView;
    }
    private void showEditTaskDialog(View taskView, String currentTitle, String currentDates) {
        // Créer une vue pour le dialogue d'édition
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_private_task, null);
        // Trouver les éléments de l'interface du dialogue
        EditText editTaskTitle = dialogView.findViewById(R.id.editprivateTasktitle);
        TextView textViewTaskEndDate = dialogView.findViewById(R.id.textViewTaskEndDate);
        // Extraire les dates actuelles
        String[] dateParts = currentDates.split("\n");
        String endDate = dateParts.length > 1 ? dateParts[1].replace("End: ", "").trim() : "";
        // Pré-remplir les champs du dialogue avec les valeurs actuelles
        editTaskTitle.setText(currentTitle);
        textViewTaskEndDate.setText(endDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modify Task")
                .setView(dialogView)
                .setPositiveButton("Save", null)  // On laissera la gestion du bouton Save plus tard
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateInput(editTaskTitle, textViewTaskEndDate)) {
                // Récupérer les nouvelles valeurs
                String newTitle = editTaskTitle.getText().toString().trim();
                String newEndDate = textViewTaskEndDate.getText().toString().trim();

                // Récupérer l'ID de la tâche
                String taskId = (String) taskView.getTag();  // Récupérer l'ID stocké dans le tag de la vue de la tâche
                // Mettre à jour la tâche dans Firestore
                db.collection("private_tasks").document(taskId)
                        .update("title", newTitle,  "endDate", newEndDate)
                        .addOnSuccessListener(aVoid -> {
                            // Mettre à jour les éléments de l'UI avec les nouvelles valeurs
                            ((TextView) taskView.findViewById(R.id.taskTitle)).setText(newTitle);
                            ((TextView) taskView.findViewById(R.id.taskDates)).setText("End: " + newEndDate);
                            Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show());

                // Mettre à jour les éléments dans l'UI
                TextView taskTitle = taskView.findViewById(R.id.taskTitle);
                TextView taskDates = taskView.findViewById(R.id.taskDates);

                taskTitle.setText(newTitle);
                taskDates.setText("End: " + newEndDate);

                Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
        textViewTaskEndDate.setOnClickListener(v -> showDateTimePicker(textViewTaskEndDate));
    }
    private void confirmAndDeleteTask(View taskView) {
        String taskId = (String) taskView.getTag();
        TextView taskTitleTextView = taskView.findViewById(R.id.taskTitle);
        String taskTitle = taskTitleTextView.getText().toString();

        new AlertDialog.Builder(Private_NoteActivity.this)
                .setTitle("Confirm Task Deletion")
                .setMessage("Are you sure you want to delete the task: '" + taskTitle + "'? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("private_tasks").document(taskId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    PrivateTaskModel task = documentSnapshot.toObject(PrivateTaskModel.class);
                                    if (task != null) {
                                        int notifId = task.getNotificationId();
                                        String endDate = task.getEndDate();
                                        String userId = task.getUserId();
                                        // 1. Annuler les notifications affichées
                                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        manager.cancel(notifId + 1); // J-1
                                        manager.cancel(notifId + 2); // H-1
                                        manager.cancel(notifId + 3); // M-1
                                        // 2. Annuler les AlarmManager programmés
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                        if (alarmManager != null) {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                            try {
                                                Date date = dateFormat.parse(endDate);
                                                if (date != null) {
                                                    long timeInMillis = date.getTime();

                                                    long oneDayBefore = timeInMillis - 24 * 60 * 60_000;
                                                    long oneHourBefore = timeInMillis - 60 * 60_000;
                                                    long oneMinuteBefore = timeInMillis - 60_000;

                                                    cancelAlarm(alarmManager, taskTitle, "reminder_24h", oneDayBefore, userId);
                                                    cancelAlarm(alarmManager, taskTitle, "reminder_1h", oneHourBefore, userId);
                                                    cancelAlarm(alarmManager, taskTitle, "reminder_1m", oneMinuteBefore, userId);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    // 3. Supprimer la tâche
                                    db.collection("private_tasks").document(taskId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                taskContainer.removeView(taskView);
                                                Toast.makeText(Private_NoteActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(Private_NoteActivity.this, "Error deleting task", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void cancelAlarm(AlarmManager alarmManager, String taskTitle, String type, long triggerTime, String userId) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("notification_type", type);
        intent.putExtra("task_user_id", userId);

        int requestCode = (taskTitle + type + triggerTime).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }

}


