package com.example.startxplanify.Notes_Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.startxplanify.Main.Map;
import com.example.startxplanify.Main.Participate;
import com.example.startxplanify.Models.PublicTaskModel;
import com.example.startxplanify.R;
import com.example.startxplanify.Translate.Translate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyPublicTasks_Activity extends BaseNoteActivity {
    private LinearLayout taskContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Button addNoteButton = findViewById(R.id.button_addNote);
        addNoteButton.setVisibility(View.GONE);

        initViews();
        initFirebase();
        loadUserTasks();
    }

    private void initViews() {
        taskContainer = findViewById(R.id.taskContainer);
        setupDrawerAndNavigation();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void loadUserTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentUserId = user.getUid();

            db.collection("public_tasks")
                    .whereEqualTo("userId", currentUserId) // filtre ici
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            PublicTaskModel task = doc.toObject(PublicTaskModel.class);
                            if (task != null) {
                                View taskView = createTaskViewFromModel(task);
                                taskView.setTag(task.getId());
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error loading tasks"));
        }
    }

    private View createTaskViewFromModel(PublicTaskModel task) {
        return addTaskToUI(
                task,
                task.getTitle(),
                task.getEventDate(),
                task.getLocation(),
                task.getDescription(),
                task.getCreatorName(),
                task.getUserId()
        );
    }

    private View addTaskToUI(PublicTaskModel task, String title, String endDate, String location, String description, String creatorName, String taskUserId) {
        View taskView = LayoutInflater.from(this).inflate(R.layout.item_mypublicasks, taskContainer, false);
        taskView.setBackgroundResource(isNightMode() ? R.drawable.background_dark : R.drawable.background_light);

        TextView taskTitle = taskView.findViewById(R.id.publictaskTitle);
        TextView taskDates = taskView.findViewById(R.id.publictaskDates);
        TextView taskLocation = taskView.findViewById(R.id.location);
        TextView creatorNameTextView = taskView.findViewById(R.id.creatorName);
        ScrollView descriptionScrollView = taskView.findViewById(R.id.descriptionScrollView);

        taskTitle.setText(title);
        taskDates.setText("Event Date: " + endDate);
        taskLocation.setText("Location: " + location);
        creatorNameTextView.setText("Created by: " + creatorName);

        TextView taskDescription = taskView.findViewById(R.id.description);
        taskDescription.setText("Description: " + description);
        descriptionScrollView.setVisibility(View.GONE);
        TextView participantList = taskView.findViewById(R.id.participantsTextView);

        taskView.setOnClickListener(v -> {
            boolean isVisible = descriptionScrollView.getVisibility() == View.VISIBLE;

            descriptionScrollView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            participantList.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        FirebaseFirestore.getInstance()
                .collection("public_tasks")
                .document(task.getId())
                .collection("participants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder participants = new StringBuilder("Participants :\n\n");
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String uid = doc.getString("uid");
                        String email = doc.getString("email");

                        if (uid != null) {
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String name = userDoc.getString("name");
                                        if (name == null || name.isEmpty()) {
                                            name = "Unknown";  // Si le nom est vide ou null, on met "Unknown"
                                        }
                                        participants.append("- ").append(name).append(" (").append(email).append(")\n\n");
                                        participantList.setText(participants.toString());
                                    });
                        } else {
                            participants.append("- Unknown (").append(email).append(")\n");
                            participantList.setText(participants.toString());
                        }
                    }
                    participantList.setText(participants.toString());
                    participantList.setVisibility(View.GONE); // Assurez-vous que le TextView est visible
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyPublicTasks_Activity.this, "Error loading participants", Toast.LENGTH_SHORT).show();
                });

        taskContainer.addView(taskView, 0);
        return taskView;
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }
}



