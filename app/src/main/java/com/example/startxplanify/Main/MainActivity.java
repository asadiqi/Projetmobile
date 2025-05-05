package com.example.startxplanify.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.startxplanify.Notes_Activity.Private_NoteActivity;
import com.example.startxplanify.R;
import com.example.startxplanify.Signup_and_Login.LoginActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in",false);

        if (isLoggedIn) {
            // Rediriger vers NoteActivity si l'utilisateur est connecté on affiche l'interface Note
            Intent intent = new Intent(MainActivity.this, Private_NoteActivity.class);
            startActivity(intent);
            finish();

        } else { // si non on reste dans l'interface Main

            setContentView(R.layout.activity_main_land);
        }

        // button de get start et l'éevenment de click
        Button getStartButton = findViewById(R.id.button);
        getStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }




}