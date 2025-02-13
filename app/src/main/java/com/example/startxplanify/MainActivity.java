package com.example.startxplanify;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private  MyViewModel myViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode",false);

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_land);

        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.getMyData().observe(this,data -> {
            TextView textView = findViewById(R.id.textView);
            textView.setText(data);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

      //okk
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch themeSwitch = findViewById(R.id.switch1);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night_mode",isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(isChecked? AppCompatDelegate.MODE_NIGHT_YES: AppCompatDelegate.MODE_NIGHT_NO);
        });
    }




}