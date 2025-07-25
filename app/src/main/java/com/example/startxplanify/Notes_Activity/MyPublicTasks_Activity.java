package com.example.startxplanify.Notes_Activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.startxplanify.Models.PublicTaskModel;
import com.example.startxplanify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyPublicTasks_Activity extends BaseNoteActivity {
    private LinearLayout taskContainer;
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
        loadUserTasks();
        hideAddpointTexteView();
    }

    // Initialisation des vues
    private void initViews() {
        taskContainer = findViewById(R.id.taskContainer);
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

    private void hideAddpointTexteView() {
        TextView textView = findViewById(R.id.pointsTextView);
        textView.setVisibility(View.GONE);
    }

    // Charger les tâches de l'utilisateur connecté
    private void loadUserTasks() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String currentUserId = user.getUid();
            db.collection("public_tasks")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            PublicTaskModel task = doc.toObject(PublicTaskModel.class);
                            if (task != null) {
                                addTaskToUI(task);
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error loading tasks"));
        }
    }

    // Ajouter la tâche à l'interface utilisateur
    private void addTaskToUI(PublicTaskModel task) {
        View taskView = createTaskViewFromModel(task);
        taskView.setTag(task.getId());
        taskContainer.addView(taskView, 0);
    }

    // Créer la vue de la tâche à partir du modèle
    private View createTaskViewFromModel(PublicTaskModel task) {
        return inflateTaskView(task);
    }

    // Infuser la vue de la tâche
    private View inflateTaskView(PublicTaskModel task) {
        View taskView = LayoutInflater.from(this).inflate(R.layout.item_mypublicasks, taskContainer, false);
        taskView.setBackgroundResource(isNightMode() ? R.drawable.background_dark : R.drawable.background_light);

        TextView taskTitle = taskView.findViewById(R.id.publictaskTitle);
        TextView taskDates = taskView.findViewById(R.id.publictaskDates);
        TextView taskLocation = taskView.findViewById(R.id.location);
        TextView creatorNameTextView = taskView.findViewById(R.id.creatorName);
        ScrollView descriptionScrollView = taskView.findViewById(R.id.descriptionScrollView);

        taskTitle.setText(task.getTitle());
        taskDates.setText("Event Date: " + task.getEventDate());
        taskLocation.setText("Location: " + task.getLocation());
        creatorNameTextView.setText("Created by: " + task.getCreatorName());

        // Cacher la description par défaut
        TextView taskDescription = taskView.findViewById(R.id.description);
        taskDescription.setText("Description: " + task.getDescription());
        descriptionScrollView.setVisibility(View.GONE);

        // Liste des participants
        TextView participantList = taskView.findViewById(R.id.participantsTextView);
        fetchParticipants(task, participantList);

        Button givePointsButton = taskView.findViewById(R.id.gievPointsToParticipants);  // Référence au bouton
        givePointsButton.setVisibility(View.GONE);
        givePointsButton.setTag(task); // nécessaire pour récupérer la tâche dans le listener

        Button deleteTaskButton = taskView.findViewById(R.id.deleteTask);  // Référence au bouton
        deleteTaskButton.setVisibility(View.GONE);
        deleteTaskButton.setTag(task);



        // Interaction sur la vue de la tâche
        taskView.setOnClickListener(v -> toggleTaskVisibility(descriptionScrollView, participantList,givePointsButton,deleteTaskButton));

        return taskView;
    }

    // Charger les participants
    private void fetchParticipants(PublicTaskModel task, TextView participantList) {
        FirebaseFirestore.getInstance()
                .collection("public_tasks")
                .document(task.getId())
                .collection("participants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder participants = new StringBuilder("Participants:\n\n");
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String uid = doc.getString("uid");
                        String email = doc.getString("email");

                        if (uid != null) {
                            fetchUserName(uid, email, participants, participantList);
                        } else {
                            participants.append("- Unknown (").append(email).append(")\n");
                            participantList.setText(participants.toString());
                        }
                    }
                    participantList.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> showToast("Error loading participants"));
    }

    // Obtenir le nom de l'utilisateur par UID
    private void fetchUserName(String uid, String email, StringBuilder participants, TextView participantList) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");
                    if (name == null || name.isEmpty()) {
                        name = "Unknown";
                    }
                    participants.append("- ").append(name).append(" (").append(email).append(")\n\n");
                    participantList.setText(participants.toString());
                });
    }

    // Basculer la visibilité de la description et des participants
    private void toggleTaskVisibility(ScrollView descriptionScrollView, TextView participantList, Button givePointsButton, Button deleteTaskButton) {
        boolean isVisible = descriptionScrollView.getVisibility() == View.VISIBLE;
        descriptionScrollView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        participantList.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        givePointsButton.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        deleteTaskButton.setVisibility(isVisible ? View.GONE : View.VISIBLE);

        PublicTaskModel task = (PublicTaskModel) deleteTaskButton.getTag();
        setupGivePointsButton(task, givePointsButton);
        setupDeleteTaskButton(task, deleteTaskButton);
    }

    private void setupGivePointsButton(PublicTaskModel task, Button givePointsButton) {
        givePointsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Give Points")
                    .setMessage("Are you sure you want to give 1 point to all participants and mark this task as completed?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        givePointsToParticipants(task);
                        markTaskAsCompleted(task);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void setupDeleteTaskButton(PublicTaskModel task, Button deleteTaskButton) {
        deleteTaskButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task? It will be marked as failed, and no points will be awarded.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        markTaskAsCompleted(task);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void markTaskAsCompleted(PublicTaskModel task) {
        db.collection("public_tasks").document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    removeTaskFromUI(task.getId());
                    showToast("Task marked as completed and removed");
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to mark task as completed");
                });
    }

    private void removeTaskFromUI(String taskId) {
        for (int i = 0; i < taskContainer.getChildCount(); i++) {
            View taskView = taskContainer.getChildAt(i);
            if (taskId.equals(taskView.getTag())) {
                taskContainer.removeView(taskView);
                break;
            }
        }
    }

    private void givePointsToParticipants(PublicTaskModel task) {
        db.collection("public_tasks")
                .document(task.getId())
                .collection("participants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String uid = doc.getString("uid");
                        if (uid != null) {
                            db.collection("users").document(uid)
                                    .update("points", FieldValue.increment(1));
                        }
                    }
                    showToast("Points successfully given to all participants");
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to give points");
                });
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
