package com.example.startxplanify.Main;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Translate {

    private Context context;
    private Button translateButton;
    private String originalText;

    public Translate(Context context, Button translateButton, String originalText) {
        this.context = context;
        this.translateButton = translateButton;
        this.originalText = originalText;
    }

    public void setupTranslateButton() {
        // Cache le bouton de traduction par défaut
        translateButton.setVisibility(View.GONE);

        // Définir un listener pour le bouton de traduction
        translateButton.setOnClickListener(v -> {
            String fakeTranslation = "[EN] " + originalText; // Traduction fictive
            Toast.makeText(context, "Traduction : " + fakeTranslation, Toast.LENGTH_SHORT).show();
        });
    }
}
