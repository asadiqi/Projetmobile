package com.example.startxplanify.Main;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.startxplanify.R;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Mettre à jour la version dynamiquement
        TextView versionTextView = findViewById(R.id.textViewVersion);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionTextView.setText("Version: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}