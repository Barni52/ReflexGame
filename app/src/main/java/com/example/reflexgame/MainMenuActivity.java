package com.example.reflexgame;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.webkit.WebView;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class MainMenuActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private WebView guideWebView;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();


        if(!checkUserId()){
            goToLogin();
            finish();
            return;
        }

        if(!user.isAnonymous()){
            usernameExists();
        }
        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        logoutIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });


        guideWebView = findViewById(R.id.guideWebView);

        // Keep navigation inside the WebView
        guideWebView.setWebViewClient(new WebViewClient());

        // Load the external guide URL
        guideWebView.loadUrl("https://barni52.github.io/");

        checkNotificationPermission();

        setDailyReminder(this);

    }

    public void logout(View view){
        goToLogin();
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

    private boolean checkUserId(){
        if(user != null){
            Log.d(LoginActivity.LOG_TAG, "User is logged in(from main)");
            return true;
        } else{
            Log.d(LoginActivity.LOG_TAG, "User is not logged in(from main)");
            return false;
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
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void goToLeaderBoard(View view) {
        Intent intent = new Intent(this, LeaderBoardActivity.class);
        startActivity(intent);
    }

    public void gotToGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }


    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            } else {
                // Permission already granted
            }
        } else {
            // Permission not required on Android < 13
        }
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

}