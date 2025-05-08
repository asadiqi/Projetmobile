package com.example.startxplanify.Main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.startxplanify.Notes_Activity.BaseNoteActivity;
import com.example.startxplanify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyPoints_Activity extends BaseNoteActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Masquer le bouton "Add Note" dans cette activité
        hideAddNoteButton();

        initViews();
        initFirebase();
    }

    // Initialisation des vues
    private void initViews() {
        setupDrawerAndNavigation();
    }

    // Initialisation de Firebase
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Cacher le bouton d'ajout de note
    private void hideAddNoteButton() {
        Button addNoteButton = findViewById(R.id.button_addNote);
        addNoteButton.setVisibility(View.GONE);
    }







    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Vérifier si le mode nuit est activé
    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }
}
