package com.example.reflexgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class LeaderBoardActivity extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        db = FirebaseFirestore.getInstance();

        loadTopHunderedPlayerScores();

        ImageView logoutIcon = findViewById(R.id.backIcon);
        logoutIcon.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, MainMenuActivity.class);
            startActivity(intent);
        });

        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderBoardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopHunderedPlayerScores();
    }


    private void loadTopHunderedPlayerScores(){
        ArrayList<User> users = new ArrayList<>();
        LinearLayout leaderboardContainer = findViewById(R.id.leaderboardContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        db.collection("users").orderBy("highscore", Query.Direction.DESCENDING).limit(100).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(DocumentSnapshot document : task.getResult()){
                    User user = document.toObject(User.class);

                    users.add(user);
                }

                leaderboardContainer.removeAllViews();

                for (int i = 0; i < users.size(); i++) {
                    View itemView = inflater.inflate(R.layout.list_item_leaderboard, leaderboardContainer, false);

                    TextView rankText = itemView.findViewById(R.id.textViewRank);
                    TextView usernameText = itemView.findViewById(R.id.textViewUsername);
                    TextView highscoreText = itemView.findViewById(R.id.textViewHighscore);

                    String rankFormat = i + 1 + ". ";

                    rankText.setText(rankFormat);
                    usernameText.setText(users.get(i).getUsername());
                    highscoreText.setText(String.valueOf(users.get(i).getHighscore()));

                    leaderboardContainer.addView(itemView);
                }
            }
        });

    }
}