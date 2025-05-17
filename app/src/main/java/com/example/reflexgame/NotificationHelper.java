package com.example.reflexgame;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "highscore_channel";
    private static final String CHANNEL_NAME = "High Score Alerts";
    private static final int NOTIF_ID = 2001;
    private Context context;
    private NotificationManager notifManager;

    public NotificationHelper(Context ctx) {
        this.context = ctx.getApplicationContext();
        notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannelIfNeeded();
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // importance default: makes a sound but no heads-up
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifies when you beat your high score");
            notifManager.createNotificationChannel(channel);
        }
    }

    /**
     * Call this when the user breaks their high score.
     * @param newScore the score they just achieved
     */
    public void sendHighScoreNotification(int newScore) {
        // For Android 13+ ensure permission granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Build an Intent to launch your MainActivity (or wherever you like)
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String title = "New High Score!";
        String text = "You scored " + newScore + " points!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // Use a built-in Android icon so you don't need any image assets
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notifManager.notify(NOTIF_ID, builder.build());
    }
}
