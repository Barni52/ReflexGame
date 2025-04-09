package com.example.reflexgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegistrationActivity extends AppCompatActivity {

    EditText userNameED;
    EditText emailED;
    EditText passwordED;
    EditText confirmPasswordED;
    RelativeLayout loadingLayout;

    private FirebaseAuth mAuth;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setElements();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    public void backToLogin(View view) {
        finish();
    }

    public void register(View view) {

        loadingLayout.setVisibility(View.VISIBLE);

        String userNameString = String.valueOf(userNameED.getText());
        String emailString = String.valueOf(emailED.getText());
        String passwordString = String.valueOf(passwordED.getText());
        String confirmPasswordString = String.valueOf(confirmPasswordED.getText());

        if(userNameString.isEmpty() || emailString.isEmpty() || passwordString.isEmpty() || confirmPasswordString.isEmpty()){
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            loadingLayout.setVisibility(View.GONE);
            return;
        }

        if(!passwordString.equals(confirmPasswordString)){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            loadingLayout.setVisibility(View.GONE);
            return;
        }

        checkUsernameUniquenessAndSaveToDB(userNameString, emailString, passwordString);


    }

    private void checkUsernameUniquenessAndSaveToDB(String userName, String email, String password){

        db.collection("users")
                .whereEqualTo("username", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            saveUserToAuth(userName, email, password);
                        } else {
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                            loadingLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(LoginActivity.LOG_TAG, "Error checking username", task.getException());
                    }
                });
    }

    private void saveUserToAuth(String userNameString, String emailString, String passwordString){
        Log.i(LoginActivity.LOG_TAG, "Registered as: " + userNameString + " " + emailString + " " + passwordString);
        mAuth.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(this, task -> {
            loadingLayout.setVisibility(View.GONE);
            if(task.isSuccessful()){
                Log.d(LoginActivity.LOG_TAG, "createUserWithEmail:success: " + emailString);
                saveUserToDatabase(userNameString, emailString);

            } else {
                Log.w(LoginActivity.LOG_TAG, "createUserWithEmail:failure", task.getException());
                showErrorToUser(Objects.requireNonNull(task.getException()));
            }
        });
    }

    private void saveUserToDatabase(String userName, String email){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            String uid = user.getUid();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", uid);
            userMap.put("username", userName);
            userMap.put("email", email);
            userMap.put("highscore", 0);

            db.collection("users").document(uid).set(userMap).addOnCompleteListener(aVoid -> {
                        Log.d(LoginActivity.LOG_TAG, "Username saved successfully.");
                        goToMainMenu();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(LoginActivity.LOG_TAG, "Error saving username", e);
                    });
        } else{
            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showErrorToUser(Exception e){
        switch (e.getClass().getSimpleName()){
            case "FirebaseAuthInvalidCredentialsException":
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseAuthInvalidUserException":
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseAuthUserCollisionException":
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseNetworkException":
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                break;
            case "FirebaseAuthWeakPasswordException":
                Toast.makeText(this, "Passwords must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void setElements(){
        userNameED = findViewById(R.id.editTextUserName);
        emailED = findViewById(R.id.editTextEmail);
        passwordED = findViewById(R.id.editTextPassword);
        confirmPasswordED = findViewById(R.id.editTextConfirmPassword);
        loadingLayout = findViewById(R.id.loadingLayout);

    }

    private void goToMainMenu(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);

    }
}