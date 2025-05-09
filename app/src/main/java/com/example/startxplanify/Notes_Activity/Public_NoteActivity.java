
package com.example.startxplanify.Notes_Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import com.example.startxplanify.Main.Map;
import com.example.startxplanify.Main.Participate;
import com.example.startxplanify.Translate.Translate;
import com.example.startxplanify.Models.PublicTaskModel;
import com.example.startxplanify.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.PopupMenu;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class Public_NoteActivity extends BaseNoteActivity {
    private Button buttonAddNote;
    private TextView  textViewPublicTaskEndDate;
    private TextView locationTextView;
    private LinearLayout taskContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initViews();
        initFirebase();
        setupListeners();
        loadUserTasks();
        hideAddpointTexteView();
    }
    private void initViews() {
        buttonAddNote = findViewById(R.id.button_addNote);
        taskContainer = findViewById(R.id.taskContainer);
        setupDrawerAndNavigation();
    }
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    private void setupListeners() {
        buttonAddNote.setOnClickListener(v -> showAddPublicTaskDialog());
    }

    private void hideAddpointTexteView() {
      TextView textView = findViewById(R.id.pointsTextView);
        textView.setVisibility(View.GONE);
    }

    private void loadUserTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("public_tasks")
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
                task.getTitle(),
                task.getEventDate(),
                task.getLocation(),
                task.getDescription(),
                task.getCreatorName(),
                task.getUserId(),
                task.getId()
        );
    }

    private void showAddPublicTaskDialog() {
        // Chargement du layout du dialogue
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_public_task, null);

        // Récupération des éléments du formulaire
        EditText publicTaskTitle = dialogView.findViewById(R.id.editpublicTasktitle);
        EditText publicTaskDescription = dialogView.findViewById(R.id.editPublicTaskDescription);
        textViewPublicTaskEndDate = dialogView.findViewById(R.id.textViewPublicTaskDeadline);
        locationTextView = dialogView.findViewById(R.id.location); // Récupérer le TextView pour l'adresse
        TextView map = dialogView.findViewById(R.id.locationTextView);
        // Redirection vers la carte au focus
        map.setOnClickListener(v -> {
            Intent intent = new Intent(Public_NoteActivity.this, Map.class);
            startActivityForResult(intent, 1);
        });
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
            String description = publicTaskDescription.getText().toString().trim();
            String endDate = textViewPublicTaskEndDate.getText().toString().trim();
            String location = locationTextView.getText().toString().trim();

            if (validateInput(publicTaskTitle, textViewPublicTaskEndDate, locationTextView,publicTaskDescription)) {
                createPublicTask(title, endDate, location,description ); // Passer tous les paramètres à la méthode
                alertDialog.dismiss();
            }
        });
        // Gestion des sélecteurs de date
        textViewPublicTaskEndDate.setOnClickListener(v -> showDateTimePicker(textViewPublicTaskEndDate));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selectedLocation");

            if (selectedLocation != null && !selectedLocation.isEmpty()) {
                Log.d("PublicNote", "Adresse reçue: " + selectedLocation);

                // Vérifier si le dialogue est ouvert
                if (alertDialog != null && alertDialog.isShowing()) {
                    TextView location = alertDialog.findViewById(R.id.location);
                    if (location != null) {
                        location.setText(selectedLocation);
                    }
                }
                showAlertDialog(selectedLocation);

            } else {
                showToast("Adresse non reçue !");
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {  // Code 2 pour la modification
            String selectedLocation = data.getStringExtra("selectedLocation");

            if (selectedLocation != null && !selectedLocation.isEmpty()) {
                Log.d("PublicNote", "Adresse modifiée reçue: " + selectedLocation);

                // Mettre à jour l'adresse dans le champ d'adresse du dialogue de modification
                if (alertDialog != null && alertDialog.isShowing()) {
                    TextView location = alertDialog.findViewById(R.id.location);
                    if (location != null) {
                        location.setText(selectedLocation);
                    }
                }
                showAlertDialog(selectedLocation);
            } else {
                showToast("Adresse non reçue !");
            }
        } else {
            showToast("Erreur lors de la récupération de l'adresse !");
        }
    }

    public void showAlertDialog(String selectedLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Adresse : " + selectedLocation)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Ici, vous appelez le callback pour mettre à jour l'adresse dans showAddPublicTaskDialog
                    onAddressUpdated(selectedLocation);
                    dialog.dismiss();
                });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void createPublicTask(String title, String endDate, String location, String description) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String taskId = db.collection("public_tasks").document().getId();
            // Récupérer le nom de l'utilisateur depuis Firebase Auth
            AtomicReference<String> creatorName = new AtomicReference<>(user.getDisplayName());

            // Si le nom est vide, aller récupérer le nom dans la collection "users"
            if (creatorName.get() == null || creatorName.get().isEmpty()) {
                db.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Récupérer le nom depuis la collection 'users'
                                creatorName.set(documentSnapshot.getString("name")); // Supposons que le nom est stocké sous la clé "name"
                            }
                            if (creatorName.get() == null || creatorName.get().isEmpty()) {
                                creatorName.set(user.getEmail()); // Utiliser l'email si le nom est toujours vide
                            }
                            // Créer la tâche publique avec le nom correct
                            PublicTaskModel publicTask = new PublicTaskModel(taskId, title, endDate, userId, location, description, creatorName.get());
                            // Enregistrer la tâche dans Firestore
                            db.collection("public_tasks").document(taskId).set(publicTask)
                                    .addOnSuccessListener(aVoid -> {
                                        // Afficher la tâche dans l'interface
                                        View taskView = addTaskToUI(title,  endDate, location, description, creatorName.get(),publicTask.getUserId(),taskId);
                                        taskView.setTag(taskId);
                                        showToast("Public Task Added");
                                    })
                                    .addOnFailureListener(e -> showToast("Error saving public task: " + e.getMessage()));
                        })
                        .addOnFailureListener(e -> showToast("Error fetching user name: " + e.getMessage()));
            } else {
                // Si le nom est déjà disponible dans Firebase Auth, on utilise ce nom
                PublicTaskModel publicTask = new PublicTaskModel(taskId, title, endDate, userId, location, description, creatorName.get());
                // Enregistrer la tâche dans Firestore
                db.collection("public_tasks").document(taskId).set(publicTask)
                        .addOnSuccessListener(aVoid -> {
                            // Afficher la tâche dans l'interface
                            View taskView = addTaskToUI(title, endDate, location, description, creatorName.get(),publicTask.getUserId(),taskId);
                            taskView.setTag(taskId);
                            showToast("Public Task Added");
                        })
                        .addOnFailureListener(e -> showToast("Error saving public task: " + e.getMessage()));
            }
        } else {
            showToast("User not authenticated");
        }
    }

    private boolean validateInput(EditText titleField, TextView endDateField, TextView locationField,EditText publicTaskDescription) {
        String title = titleField.getText().toString().trim();
        String description = publicTaskDescription.getText().toString().trim();  // Récupérer la description
        String endDateStr = endDateField.getText().toString().trim();
        String location = locationField.getText().toString().trim();

        if (title.isEmpty()) {
            showToast("Please enter a Title");
            return false;
        }
        if (endDateStr.isEmpty()) {
            showToast("Please select an End Date and Time");
            return false;
        }
        if (location.isEmpty()) {
            showToast("Please select an adress for this task");
            return false;
        }
        if (description.isEmpty()) {
            showToast("Please make a description for this Task");
            return false;
        }
        return true;
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
    private View addTaskToUI(String title, String endDate, String location, String description, String creatorName, String taskUserId, String taskId) {
        View taskView = LayoutInflater.from(this).inflate(R.layout.item_public_task, taskContainer, false);
        taskView.setBackgroundResource(isNightMode() ? R.drawable.background_dark : R.drawable.background_light);

        TextView taskTitle = taskView.findViewById(R.id.publictaskTitle);
        TextView taskDates = taskView.findViewById(R.id.publictaskDates);
        TextView taskLocation = taskView.findViewById(R.id.location);
        TextView creatorNameTextView = taskView.findViewById(R.id.creatorName);
        ImageView optionMenu = taskView.findViewById(R.id.publicoptionMenu);
        ScrollView descriptionScrollView = taskView.findViewById(R.id.descriptionScrollView);

        taskTitle.setText(title);
        taskDates.setText("Event Date: " + endDate);
        taskLocation.setText("Location: " + location);
        creatorNameTextView.setText("Created by: " + creatorName);

        TextView taskDescription = taskView.findViewById(R.id.description);
        taskDescription.setText("Description: " + description);
        descriptionScrollView.setVisibility(View.GONE);

        Button openMapButton = taskView.findViewById(R.id.openMapButton);
        openMapButton.setVisibility(View.GONE);
        openMapButton.setOnClickListener(v -> {
            String taskLocationText = taskLocation.getText().toString().replace("Location: ", "").trim();
            Intent intent = new Intent(Public_NoteActivity.this, Map.class);
            intent.putExtra("taskLocation", taskLocationText);
            startActivityForResult(intent, 2);
        });

        Button translateButton = taskView.findViewById(R.id.translateButton);
        String originalText = description.replace("Description: ", "").trim();
        new Translate(this, translateButton, taskTitle, taskDates, taskLocation, creatorNameTextView, taskDescription).setupTranslateButton();

        // Set the taskId as the tag on the participateButton
        Button participateButton = taskView.findViewById(R.id.participateButton);
        participateButton.setTag(taskId);  // Assign the taskId to the participateButton
        Participate participateHandler = new Participate(this, participateButton);
        participateHandler.setupParticipateButton();
        participateButton.setVisibility(View.GONE); // Hide the button by default

        taskView.setOnClickListener(v -> {
            boolean isVisible = descriptionScrollView.getVisibility() == View.VISIBLE;
            descriptionScrollView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            openMapButton.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            translateButton.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            participateButton.setVisibility(isVisible ? View.GONE : View.VISIBLE); // Show/hide the participate button
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.getUid().equals(taskUserId)) {
            optionMenu.setVisibility(View.GONE);  // Hide if not the creator
        } else {
            optionMenu.setVisibility(View.VISIBLE);  // Show if the creator
            optionMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(Public_NoteActivity.this, v);
                popupMenu.inflate(R.menu.menu_task_options);

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.optionModify) {
                        String currentTitle = taskTitle.getText().toString();
                        String currentDates = taskDates.getText().toString();
                        String currentLocation = taskLocation.getText().toString();
                        String currentDescription = taskDescription.getText().toString();
                        showEditTaskDialog(taskView, currentTitle, currentDates, currentLocation, currentDescription);
                    } else if (item.getItemId() == R.id.optionDelete) {
                        confirmAndDeleteTask(taskView);
                    } else {
                        return false;
                    }
                    return true;
                });
                popupMenu.show();
            });
        }

        taskContainer.addView(taskView, 0);
        return taskView;
    }
    private void showEditTaskDialog(View taskView, String currentTitle, String currentDates, String currentLocation,String currentDescription) {
        // Chargement du layout du dialogue
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_public_task, null);
        // Récupération des éléments du formulaire
        EditText editTaskTitle = dialogView.findViewById(R.id.editpublicTasktitle);
        EditText editPublicTaskDescription = dialogView.findViewById(R.id.editPublicTaskDescription);
        TextView editEndDate = dialogView.findViewById(R.id.textViewPublicTaskDeadline);
        TextView editTaskLocation = dialogView.findViewById(R.id.location);
        String creatorName = auth.getCurrentUser().getDisplayName();
        TextView map = dialogView.findViewById(R.id.locationTextView);
        // Remplir les champs avec les valeurs actuelles
        editTaskTitle.setText(currentTitle);
        editEndDate.setText(currentDates.replace("Event Date: ", ""));
        editTaskLocation.setText(currentLocation);
        editPublicTaskDescription.setText(currentDescription);
        // Redirection vers la carte pour modifier l'adresse
        map.setOnClickListener(v -> {
            Intent intent = new Intent(Public_NoteActivity.this, Map.class);
            startActivityForResult(intent, 2);  // Utiliser un autre code de demande (2 pour la modification)
        });

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
            String description = editPublicTaskDescription.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();
            String location = editTaskLocation.getText().toString().trim();
            // Validation des données
            if (validateInput(editTaskTitle, editEndDate, editTaskLocation,editPublicTaskDescription)) {
                // Si tout est valide, on met à jour Firestore et l'interface utilisateur
                String taskId = (String) taskView.getTag();  // Récupérer l'ID de la tâche à partir du tag
                // Création du modèle de la tâche mise à jour
                PublicTaskModel updatedTask = new PublicTaskModel(taskId, title, endDate, auth.getCurrentUser().getUid(), location,description,creatorName);
                // Mettre à jour la tâche dans Firestore
                db.collection("public_tasks").document(taskId).set(updatedTask)
                        .addOnSuccessListener(aVoid -> {
                            // Mise à jour de la vue de la tâche sur l'interface
                            TextView taskTitle = taskView.findViewById(R.id.publictaskTitle);
                            TextView taskDates = taskView.findViewById(R.id.publictaskDates);
                            TextView taskLocation = taskView.findViewById(R.id.location);
                            TextView taskDescription = taskView.findViewById(R.id.description);
                            taskTitle.setText(title);
                            taskDates.setText("Event Date: " + endDate);
                            taskLocation.setText(location);
                            taskDescription.setText(description);

                            showToast("Public Task Updated");
                        })
                        .addOnFailureListener(e -> {
                            showToast("Error updating public task: " + e.getMessage());
                        });

                alertDialog.dismiss();
            }
        });
        // Gestion des sélecteurs de date
        editEndDate.setOnClickListener(v -> showDateTimePicker(editEndDate));
    }
    private void confirmAndDeleteTask(View taskView) {
        // Récupérer l'ID de la tâche à partir du tag de la vue
        String taskId = (String) taskView.getTag();

        // Afficher une boîte de dialogue de confirmation
        new AlertDialog.Builder(Public_NoteActivity.this)
                .setTitle("Delete Task Confirmation")
                .setMessage("Are you sure you want to delete this task? no point will be given to participants")
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNightMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode", false);
    }
    @Override
    public void onAddressUpdated(String address) {
        locationTextView.setText(address);
    }
}

