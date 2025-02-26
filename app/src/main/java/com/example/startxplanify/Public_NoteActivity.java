package com.example.startxplanify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.PopupMenu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Public_NoteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button buttonAddNote;
    private TextView textViewPublicTaskStartDate, textViewPublicTaskEndDate;

    private AutoCompleteTextView location;

    private LinearLayout taskContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Initialisation des vues
        buttonAddNote = findViewById(R.id.button_addNote);
        taskContainer = findViewById(R.id.taskContainer);
        setupDrawerAndNavigation();
        setupNightModePreferences();

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Gestion du clic sur le bouton Ajouter une tâche
        buttonAddNote.setOnClickListener(v -> showAddPublicTaskDialog());

        loadUserTasks(); // Chargement des tâches à chaque redémarrage de l'activité

    }

    private void setupDrawerAndNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemClick(item.getItemId());
            return true;
        });
    }

    private void handleNavigationItemClick(int itemId) {
        if (itemId == R.id.nav_home) {
            showToast("Home sélectionné");
        } else if (itemId == R.id.nav_settings) {
            showToast("Settings sélectionné");
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
        }
        drawerLayout.closeDrawers();
    }


    private void setupNightModePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Switch pour le mode nuit
        Switch themeSwitch = findViewById(R.id.switchTheme);
        themeSwitch.setChecked(isNightMode);
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void loadUserTasks() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("public_tasks")
                    .whereEqualTo("userId", userId)  // Assurez-vous que c'est le userId et non la location
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            PublicTaskModel public_task = doc.toObject(PublicTaskModel.class);
                            if (public_task != null) {
                                // Utilisation des bonnes données de modèle ici
                                View taskView = addTaskToUI(
                                        public_task.getTitle(),
                                        public_task.getStartDate(),
                                        public_task.getEndDate(),
                                        public_task.getLocation()
                                );
                                taskView.setTag(public_task.getId());
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error loading tasks"));
        }
    }


    private void showAddPublicTaskDialog() {
        // Chargement du layout du dialogue
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_public_task, null);

        // Récupération des éléments du formulaire
        EditText publicTaskTitle = dialogView.findViewById(R.id.editpublicTasktitle);
        textViewPublicTaskStartDate = dialogView.findViewById(R.id.textViewPublicTaskStartDate);
        textViewPublicTaskEndDate = dialogView.findViewById(R.id.textViewPublicTaskEndDate);
        AutoCompleteTextView taskLocation = dialogView.findViewById(R.id.autoCompleteTaskLocation);

        // Création du dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Public Task") // Correction du titre
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Gestion du bouton "Add" pour éviter la fermeture du dialogue en cas de validation incorrecte
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = publicTaskTitle.getText().toString().trim();
            String startDate = textViewPublicTaskStartDate.getText().toString().trim();
            String endDate = textViewPublicTaskEndDate.getText().toString().trim();
            String location = taskLocation.getText().toString().trim();

            if (validateInput(publicTaskTitle, textViewPublicTaskStartDate, textViewPublicTaskEndDate, taskLocation)) {
                createPublicTask(title, startDate, endDate, location); // Passer tous les paramètres à la méthode
                alertDialog.dismiss();
            }
        });

        // Gestion des sélecteurs de date
        textViewPublicTaskStartDate.setOnClickListener(v -> showDateTimePicker(textViewPublicTaskStartDate));
        textViewPublicTaskEndDate.setOnClickListener(v -> showDateTimePicker(textViewPublicTaskEndDate));
    }


    private void createPublicTask(String title, String startDate, String endDate, String location) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String taskId = db.collection("public_tasks").document().getId();

            // Correction de l'initialisation du modèle : l'ID de l'utilisateur va dans "userId" et la location dans "location"
            PublicTaskModel publictask = new PublicTaskModel(taskId, title, startDate, endDate, userId, location);

            // Enregistrement de la tâche publique dans Firestore
            db.collection("public_tasks").document(taskId).set(publictask)
                    .addOnSuccessListener(aVoid -> {
                        // Ajout de la tâche à l'interface utilisateur
                        View taskView = addTaskToUI(title, startDate, endDate, location);
                        taskView.setTag(taskId);
                        showToast("Public Task Added");
                    })
                    .addOnFailureListener(e -> showToast("Error saving public task: " + e.getMessage()));
        } else {
            showToast("User not authenticated");
        }
    }


    private boolean validateInput(EditText titleField, TextView startDateField, TextView endDateField,AutoCompleteTextView locationField) {
        String title = titleField.getText().toString().trim();
        String startDateStr = startDateField.getText().toString().trim();
        String endDateStr = endDateField.getText().toString().trim();
        String location = locationField.getText().toString().trim();

        if (title.isEmpty()) {
            showToast("Please enter a Title");
            return false;
        }
        if (startDateStr.isEmpty()) {
            showToast("Please select a Start Date and Time");
            return false;
        }
        if (endDateStr.isEmpty()) {
            showToast("Please select an End Date and Time");
            return false;
        }

        if (location.isEmpty()) {
            showToast("Please enter a Location");
            return false;
        }

        return isValidDateRange(startDateStr, endDateStr);
    }

    // Méthode modifiée de validation de la plage de dates
    private boolean isValidDateRange(String startDateStr, String endDateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            // Vérification si la date de début est avant la date de fin
            if (startDate != null && endDate != null) {
                if (startDate.after(endDate)) {
                    showToast("Start Date cannot be after End Date");
                    return false;  // Si la date de début est après la date de fin, retournera false
                }
                return true;
            } else {
                showToast("Invalid date format");
                return false;
            }
        } catch (ParseException e) {
            showToast("Invalid date format");
            return false;
        }
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

    private View addTaskToUI(String title, String startDate, String endDate, String location) {
        View taskView = LayoutInflater.from(this).inflate(R.layout.item_public_task, taskContainer, false);
        taskView.setBackgroundResource(isNightMode() ? R.drawable.background_dark : R.drawable.background_light);

        TextView taskTitle = taskView.findViewById(R.id.publictaskTitle);
        TextView taskDates = taskView.findViewById(R.id.publictaskDates);
        TextView taskLocation = taskView.findViewById(R.id.location);
        ImageView optionMenu = taskView.findViewById(R.id.publicoptionMenu);

        taskTitle.setText(title);
        taskDates.setText("Start: " + startDate + "\nEnd: " + endDate);
        taskLocation.setText("Location: " + location);

        optionMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Public_NoteActivity.this, v);
            popupMenu.inflate(R.menu.menu_task_options);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.optionModify) {
                    // Modification de la tâche
                    String currentTitle = taskTitle.getText().toString();
                    String currentDates = taskDates.getText().toString();
                    String currentLocation = taskLocation.getText().toString();
                    showEditTaskDialog(taskView, currentTitle, currentDates, currentLocation);
                } else if (item.getItemId() == R.id.optionDelete) {
                    // Suppression avec confirmation
                 confirmAndDeleteTask(taskView);
                } else {
                    return false;
                }
                return true;
            });

            popupMenu.show();
        });

        taskContainer.addView(taskView, 0);
        return taskView;
    }


    private void showEditTaskDialog(View taskView, String currentTitle, String currentDates, String currentLocation) {
        // Chargement du layout du dialogue
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_public_task, null);

        // Récupération des éléments du formulaire
        EditText editTaskTitle = dialogView.findViewById(R.id.editpublicTasktitle);
        TextView editStartDate = dialogView.findViewById(R.id.textViewPublicTaskStartDate);
        TextView editEndDate = dialogView.findViewById(R.id.textViewPublicTaskEndDate);
        AutoCompleteTextView editTaskLocation = dialogView.findViewById(R.id.autoCompleteTaskLocation);

        // Remplir les champs avec les valeurs actuelles
        editTaskTitle.setText(currentTitle);
        editStartDate.setText(currentDates.split("\n")[0].replace("Start: ", ""));
        editEndDate.setText(currentDates.split("\n")[1].replace("End: ", ""));
        editTaskLocation.setText(currentLocation);

        // Créer le dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Public Task")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Gestion du bouton "Save"
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = editTaskTitle.getText().toString().trim();
            String startDate = editStartDate.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();
            String location = editTaskLocation.getText().toString().trim();

            // Validation des données
            if (validateInput(editTaskTitle, editStartDate, editEndDate, editTaskLocation)) {
                // Si tout est valide, on met à jour Firestore et l'interface utilisateur
                String taskId = (String) taskView.getTag();  // Récupérer l'ID de la tâche à partir du tag

                // Création du modèle de la tâche mise à jour
                PublicTaskModel updatedTask = new PublicTaskModel(taskId, title, startDate, endDate, auth.getCurrentUser().getUid(), location);

                // Mettre à jour la tâche dans Firestore
                db.collection("public_tasks").document(taskId).set(updatedTask)
                        .addOnSuccessListener(aVoid -> {
                            // Mise à jour de la vue de la tâche sur l'interface
                            TextView taskTitle = taskView.findViewById(R.id.publictaskTitle);
                            TextView taskDates = taskView.findViewById(R.id.publictaskDates);
                            TextView taskLocation = taskView.findViewById(R.id.location);

                            taskTitle.setText(title);
                            taskDates.setText("Start: " + startDate + "\nEnd: " + endDate);
                            taskLocation.setText(location);

                            showToast("Public Task Updated");
                        })
                        .addOnFailureListener(e -> {
                            showToast("Error updating public task: " + e.getMessage());
                        });

                alertDialog.dismiss();
            }
        });

        // Gestion des sélecteurs de date
        editStartDate.setOnClickListener(v -> showDateTimePicker(editStartDate));
        editEndDate.setOnClickListener(v -> showDateTimePicker(editEndDate));
    }



    // Ajouter le message de confirmation personnalisé pour la suppression de tâche
    private void confirmAndDeleteTask(View taskView) {
        // Récupérer l'ID de la tâche à partir du tag de la vue
        String taskId = (String) taskView.getTag();

        // Afficher une boîte de dialogue de confirmation
        new AlertDialog.Builder(Public_NoteActivity.this)
                .setTitle("Delete Task Confirmation")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Supprimer la tâche de Firestore
                    db.collection("public_tasks").document(taskId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Supprimer la tâche de l'interface utilisateur
                                taskContainer.removeView(taskView);
                                showToast("Public Task Deleted");
                            })
                            .addOnFailureListener(e -> {
                                // Afficher un message d'erreur en cas de problème avec la suppression
                                showToast("Error deleting task: " + e.getMessage());
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Fermer la boîte de dialogue sans suppression
                .show();
    }

    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }

    private void handleLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        Intent intent = new Intent(Public_NoteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        showToast("You are logged out");
    }
}
