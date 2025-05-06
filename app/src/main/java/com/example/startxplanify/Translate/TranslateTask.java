package com.example.startxplanify.Translate;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class TranslateTask extends AsyncTask<String, Void, String[]> {

    private final TextView taskTitle;
    private final TextView eventDate;
    private final TextView location;
    private final TextView creatorName;
    private final TextView description;
    private final String API_KEY = "450bd98f-2450-4a4a-9841-f3fbc9ea2703:fx";
    private final Context context;

    // Constructeur
    public TranslateTask(Context context, TextView taskTitle, TextView eventDate, TextView location,
                         TextView creatorName, TextView description) {
        this.context = context;
        this.taskTitle = taskTitle;
        this.eventDate = eventDate;
        this.location = location;
        this.creatorName = creatorName;
        this.description = description;
    }


    @Override
    protected String[] doInBackground(String... params) {
        String targetLang = params[0];
        String sourceLang = params[1];

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

            // Si la langue source est vide (par exemple pour le français), ne pas spécifier source_lang
            String paramsStr = "auth_key=" + API_KEY + "&text=" + textToTranslate + "&target_lang=" + targetLang;
            if (!sourceLang.isEmpty()) {
                paramsStr += "&source_lang=" + sourceLang;
            }

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

