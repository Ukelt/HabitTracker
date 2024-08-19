package com.example.mobiledevproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "HABIT_NOTIFICATIONS";
    private static final int DAILY_NOTIFICATION_ID = 1;
    private static final int WEEKLY_NOTIFICATION_ID = 2;
    private static final int MONTHLY_NOTIFICATION_ID = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "DAILY_NOTIFICATION":
                    checkHabitsAndNotify(context, "daily", DAILY_NOTIFICATION_ID, "You have unchecked daily habits!");
                    break;
                case "WEEKLY_NOTIFICATION":
                    checkHabitsAndNotify(context, "weekly", WEEKLY_NOTIFICATION_ID, "You have unchecked weekly habits!");
                    break;
                case "MONTHLY_NOTIFICATION":
                    checkHabitsAndNotify(context, "monthly", MONTHLY_NOTIFICATION_ID, "You have unchecked monthly habits!");
                    break;
            }
        }
    }

    private void checkHabitsAndNotify(Context context, String habitType, int notificationId, String notificationText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("habits")
                .whereEqualTo("HabitType", habitType)
                .whereEqualTo("Checked", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        sendNotification(context, notificationId, notificationText);
                    }
                });
    }

    private void sendNotification(Context context, int notificationId, String text) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Habit Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Habit Reminder")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Create an Intent to launch the app when the notification is clicked
        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // Show the notification
        notificationManager.notify(notificationId, builder.build());
    }
}

