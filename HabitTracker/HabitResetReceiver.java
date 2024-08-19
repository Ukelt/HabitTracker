package com.example.mobiledevproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class HabitResetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "RESET_DAILY_HABITS":
                    resetDailyHabits();
                    break;
                case "RESET_WEEKLY_HABITS":
                    resetWeeklyHabits();
                    break;
                case "RESET_MONTHLY_HABITS":
                    resetMonthlyHabits();
                    break;
            }
        }
    }

    private void resetDailyHabits() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("habits")
                .whereEqualTo("HabitType", "Daily")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().update("Checked", false)
                                    .addOnSuccessListener(aVoid -> Log.d("HabitReset", "Daily habit reset"))
                                    .addOnFailureListener(e -> Log.e("HabitReset", "Error resetting daily habit", e));
                        }
                    }
                });
    }

    private void resetWeeklyHabits() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .collection("habits")
                    .whereEqualTo("HabitType", "Weekly")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().update("Checked", false)
                                        .addOnSuccessListener(aVoid -> Log.d("HabitReset", "Weekly habit reset"))
                                        .addOnFailureListener(e -> Log.e("HabitReset", "Error resetting weekly habit", e));
                            }
                        }
                    });
        }
    }

    private void resetMonthlyHabits() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .collection("habits")
                    .whereEqualTo("HabitType", "Monthly")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().update("Checked", false)
                                        .addOnSuccessListener(aVoid -> Log.d("HabitReset", "Monthly habit reset"))
                                        .addOnFailureListener(e -> Log.e("HabitReset", "Error resetting monthly habit", e));
                            }
                        }
                    });
        }
    }
}
