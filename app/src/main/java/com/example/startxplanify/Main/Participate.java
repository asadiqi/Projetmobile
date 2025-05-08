package com.example.startxplanify.Main;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.startxplanify.Notes_Activity.MyPublicTasks_Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import java.util.HashMap;



import java.util.HashMap;

public class Participate {

    private Context context;
    private Button participateButton;

    public Participate(Context context, Button participateButton) {
        this.context = context;
        this.participateButton = participateButton;
    }

    public void setupParticipateButton() {
        participateButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String taskId = (String) participateButton.getTag();
                if (taskId == null || taskId.isEmpty()) {
                    Toast.makeText(context, "Task ID is missing!", Toast.LENGTH_SHORT).show();
                    return;
                }


                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String uid = currentUser.getUid();
                String email = currentUser.getEmail();

                // Récupérer le nom de la collection "users"
                db.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {

                            Map<String, Object> participant = new HashMap<>();
                            participant.put("uid", uid);
                            participant.put("email", email);


                            db.collection("public_tasks").document(taskId)
                                    .collection("participants")
                                    .add(participant)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "You have joined the task!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error joining the task.", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(context, "You must be logged in to participate.", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
