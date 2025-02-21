package com.example.startxplanify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.startxplanify.R;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in",false);

        if (isLoggedIn) {
            // Rediriger vers NoteActivity si l'utilisateur est connecté on affiche l'interface Note
            Intent intent = new Intent(MainActivity.this,NoteActivity.class);
            startActivity(intent);
            finish();

        } else { // si non on reste dans l'interface Main

            EdgeToEdge.enable(this);
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