package com.example.reflexgame;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GameActivity extends AppCompatActivity {

    TextView countdownText;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        countdownText = findViewById(R.id.countdownText);

        startCountdown();
    }

    private void startCountdown() {
        int countdownSeconds = 3;

        new CountDownTimer(countdownSeconds * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000) + 1;
                countdownText.setText(String.valueOf(secondsRemaining));

                countdownText.setScaleX(0f);
                countdownText.setScaleY(0f);
                countdownText.setAlpha(0f);

                countdownText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }

            public void onFinish() {
                countdownText.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            countdownText.setText("");
                            startGame();
                        })
                        .start();
            }
        }.start();
    }

    private void startGame(){
        GameView gameView = new GameView(this, null);
        gameView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        setContentView(gameView);
    }

    public void gameOver(int score){
        Toast.makeText(this, "Game Over, Score: " + score, Toast.LENGTH_SHORT).show();
        if(user != null && !user.isAnonymous()){
            saveHighScore(score);
        }
        finish();
    }

    private void saveHighScore(int score){
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long highscore = documentSnapshot.getLong("highscore");
                if(highscore == null || score > highscore.intValue()){
                    docRef.update("highscore", score);
                    new NotificationHelper(this).sendHighScoreNotification(score);
                }

                Log.d(LoginActivity.LOG_TAG, "Highscore: " + highscore);
            } else {
                Log.d(LoginActivity.LOG_TAG, "No such document");
            }
        }).addOnFailureListener(e -> {
            Log.e(LoginActivity.LOG_TAG, "Failed to fetch document", e);
        });
    }

    public void vibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100);
        }
    }

}