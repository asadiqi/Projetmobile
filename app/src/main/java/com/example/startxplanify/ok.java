package com.example.startxplanify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ok extends AppCompatActivity {
    private TextView textView7;  // Le TextView où l'adresse sera affichée

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ok);

        textView7 = findViewById(R.id.textView7);  // Assure-toi que c'est le bon TextView

        // Trouver la vue racine (par exemple, un ConstraintLayout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button button = findViewById(R.id.button2);

        // Définir l'action à faire lorsque le bouton est cliqué
        button.setOnClickListener(v -> {
            // Créer un Intent pour ouvrir l'activité Map
            Intent intent = new Intent(ok.this, Map.class);
            startActivityForResult(intent, 1);  // On démarre Map et attend un résultat
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérifie si le résultat provient de l'activité Map
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Récupérer l'adresse depuis l'intent
            String selectedLocation = data.getStringExtra("selectedLocation");

            if (selectedLocation != null) {
                // Mettre à jour le TextView avec l'adresse
                textView7.setText(selectedLocation);
            }
        }
    }
}
