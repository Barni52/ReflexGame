package com.example.reflexgame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SelectUsernameActivity extends AppCompatActivity {

    EditText usernameEditText;
    FirebaseUser user;
    FirebaseFirestore db;

    RelativeLayout loadingLayout;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_username);

        setElements();

        user = FirebaseAuth.getInstance().getCurrentUser();

        checkUserId();

        db = FirebaseFirestore.getInstance();
    }

    public void setUsername(View view) {
        String username = usernameEditText.getText().toString();
        checkUsernameUniquenessAndSaveToDB(username, user.getEmail());

    }



    private void checkUsernameUniquenessAndSaveToDB(String userName, String email){
        db.collection("users")
                .whereEqualTo("username", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d(LoginActivity.LOG_TAG, "Username started to save successfully.");
                            saveUserToDatabase(userName, email);
                        } else {
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                            loadingLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(LoginActivity.LOG_TAG, "Error checking username", task.getException());
                    }
                });
    }

    private void saveUserToDatabase(String userName, String email){
        if(user != null){
            String uid = user.getUid();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", uid);
            userMap.put("username", userName);
            userMap.put("email", email);
            userMap.put("highscore", 0);
            Log.d(LoginActivity.LOG_TAG, "Username started to save successfully 2.");

            db.collection("users").document(uid).set(userMap).addOnCompleteListener(aVoid -> {
                    Log.d(LoginActivity.LOG_TAG, "Username saved successfully.");
                    goToMainMenu();

                    })
                    .addOnFailureListener(e -> {
                        Log.w(LoginActivity.LOG_TAG, "Error saving username", e);
                    });
        } else{
            Toast.makeText(this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setElements() {
        usernameEditText = findViewById(R.id.usernameEditText);
        loadingLayout = findViewById(R.id.loadingLayout);
    }

    private void goToMainMenu(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }
    private void checkUserId(){
        if(user != null){
            Log.d(LoginActivity.LOG_TAG, "User is logged in (from select)");
        } else{
            Log.d(LoginActivity.LOG_TAG, "User is not logged in (from select)");
            goToMainMenu();
        }
    }

    //fake error, do ignore
    @Override
    public void onBackPressed () {
        goToLogin();

        super.onBackPressed();
    }

    private void goToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}