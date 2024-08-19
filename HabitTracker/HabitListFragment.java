package com.example.mobiledevproject;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import java.util.Calendar; // Import Calendar class
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import android.app.TimePickerDialog;
import com.google.firebase.firestore.FieldValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HabitListFragment extends Fragment {
    private static final String ARG_HABIT_TYPE = "habit_type";

    private String habitType;
    private LinearLayout habitsLayout;

    private HabitCheckboxListener habitCheckboxListener;
    private FirebaseFirestore db;

    public void setHabitCheckboxListener(HabitCheckboxListener listener) {
        this.habitCheckboxListener = listener;
    }

    public static HabitListFragment newInstance(String habitType) {
        HabitListFragment fragment = new HabitListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HABIT_TYPE, habitType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            habitType = getArguments().getString(ARG_HABIT_TYPE);
        }
        db = FirebaseFirestore.getInstance(); // Initialize Firebase Firestore
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.habit_list_fragment, container, false);
        habitsLayout = view.findViewById(R.id.habitsLayout);

        // Load habits from Firebase Firestore
        loadDefaultHabitsFromFirestore();

        return view;
    }

    private void loadDefaultHabitsFromFirestore() {
        db.collection("habits")
                .whereEqualTo("HabitType", habitType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String habitName = document.getString("HabitName");
                            boolean isChecked = Boolean.TRUE.equals(document.getBoolean("Checked"));
                            String notificationTime = document.getString("NotificationTime"); // Fetch notification time
                            addHabitCheckbox(habitName, isChecked, notificationTime); // Pass notification time to addHabitCheckbox
                        }
                    } else {
                        Log.d("HabitListFragment", "Error getting habits: ", task.getException());
                    }
                });
    }


    public void addHabitCheckbox(String habit, boolean checked, String notificationTime) {
        // Check if the habit is "Shower" or "Brush Teeth"
        boolean canDelete = !habit.equals("Shower") && !habit.equals("Brush Teeth");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a Button to show the selected time or open the time picker dialog
        Button timeButton = new Button(requireContext());
        if (notificationTime != null && !notificationTime.isEmpty()) {
            timeButton.setText("Notification Scheduled:\n" + notificationTime); // Set text if notification time exists
        } else {
            timeButton.setText("Schedule Notification");
        }
        timeButton.setTextSize(8);
        timeButton.setBackground(null); // Remove background color
        timeButton.setBackgroundResource(R.drawable.button_border); // Add border
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2.0f);
        timeButton.setLayoutParams(timeParams);
        layout.addView(timeButton);

        // Create a Button to clear the scheduled time
        Button clearButton = new Button(requireContext());
        clearButton.setText("Clear");
        clearButton.setTextSize(8);
        clearButton.setBackground(null);
        clearButton.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        clearButton.setLayoutParams(clearParams);
        layout.addView(clearButton);

        // CheckBox for habit
        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setText(habit);
        checkBox.setChecked(checked);
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        checkBox.setLayoutParams(checkParams);
        layout.addView(checkBox);

        if (canDelete) {
            // Create a Button to delete the habit
            Button deleteButton = new Button(requireContext());
            deleteButton.setText("X");
            deleteButton.setTextColor(getResources().getColor(android.R.color.white));
            deleteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            deleteButton.setPadding(-4, -4, -4, -4); // Add padding to make it circular
            deleteButton.setAllCaps(false); // Disable all caps for the button text
            deleteButton.setBackgroundResource(R.drawable.circular_button_background);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int sizeInPixels = (int) getResources().getDisplayMetrics().density * 24;
            deleteParams.width = sizeInPixels;
            deleteParams.height = sizeInPixels;
            deleteButton.setLayoutParams(deleteParams);
            deleteButton.setOnClickListener(v -> {
                // Remove habit from Firebase Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection("users")
                        .document(uid)
                        .collection("habits")
                        .whereEqualTo("HabitName", habit)
                        .whereEqualTo("HabitType", habitType)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                            } else {
                                Log.d("HabitListFragment", "Error deleting habit: ", task.getException());
                            }
                        });

                // Remove layout containing habit and delete button from layout
                habitsLayout.removeView(layout);
            });
            layout.addView(deleteButton);
        }

        habitsLayout.addView(layout);

        // Add a listener to handle checkbox state change
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (habitCheckboxListener != null) {
                habitCheckboxListener.onCheckboxStateChanged(habit, isChecked);
            }

            // Update the checked value in the database
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .collection("habits")
                    .whereEqualTo("HabitName", habit)
                    .whereEqualTo("HabitType", habitType)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().update("Checked", isChecked)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("HabitListFragment", "Checked value updated in Firestore");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("HabitListFragment", "Error updating checked value in Firestore", e);
                                        });
                            }
                        } else {
                            Log.d("HabitListFragment", "Error getting habit document: ", task.getException());
                        }
                    });
        });

        // Set onClickListener for timeButton
        timeButton.setOnClickListener(v -> showTimePickerDialog(timeButton, habitType, habit));

        // Set onClickListener for clearButton
        clearButton.setOnClickListener(v -> {
            // Clear the notification time for the habit
            clearNotificationTimeFromFirestore(habitType, habit, timeButton);
        });
    }


    private void showTimePickerDialog(Button timeButton, String habitType, String habitName) {
        final Calendar calendar = Calendar.getInstance();
        final TimePickerDialog[] timePickerDialog = {null};

        if (habitType.equals("Weekly")) {
            // For weekly habits, allow selecting a specific day and time
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth); // Update calendar with selected date

                // Show the TimePickerDialog
                timePickerDialog[0] = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                    String timeString = String.format("%02d:%02d", hourOfDay, minute);
                    String dayOfWeek = getDayOfWeek(); // Calculate day of week based on selected date
                    timeButton.setText("Notification Scheduled:\n" + timeString + " on " + dayOfWeek);
                    saveNotificationTimeToFirestore(habitType, habitName, timeString);
                }, 0, 0, false); // Set initial time to 00:00 and use 12-hour format

                timePickerDialog[0].show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        } else if (habitType.equals("Monthly")) {
            // For monthly habits, allow selecting a specific date and time
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);

                timePickerDialog[0] = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                    String timeString = String.format("%02d:%02d", hourOfDay, minute);
                    timeButton.setText("Notification Scheduled:\n" + timeString + " on " + dayOfMonth);
                    saveNotificationTimeToFirestore(habitType, habitName, timeString);
                }, 0, 0, false);

                timePickerDialog[0].show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        } else {
            // For daily habits, show the TimePickerDialog
            timePickerDialog[0] = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                String timeString = String.format("%02d:%02d", hourOfDay, minute);
                timeButton.setText("Notification Scheduled:\n" + timeString);
                saveNotificationTimeToFirestore(habitType, habitName, timeString);
            }, 0, 0, false);

            timePickerDialog[0].show();
        }
    }




    private void saveNotificationTimeToFirestore(String habitType, String habitName, String timeString) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Format the notification time based on the habit type
        String formattedTimeString;
        if (habitType.equals("Monthly")) {
            // For monthly habits, format the time as "24th 3:00"
            formattedTimeString = timeString + " on " + getDayOfMonth();
        } else if (habitType.equals("Weekly")) {
            // For weekly habits, format the time as "Tuesday 2:00"
            formattedTimeString = timeString + " on " + getDayOfWeek();
        } else {
            formattedTimeString = timeString;
        }

        final String finalFormattedTimeString = formattedTimeString;

        db.collection("users")
                .document(uid)
                .collection("habits")
                .whereEqualTo("HabitName", habitName)
                .whereEqualTo("HabitType", habitType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().update("NotificationTime", finalFormattedTimeString)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("HabitListFragment", "Notification time updated in Firestore");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("HabitListFragment", "Error updating notification time in Firestore", e);
                                    });
                        }
                    } else {
                        Log.d("HabitListFragment", "Error getting habit document: ", task.getException());
                    }
                });
    }

    private void clearNotificationTimeFromFirestore(String habitType, String habitName, Button timeButton) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("habits")
                .whereEqualTo("HabitName", habitName)
                .whereEqualTo("HabitType", habitType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().update("NotificationTime", FieldValue.delete())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("HabitListFragment", "Notification time cleared in Firestore");
                                        // Reset the text of the timeButton
                                        timeButton.setText("Schedule Notification");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("HabitListFragment", "Error clearing notification time in Firestore", e);
                                    });
                        }
                    } else {
                        Log.d("HabitListFragment", "Error getting habit document: ", task.getException());
                    }
                });
    }




    // Method to get the numbered day of the month
    private String getDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day >= 11 && day <= 13) {
            return day + "th";
        }
        switch (day % 10) {
            case 1: return day + "st";
            case 2: return day + "nd";
            case 3: return day + "rd";
            default: return day + "th";
        }
    }

    // Method to get the day of the week
    private String getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "";
        }
    }


    public void clearHabits() {
        habitsLayout.removeAllViews();
    }

    public void setHabitType(String habitType) {
        this.habitType = habitType;
    }

    public String getHabitType() {
        return habitType;
    }
}
