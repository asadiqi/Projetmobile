package com.example.startxplanify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Patterns;

public class SignUpActivity extends AppCompatActivity {

    // Les élement de signup activite

    private EditText editTextName,editTextEmail,editTextPassword,editTextConfirmPassword;
    private Button buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sigup);

        // Récupère les élement d'interface signup

        editTextName = findViewById(R.id.nameInput);
        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword =findViewById(R.id.passwordInput);
        editTextConfirmPassword = findViewById(R.id.confirmPasswordInput);
        buttonSignUp=findViewById(R.id.signupButton);

        // Signup button pour valider
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSignUp();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    //Cette méthode valide les informations saisies par l'utilisateur lors de l'inscription.
    private void validateAndSignUp() {

        String name = editTextName.getText().toString().trim(); // Cette ligne récupère le texte entré par l'utilisateur dans le champ editTextName, puis utilise trim() pour supprimer les espaces vides au début et à la fin du texte. elle stocke le nom de l'utilisateur sans espaces inutiles.
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter an Email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification de la validité de l'email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter a Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification de la longueur du mot de passe
        if (password.length() < 6) {
            Toast.makeText(this, "Password must contain at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please Confirm Your Password", Toast.LENGTH_SHORT).show();
            return;
        }


        // Vérification que les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }


        // Inscription réussie
        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
    }

}