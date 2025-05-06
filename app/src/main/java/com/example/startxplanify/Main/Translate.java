package com.example.startxplanify.Main;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

public class Translate {

    private final Context context;
    private final Button translateButton;
    private final String originalText;

    public Translate(Context context, Button translateButton, String originalText) {
        this.context = context;
        this.translateButton = translateButton;
        this.originalText = originalText;
    }

    public void setupTranslateButton() {
        translateButton.setVisibility(View.GONE);

        translateButton.setOnClickListener(v -> showLanguageMenu());
    }

    private void showLanguageMenu() {
        PopupMenu popupMenu = new PopupMenu(context, translateButton);
        popupMenu.getMenu().add("English");
        popupMenu.getMenu().add("Dutch");
        popupMenu.getMenu().add("French");

        popupMenu.setOnMenuItemClickListener(item -> {
            handleTranslation(item.getTitle().toString());
            return true;
        });

        popupMenu.show();
    }

    private void handleTranslation(String language) {
        String prefix;

        if (language.equals("English")) {
            prefix = "[EN]";
        } else if (language.equals("Dutch")) {
            prefix = "[NL]";
        } else if (language.equals("French")) {
            prefix = "[FR]";
        } else {
            prefix = "[??]";
        }

        String translatedText = prefix + " " + originalText; // Traduction fictive
        Toast.makeText(context, "Traduction : " + translatedText, Toast.LENGTH_SHORT).show();
    }
}
