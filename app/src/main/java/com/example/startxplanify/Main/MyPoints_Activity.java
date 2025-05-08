package com.example.startxplanify.Main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.startxplanify.Notes_Activity.BaseNoteActivity;
import com.example.startxplanify.R;
import com.google.firebase.auth.FirebaseAuth;
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
        loadUserPoints();  // Charger les points de l'utilisateur
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

    // Charger les points de l'utilisateur connecté
    private void loadUserPoints() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String currentUserId = user.getUid();

            // Accéder aux points de l'utilisateur dans la collection "users"
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Récupérer les points de l'utilisateur
                            Long points = documentSnapshot.getLong("points");
                            if (points != null) {
                                // Afficher les points dans le TextView
                                pointsTextView.setText("Your Points: " + points);
                            } else {
                                pointsTextView.setText("Your Points: 0");
                            }
                        } else {
                            showToast("User not found.");
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error loading points"));
        } else {
            showToast("User not logged in.");
        }
    }

    // Afficher un toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Vérifier si le mode nuit est activé
    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }
}
