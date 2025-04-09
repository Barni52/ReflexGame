package com.example.reflexgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.bolts.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainMenuActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();


        checkUserId();

        if(!user.isAnonymous()){
            usernameExists();
        }
    }

    private void usernameExists() {

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(LoginActivity.LOG_TAG, "Good username");
                } else {
                    Log.d(LoginActivity.LOG_TAG, "No username");
                    goToUsernameSelection();
                }
            } else{
                Log.d(LoginActivity.LOG_TAG, "Error getting documents: ", task.getException());
                finish();
            }
        });

    }

    private void checkUserId(){
        if(user != null){
            Log.d(LoginActivity.LOG_TAG, "User is logged in(from main)");
        } else{
            Log.d(LoginActivity.LOG_TAG, "User is not logged in(from main)");
            finish();
        }
    }

    private void goToUsernameSelection(){
        Intent intent = new Intent(this, SelectUsernameActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        goToLogin();

        super.onBackPressed();
    }

    private void goToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void deleteAccount(View view){
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "User account deleted.");
                            goToLogin();
                        } else {
                            Log.e("Firebase", "Account deletion failed", task.getException());
                        }
                    });
        }
    }
}