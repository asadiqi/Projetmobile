package com.example.startxplanify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Récupération des éléments de l'interface
        editTextEmail =findViewById(R.id.editTextEmil);
        editTextPassword  = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(v -> loginUser());

        //Texte Forgot password click
        TextView textViewForgotPass = findViewById(R.id.textViewForgotPass);
        textViewForgotPass.setOnClickListener(v -> resetPassword());



        //Texte view don't have account et l'évenement de click
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView textViewSignUp = findViewById(R.id.textView_dont_have_account);
        textViewSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    // Méthode de connexion
    private void loginUser() {

        // on récupère les valeurs saisie par utilisateur
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // vérifécation des champs
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
          return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
           return;
        }

        //L'API Firebase Authentication est utilisée ici pour authentifier l'utilisateur avec son email et mot de passe
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null ) {
                            if (user.isEmailVerified()) {
                            // Si l'email est vérifié, enregistrer l'utilisateur dans Firestore
                            addUserToFirestore(user);

                            //on sauvgarder l'état de connexion dans SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("is_logged_in",true);
                                editor.apply();

                                // Rediriger vers NoteActivity
                                startActivity(new Intent(LoginActivity.this , Private_NoteActivity.class));
                                finish();

                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();

                        }
                            }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    // Méthode pour ajouter l'utilisateur dans Firestore
    //L'API Firebase Firestore est utilisée ici pour enregistrer les informations de l'utilisateur après la connexion
    private void addUserToFirestore(FirebaseUser user) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("email", user.getEmail());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> showToast("You are Loged in"))
                .addOnFailureListener(e -> showToast("Database error: " + e.getMessage()));
    }

    // Méthode pour afficher un message Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Affiche une boîte de dialogue pour demander l'email de l'utilisateur et envoyer un email de réinitialisation quand utilisateur click sur forgotPassword
    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        // Ajouter un champ de saisie pour l'email
        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showResetPassDialog();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Affiche un message après l'envoi de l'email de réinitialisation
    private void showResetPassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification")
                .setMessage("Please check your email and follow the instructions to reset your password.")
                .setPositiveButton("OK", (dialog, which) -> {})
                .setCancelable(false)
                .show();
    }

}