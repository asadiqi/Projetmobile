package com.example.startxplanify;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.PopupMenu;


public class NoteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button buttonAddNote ;
    private TextView textViewTaskStartDate, textViewTaskEndDate;

    private LinearLayout taskContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        buttonAddNote = findViewById(R.id.button_addNote);
        taskContainer = findViewById(R.id.taskContainer);

        buttonAddNote.setOnClickListener(v -> {
            showAddPrivateTaskDialog();

        });

        // Récupérer la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialiser le DrawerLayout et NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Ajouter le bouton hamburger ☰
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Gérer les clics sur le menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(NoteActivity.this, "Home sélectionné", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(NoteActivity.this, "Settings sélectionné", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                SharedPreferences sharesdPreferences = getSharedPreferences("settings",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharesdPreferences.edit();
                editor.putBoolean("is_logged_in",false);
                editor.apply();
                // Rediriger vers MainActivity (écran d'accueil)
                Intent intent = new Intent(NoteActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(NoteActivity.this, "You are Loged out", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers(); // Fermer le menu après un clic
            return true;
        });

        // Charger les préférences après le super.onCreate
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        // Appliquer le mode nuit selon la préférence
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Gérer le Switch pour le mode nuit
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch themeSwitch = findViewById(R.id.switchTheme);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    // Déclaration correcte de la méthode showAddPrivateTaskDialog
    private void showAddPrivateTaskDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText privateTaskTitle = dialogView.findViewById(R.id.editprivateTasktitle);
        textViewTaskStartDate = dialogView.findViewById(R.id.textViewTaskStartDate);
        textViewTaskEndDate = dialogView.findViewById(R.id.textViewTaskEndDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Private Task")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();

        // Afficher le dialogue
        alertDialog.show();
        // Gérer l'ajout de la tâche dans le dialog
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateInput(privateTaskTitle, textViewTaskStartDate, textViewTaskEndDate)) {
                // Récupérer les valeurs de la tâche
                String taskTitle = privateTaskTitle.getText().toString().trim();
                String startDate = textViewTaskStartDate.getText().toString().trim();
                String endDate = textViewTaskEndDate.getText().toString().trim();

                // Appeler la méthode pour ajouter la tâche à l'interface
                addTaskToUI(taskTitle, startDate, endDate);

                // Afficher un message
                Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show();

                // Fermer le dialog
                alertDialog.dismiss();
            }
        });

        textViewTaskStartDate.setOnClickListener(v -> showDateTimePicker(textViewTaskStartDate));
        textViewTaskEndDate.setOnClickListener(v -> showDateTimePicker(textViewTaskEndDate));
    }

    // Validation des entrées
    private boolean validateInput(EditText titleField, TextView startDateField, TextView endDateField) {
        String title = titleField.getText().toString().trim();
        String startDateStr = startDateField.getText().toString().trim();
        String endDateStr = endDateField.getText().toString().trim();

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

        if (!isValidDateRange(startDateStr, endDateStr)) {
            showToast("End Date must be after Start Date");
            return false;
        }

        return true;
    }

    // Vérification que la date de fin est après la date de début
    private boolean isValidDateRange(String startDateStr, String endDateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            return startDate != null && endDate != null && startDate.before(endDate);
        } catch (ParseException e) {
            showToast("Invalid date format");
            return false;
        }
    }

    // Afficher un toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Fonction pour afficher le sélecteur de date et d'heure
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
    // Ajouter une tâche dynamiquement à l'interface
    private void addTaskToUI(String title, String startDate, String endDate) {
        View taskView = LayoutInflater.from(this).inflate(R.layout.item_private_task, taskContainer, false);

        // Appliquer l'arrière-plan en fonction du thème actuel
        if (isNightMode()) {
            taskView.setBackgroundResource(R.drawable.background_dark);  // Mode sombre
        } else {
            taskView.setBackgroundResource(R.drawable.background_light);  // Mode clair
        }

        TextView taskTitle = taskView.findViewById(R.id.taskTitle);
        TextView taskDates = taskView.findViewById(R.id.taskDates);
        Button deleteButton = taskView.findViewById(R.id.deleteButton);

        taskTitle.setText(title);
        taskDates.setText("Start: " + startDate + "\nEnd: " + endDate);

        // Initialiser l'optionMenu dans la méthode addTaskToUI (correctement liée à chaque taskView)
        ImageView optionMenu = taskView.findViewById(R.id.optionMenu);

        // Gérer le clic sur l'icône des 3 points (menu d'options)
        optionMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(NoteActivity.this, v);
            popupMenu.inflate(R.menu.menu_task_options);  // Définir un menu XML pour les options (Modifier, Supprimer)

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.optionModify) {
                    // Logique pour modifier la tâche
                    Toast.makeText(NoteActivity.this, "Modifier tâche", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.optionDelete) {
                    // Logique pour supprimer la tâche
                    taskContainer.removeView(taskView);
                    Toast.makeText(NoteActivity.this, "Tâche supprimée", Toast.LENGTH_SHORT).show();
                } else {
                    return false;
                }
                return true;
            });

            popupMenu.show();
        });

        taskContainer.addView(taskView, 0);
    }

    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false); // Retourne si le mode nuit est activé
    }

}
