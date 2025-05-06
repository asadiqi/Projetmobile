package com.example.startxplanify.Main;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;

public class Translate {

    private final Context context;
    private final Button translateButton;
    private final TextView taskTitle;
    private final TextView eventDate;
    private final TextView location;
    private final TextView creatorName;
    private final TextView description;

    private static final String API_KEY = "450bd98f-2450-4a4a-9841-f3fbc9ea2703:fx";

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
        String targetLang = "EN"; // Default to English
        if (language.equals("English")) {
            targetLang = "EN";
        } else if (language.equals("Dutch")) {
            targetLang = "NL";
        } else if (language.equals("French")) {
            targetLang = "FR";
        }

        // Traduire tous les champs en une fois
        new TranslateTask().execute(targetLang);
    }

    // AsyncTask pour effectuer la requête HTTP en arrière-plan
    private class TranslateTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            String targetLang = params[0];
            try {
                // URL de l'API DeepL
                String urlStr = "https://api-free.deepl.com/v2/translate";
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                // Préparer tous les champs à traduire
                String textToTranslate = taskTitle.getText().toString() + "\n" +
                        eventDate.getText().toString() + "\n" +
                        location.getText().toString() + "\n" +
                        creatorName.getText().toString() + "\n" +
                        description.getText().toString();

                // Paramètres de la requête
                String paramsStr = "auth_key=" + API_KEY + "&text=" + textToTranslate + "&source_lang=FR&target_lang=" + targetLang;

                // Envoyer la requête POST
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                OutputStream os = connection.getOutputStream();
                byte[] input = paramsStr.getBytes("utf-8");
                os.write(input, 0, input.length);

                // Lire la réponse
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse la réponse JSON pour extraire les textes traduits
                JSONObject responseObj = new JSONObject(response.toString());
                String translatedText = responseObj.getJSONArray("translations").getJSONObject(0).getString("text");

                // Séparer les textes traduits pour chaque champ
                return translatedText.split("\n");

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] translatedTexts) {
            if (translatedTexts != null && translatedTexts.length == 5) {
                // Mettre à jour tous les TextViews avec leurs traductions respectives
                taskTitle.setText(translatedTexts[0]);
                eventDate.setText(translatedTexts[1]);
                location.setText(translatedTexts[2]);
                creatorName.setText(translatedTexts[3]);
                description.setText(translatedTexts[4]);

                Toast.makeText(context, "Traduction réussie!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Erreur de traduction", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
