package com.example.reflexgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    TextView usernameTextView;
    TextView highscoreTextView;
    TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();

        usernameTextView = findViewById(R.id.textViewUsername);
        highscoreTextView = findViewById(R.id.textViewHighscore);
        emailTextView = findViewById(R.id.textViewEmail);

        if(user != null && !user.isAnonymous()){
            displayUserInformation();
        } else{
            usernameTextView.setText("Guest");
            highscoreTextView.setText("Log in to save highscore");
            emailTextView.setText("-------");
        }

    }

    private void displayUserInformation() {
        DocumentReference document = db.collection("users").document(user.getUid());

        document.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);

            usernameTextView.setText(user.getUsername());
            setRankingInTheWorld(user.getHighscore());
            emailTextView.setText(user.getEmail());

        });
    }

    public void backButton(View view) {
        finish();
    }

    public void resetButton(View view) {
        if(user != null && !user.isAnonymous()){
            db.collection("users").document(user.getUid()).delete();
            mAuth.signOut();
            goToLogin();
        } else{
            Toast.makeText(this, "Can't reset guest profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToLogin(){
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void setRankingInTheWorld(int highScore){

        ArrayList<User> users = new ArrayList<>();

        AtomicInteger rank = new AtomicInteger(-1);

        AtomicBoolean rankSet = new AtomicBoolean(false);

        db.collection("users").orderBy("highscore", Query.Direction.DESCENDING).limit(1000).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(DocumentSnapshot document : task.getResult()){
                    User user = document.toObject(User.class);
                    users.add(user);
                }


                for (int i = 0; i < users.size(); i++) {
                    if(users.get(i).getUid().equals(user.getUid())){
                        rank.set(i + 1);

                        String rankFormat = highScore + " points, "+ rank.get() + ". (In the world)";

                        highscoreTextView.setText(rankFormat);

                        rankSet.set(true);
                    }
                }
                if(!rankSet.get()){
                    String rankFormat = highScore + " points";

                    highscoreTextView.setText(rankFormat);
                }
            }
        });

    }
}