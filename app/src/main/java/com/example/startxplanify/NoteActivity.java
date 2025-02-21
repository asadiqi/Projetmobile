package com.example.startxplanify;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoteActivity extends AppCompatActivity {


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gonfle le menu depuis le fichier XML
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.about) {
            Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.logout) {
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Cela permet d'afficher le menu dans la Toolbar


        // Charger les préférences après le super.onCreate
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        // Appliquer le mode nuit selon la préférence
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }



    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch themeSwitch = findViewById(R.id.switchTheme);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("night_mode",isChecked);
        editor.apply();

        AppCompatDelegate.setDefaultNightMode(isChecked? AppCompatDelegate.MODE_NIGHT_YES: AppCompatDelegate.MODE_NIGHT_NO);
    });
}




}