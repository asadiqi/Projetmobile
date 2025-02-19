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

        //utilisation des methode isEmpty et isvalidEmail pour verifie les champs
        if (isEmpty(name,"Please Enter Your Name")) return;
        if (isEmpty(email,"Please Enter an Email")) return;
        if (!isValidEmail(email)) return;
        if (isEmpty(password,"Please Enter a Password")) return;
        if (password.length()<6) {
            showToast("Password must contain at least 6 characters");
            return;
        }
        if (isEmpty(confirmPassword,"Please Confirm Your Password")) return;
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }
        // Inscription réussie
        showToast("Registration successful");
    }

    //Methode pour vérifier que le champs n'es pas vide
    private boolean isEmpty(String field,String message) {

        if (TextUtils.isEmpty(field)) {
            showToast(message);
            return true;
        }
        return false;

    }

    //Methode pour vérifier la validité d'email
    private boolean isValidEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please Enter a valid email");
            return false;
        }
        return true;
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}