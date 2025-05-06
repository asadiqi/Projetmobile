package com.example.startxplanify.Translate;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class Translate {

    private final Context context;
    private final Button translateButton;
    private final TextView taskTitle;
    private final TextView eventDate;
    private final TextView location;
    private final TextView creatorName;
    private final TextView description;
    private String currentLanguage = "EN"; // Set to English initially

    private static final String API_KEY = "450bd98f-2450-4a4a-9841-f3fbc9ea2703:fx";

    private final Map<String, String> languageCodeMap = new HashMap<String, String>() {{
        put("English", "EN");
        put("Nederlands", "NL");
        put("Français", "FR");
        put("Deutsch", "DE");
        put("Español", "ES");
        put("Italiano", "IT");
        put("Português", "PT");
        put("Polski", "PL");
        put("Русский", "RU");
        put("日本語", "JA");
        put("中文", "ZH");
        put("한국어", "KO");
        put("Türkçe", "TR");
        put("العربية", "AR");
        put("हिन्दी", "HI");
        put("ไทย", "TH");
        put("繁體中文", "ZH-TW");
        put("Romanian", "RO");
        put("Dansk", "DA");
        put("Svenska", "SV");
        put("Norsk", "NO");
        put("Suomi", "FI");
        put("Ελληνικά", "EL");
        put("Čeština", "CS");
        put("Slovenščina", "SL");
        put("Magyar", "HU");
        put("Български", "BG");
        put("Hrvatski", "HR");
        put("Latviešu", "LV");
        put("Lietuvių", "LT");
        put("Estonian", "ET");
        put("Українська", "UK");
        put("فارسی", "FA");
        put("Català", "CA");
        put("Bahasa Indonesia", "ID");
    }};

    public Translate(Context context, Button translateButton, TextView taskTitle, TextView eventDate, TextView location, TextView creatorName, TextView description) {
        this.context = context;
        this.translateButton = translateButton;
        this.taskTitle = taskTitle;
        this.eventDate = eventDate;
        this.location = location;
        this.creatorName = creatorName;
        this.description = description;
    }

    public void setupTranslateButton() {
        translateButton.setVisibility(View.GONE);
        translateButton.setOnClickListener(v -> showLanguageMenu());
    }

    private void showLanguageMenu() {
        PopupMenu popupMenu = new PopupMenu(context, translateButton);

        // Langues prioritaires en haut
        String[] priorityLanguages = {"English", "Nederlands", "Français"};
        for (String lang : priorityLanguages) {
            popupMenu.getMenu().add(lang);
        }

        // Ajout des autres langues (en excluant les prioritaires)
        for (String language : languageCodeMap.keySet()) {
            boolean isPriority = false;
            for (String pl : priorityLanguages) {
                if (pl.equals(language)) {
                    isPriority = true;
                    break;
                }
            }
            if (!isPriority) {
                popupMenu.getMenu().add(language);
            }
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            handleTranslation(item.getTitle().toString());
            return true;
        });

        popupMenu.show();
    }

    private void handleTranslation(String language) {
        // Get target language code based on the selected language
        String targetLang = languageCodeMap.getOrDefault(language, "EN");

        // Check if the language is already the current one
        if (targetLang.equals(currentLanguage)) {
            Toast.makeText(context, "Already in " + language, Toast.LENGTH_SHORT).show();
            return;
        }

        // If we are translating to French, we don't need a source language
        String sourceLang = currentLanguage.equals("FR") ? "FR" : "";

        if (targetLang.equals("FR")) {
            sourceLang = ""; // No source language needed for French
        }

        // Update the current language
        currentLanguage = targetLang;

        // Trigger translation
        new TranslateTask(context, taskTitle, eventDate, location, creatorName, description)
                .execute(targetLang, sourceLang);
    }
}

