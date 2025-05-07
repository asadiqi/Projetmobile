package com.example.startxplanify.Notes_Activity;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.startxplanify.R;

public class MyPublicTasks_Activity extends BaseNoteActivity {
    private Button buttonAddPublicTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initViews();
        setupListeners();
    }

    private void initViews() {
        buttonAddPublicTask = findViewById(R.id.button_addNote);
        setupDrawerAndNavigation();
    }

    private void setupListeners() {
        buttonAddPublicTask.setOnClickListener(v -> showAddPublicTaskDialog());
    }

    private void showAddPublicTaskDialog() {
        // Logic for showing dialog to add a public task will go here
    }
}
