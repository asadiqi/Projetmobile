package com.example.startxplanify.Main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.startxplanify.Models.PublicTaskModel;
import com.example.startxplanify.Notes_Activity.BaseNoteActivity;
import com.example.startxplanify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

public class MyPoints_Activity extends BaseNoteActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView pointsTextView;  // TextView pour afficher les points

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Masquer le bouton "Add Note" dans cette activité
        hideAddNoteButton();

        initViews();
        initFirebase();
        loadUserPoints();

    }

    // Initialisation des vues
    private void initViews() {
        pointsTextView = findViewById(R.id.pointsTextView);  // Référence au TextView pour afficher les points
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

    private void loadUserPoints() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showToast("User not logged in");
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long points = documentSnapshot.getLong("points");
                        if (points != null) {
                            pointsTextView.setText("Your points: " + points);
                        } else {
                            pointsTextView.setText("No points available");
                        }
                    } else {
                        pointsTextView.setText("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to load points");
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }
}
