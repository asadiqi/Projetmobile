package com.example.startxplanify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // Les éléments de signup activité
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonSignUp;

    // Déclaration de Firebase Authentication
    private FirebaseAuth firebaseAuth;

    // Déclaration de la DB Firestore pour enregistrer les utilisateurs
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Variables globales pour stocker les informations de l'utilisateur
    private String name, email, password, confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sigup);

        // Récupère les éléments d'interface signup
        editTextName = findViewById(R.id.nameInput);
        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword = findViewById(R.id.passwordInput);
        editTextConfirmPassword = findViewById(R.id.confirmPasswordInput);
        buttonSignUp = findViewById(R.id.signupButton);

        // Signup button pour valider
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSignUp();
            }
        });

        // Authentification
        firebaseAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Cette méthode valide les informations saisies par l'utilisateur lors de l'inscription.
    private void validateAndSignUp() {
         name = editTextName.getText().toString().trim();
         email = editTextEmail.getText().toString().trim();
         password = editTextPassword.getText().toString().trim();
         confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Utilisation des méthodes isEmpty et isValidEmail pour vérifier les champs
        if (isEmpty(name, "Please Enter Your Name")) return;
        if (isEmpty(email, "Please Enter an Email")) return;
        if (!isValidEmail(email)) return;
        if (isEmpty(password, "Please Enter a Password")) return;
        if (password.length() < 6) {
            showToast("Password must contain at least 6 characters");
            return;
        }
        if (isEmpty(confirmPassword, "Please Confirm Your Password")) return;
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        //Api de firebase fetchSignInMethodsForEmail(email) → Vérifie si l'email est déjà utilisé.
        // Vérifie si l'email existe déjà dans Firebase
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Si l'email est déjà enregistré
                        if (!task.getResult().getSignInMethods().isEmpty()) {
                            showToast("This email is already registered. Please use another one.");
                        } else {
                            // Si l'email n'est pas encore enregistré, créer l'utilisateur
                            createUserWithEmailAndPassword(email, password, name);
                        }
                    } else {
                        showToast("Error checking email: " + task.getException().getMessage());
                    }
                });
    }

    //Api firebase createUserWithEmailAndPassword(email, password) → Crée un nouvel utilisateur dans Firebase.
    // Méthode pour créer un utilisateur avec email et mot de passe
    private void createUserWithEmailAndPassword(String email, String password, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Envoi de l'email de vérification
                        firebaseAuth.getCurrentUser().sendEmailVerification()
                                .addOnSuccessListener(aVoid -> {
                                    // Afficher un dialogue pour informer l'utilisateur
                                    showVerificationDialog();
                                    // L'utilisateur n'est pas encore enregistré dans Firestore
                                })
                                .addOnFailureListener(e -> showToast("Failed to send verification email: " + e.getMessage()));
                    } else {
                        showToast("Registration failed: " + task.getException().getMessage());
                    }

                });

    }


    private void  showVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification")
                .setMessage("Please check your email for the verification link.")
                .setPositiveButton("ok", ((dialog, which) -> {
                    // Une fois que l'utilisateur ferme le dialogue, rediriger vers l'écran de login
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();  // Fermer l'activité actuelle pour éviter de revenir à l'écran d'inscription
                }))
                .setCancelable(false)
                .show();
    }

    // Méthode pour vérifier que le champ n'est pas vide
    private boolean isEmpty(String field, String message) {
        if (TextUtils.isEmpty(field)) {
            showToast(message);
            return true;
        }
        return false;
    }

    // Méthode pour vérifier la validité de l'email
    private boolean isValidEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please Enter a valid email");
            return false;
        }
        return true;
    }

    // Méthode pour afficher un message Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    }


