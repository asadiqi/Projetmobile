package com.example.startxplanify;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class NoteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button buttonAddNote ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        buttonAddNote = findViewById(R.id.button_addNote);

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

        // Initialiser les champs de texte dans le dialogue
        EditText privateTaskTitle = dialogView.findViewById(R.id.editprivateTasktitle);
        EditText privateTaskStartDate = dialogView.findViewById(R.id.editprivateTaskStartDate);
        EditText privateTaskEndDate = dialogView.findViewById(R.id.editprivateTaskEndtDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Private Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = privateTaskTitle.getText().toString();
                    String startDate = privateTaskStartDate.getText().toString();
                    String endDate = privateTaskEndDate.getText().toString();

                    // Validation des champs
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Please enter a Title", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (startDate.isEmpty()) {
                        Toast.makeText(this, "Please select a Start Date and Time", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (endDate.isEmpty()) {
                        Toast.makeText(this, "Please select an End Date and Time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(NoteActivity.this, "Task Added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();

        // Ajouter des écouteurs pour la date et l'heure
        privateTaskStartDate.setOnClickListener(v -> showDateTimePicker(privateTaskStartDate));
        privateTaskEndDate.setOnClickListener(v -> showDateTimePicker(privateTaskEndDate));
    }

    // Fonction pour afficher le DatePickerDialog et TimePickerDialog
    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                // Formater et afficher la date et l'heure sélectionnées
                                String dateTime = year + "-" + (month + 1) + "-" + dayOfMonth + " " +
                                        String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
                                editText.setText(dateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


}
