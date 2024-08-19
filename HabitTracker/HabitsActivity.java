package com.example.mobiledevproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class HabitsActivity extends AppCompatActivity implements HabitCheckboxListener {
    private static final String TAG = "HabitsActivity";
    private EditText editTextHabit;
    private Spinner habitTypeSpinner;
    private ViewPager2 viewPager;
    private List<HabitListFragment> habitFragments;
    private int currentTabPosition = 0; // Track the current tab position

    // Shared preferences
    private SharedPreferences sharedPreferences;

    @Override
    public void onCheckboxStateChanged(String habit, boolean isChecked) {
        Log.d(TAG, "Checkbox state changed: " + habit + " -> " + isChecked);
        // Get the habit type from the currently selected tab
        String habitType = (String) habitTypeSpinner.getSelectedItem();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sharedPrefsName = "UserHabits_" + uid;
        SharedPreferences userPrefs = getSharedPreferences(sharedPrefsName, MODE_PRIVATE);

        // Save the checkbox state to SharedPreferences
        Set<String> checkedHabits = userPrefs.getStringSet(habitType.toLowerCase() + "CheckedHabits", new HashSet<>());
        if (isChecked) {
            checkedHabits.add(habit);
        } else {
            checkedHabits.remove(habit);
        }

        userPrefs.edit().putStringSet(habitType.toLowerCase() + "CheckedHabits", checkedHabits).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        editTextHabit = findViewById(R.id.editTextHabit);
        habitTypeSpinner = findViewById(R.id.habitTypeSpinner);
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Get the user-specific SharedPreferences file
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sharedPrefsName = "UserHabits_" + uid;
        sharedPreferences = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);

        // Set up the habit type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.habit_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        habitTypeSpinner.setAdapter(adapter);

        // Set up the habit fragments
        habitFragments = new ArrayList<>();
        HabitListFragment dailyFragment = HabitListFragment.newInstance("Daily");
        dailyFragment.setHabitType("Daily");
        habitFragments.add(dailyFragment);

        HabitListFragment weeklyFragment = HabitListFragment.newInstance("Weekly");
        weeklyFragment.setHabitType("Weekly");
        habitFragments.add(weeklyFragment);

        HabitListFragment monthlyFragment = HabitListFragment.newInstance("Monthly");
        monthlyFragment.setHabitType("Monthly");
        habitFragments.add(monthlyFragment);

        // Set up the ViewPager and TabLayout
        viewPager.setAdapter(new HabitPagerAdapter(this, habitFragments));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(habitFragments.get(position).getHabitType());
        }).attach();

        // Listen for tab changes
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                // Load habits for the selected tab
                loadHabitsForSelectedTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Load habits for the initially selected tab
        loadHabitsForSelectedTab();
        AlarmSetupUtility.scheduleHabitResetAlarms(this);
        NotificationSetupUtility.scheduleNotificationAlarms(this);
    }

    // Method to load habits for the selected tab
    private void loadHabitsForSelectedTab() {
        Log.d(TAG, "Loading habits for selected tab: " + currentTabPosition);
        if (currentTabPosition < habitFragments.size()) {
            HabitListFragment selectedFragment = habitFragments.get(currentTabPosition);
            if (selectedFragment != null && selectedFragment.isAdded()) {
                // Clear existing habits in the selected fragment
                selectedFragment.clearHabits();
                // Load habits for the selected tab
                loadHabits(selectedFragment.getHabitType());
            }
        }
    }

    // Method to load habits from Firestore
    private void loadHabits(String habitType) {
        Log.d(TAG, "Loading habits of type: " + habitType);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).collection("habits")
                .whereEqualTo("HabitType", habitType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Set<String> loadedHabits = new HashSet<>(); // Set to track loaded habits
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> habitData = document.getData();
                                String habitName = (String) habitData.get("HabitName");
                                boolean checked = (boolean) habitData.get("Checked");
                                String notificationTime = (String) habitData.get("NotificationTime");
                                // Add habit if it's not already loaded
                                if (!loadedHabits.contains(habitName)) {
                                    loadedHabits.add(habitName);
                                    // Find the fragment by habit type
                                    HabitListFragment fragment = getFragmentByHabitType(habitType);
                                    // If fragment exists and is currently added, add the habit checkbox
                                    if (fragment != null && fragment.isAdded()) {
                                        fragment.addHabitCheckbox(habitName, checked, notificationTime);
                                    }
                                }
                            }
                            // Schedule notifications for unchecked habits
                            scheduleNotificationsForUncheckedHabits(habitType);
                        } else {
                            Log.e(TAG, "Error getting habits: ", task.getException());
                        }
                    }
                });
    }

    // Method to schedule notifications based on habit type and time for unchecked habits
    private void scheduleNotificationsForUncheckedHabits(String habitType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).collection("habits")
                .whereEqualTo("HabitType", habitType)
                .whereEqualTo("Checked", false) // Only select unchecked habits
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String habitName = document.getString("HabitName");
                            String notificationTime = document.getString("NotificationTime");

                            // Check if notificationTime is not null or empty
                            if (notificationTime != null && !notificationTime.isEmpty()) {
                                switch (habitType) {
                                    case "Daily":
                                        scheduleDailyNotification(habitName, notificationTime);
                                        Log.d(TAG,"Scheduled Notification for habit: " + habitName + " at " + notificationTime);
                                        break;
                                    case "Weekly":
                                        scheduleWeeklyNotification(habitName, notificationTime);
                                        Log.d(TAG,"Scheduled Notification for habit: " + habitName + " at " + notificationTime);
                                        break;
                                    case "Monthly":
                                        scheduleMonthlyNotification(habitName, notificationTime);
                                        Log.d(TAG,"Scheduled Notification for habit: " + habitName + " at " + notificationTime);
                                        break;
                                }
                            } else {
                                Log.d(TAG, "Skipping scheduling notification for habit: " + habitName + " as notificationTime is null or empty");
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting unchecked habits: ", task.getException());
                    }
                });
    }


    public void addHabit(View view) {
        String habit = editTextHabit.getText().toString().trim();
        if (!habit.isEmpty()) {
            String habitType = (String) habitTypeSpinner.getSelectedItem();
            HabitListFragment fragment = getFragmentByHabitType(habitType);
            if (fragment != null) {
                fragment.addHabitCheckbox(habit, false, "");
                editTextHabit.setText("");

                // Save habit to SharedPreferences
                saveHabit(habit, habitType);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed, loading habits for selected tab");
        // Load habits for the currently selected tab when the activity resumes
        loadHabitsForSelectedTab();
    }

    private void saveHabit(String habit, String habitType) {
        Log.d(TAG, "Saving habit: " + habit + ", type: " + habitType);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sharedPrefsName = "UserHabits_" + uid;
        SharedPreferences userPrefs = getSharedPreferences(sharedPrefsName, MODE_PRIVATE);

        // Update SharedPreferences
        Set<String> habits = userPrefs.getStringSet(habitType.toLowerCase() + "Habits", new HashSet<>());
        habits.add(habit);
        userPrefs.edit().putStringSet(habitType.toLowerCase() + "Habits", habits).apply();

        // Save habit to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> habitData = new HashMap<>();
        habitData.put("HabitName", habit);
        habitData.put("HabitType", habitType);
        habitData.put("Checked", false); // Assuming the habit is initially unchecked

        db.collection("users")
                .document(uid)
                .collection("habits")
                .add(habitData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Habit added to Firestore: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding habit to Firestore", e);
                    }
                });
    }

    public void logout(View view) {
        Log.d(TAG, "Logging out");
        // Remove user UID from shared preferences
        SharedPreferences appPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = appPrefs.edit();
        editor.remove("user_uid");
        editor.apply();

        // Navigate back to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private HabitListFragment getFragmentByHabitType(String habitType) {
        for (HabitListFragment fragment : habitFragments) {
            if (fragment.getHabitType().equals(habitType)) {
                return fragment;
            }
        }
        return null;
    }

    public void showNotification(View view) {
        NotificationHelper.showBasicNotification(this);
    }

    private static class HabitPagerAdapter extends FragmentStateAdapter {
        private final List<HabitListFragment> habitFragments;

        public HabitPagerAdapter(FragmentActivity fa, List<HabitListFragment> habitFragments) {
            super(fa);
            this.habitFragments = habitFragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return habitFragments.get(position);
        }

        @Override
        public int getItemCount() {
            return habitFragments.size();
        }
    }

    private void scheduleDailyNotification(String habitName, String time) {
        Log.d(TAG, "Scheduling daily notification for habit: " + habitName + " at " + time);
        try {
            Calendar calendar = Calendar.getInstance();
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            scheduleNotification(habitName, calendar);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling daily notification", e);
        }
    }

    private void scheduleWeeklyNotification(String habitName, String time) {
        Log.d(TAG, "Scheduling weekly notification for habit: " + habitName + " at " + time);
        try {
            Calendar calendar = Calendar.getInstance();
            String[] parts = time.split(" on ");
            String[] timeParts = parts[0].split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            int dayOfWeek = getDayOfWeek(parts[1]);

            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            scheduleNotification(habitName, calendar);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling weekly notification", e);
        }
    }

    private void scheduleMonthlyNotification(String habitName, String time) {
        Log.d(TAG, "Scheduling monthly notification for habit: " + habitName + " at " + time);
        try {
            Calendar calendar = Calendar.getInstance();
            String[] parts = time.split(" on ");
            String dayOfMonthString = parts[1].substring(0, parts[1].length() - 2); // Remove last two characters
            String[] timeParts = parts[0].split(":");

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            int dayOfMonth = Integer.parseInt(dayOfMonthString);


            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.MONTH, 1);
            }

            scheduleNotification(habitName, calendar);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling monthly notification", e);
        }
    }

    private void scheduleNotification(String habitName, Calendar calendar) {
        Intent intent = new Intent(this, NotificationHelper.class);
        intent.putExtra("habitName", habitName);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE; // Add FLAG_IMMUTABLE for Android 12 and higher
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, habitName.hashCode(), intent, flags);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    // Prevent error on older android devices with scheduling exact alarms
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    Log.e("HabitsActivity", "Cannot schedule exact alarms. Scheduling non-exact alarm instead.");
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }



    private int getDayOfWeek(String day) {
        Log.d(TAG, "Getting day of week for: " + day);
        switch (day.toLowerCase()) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: throw new IllegalArgumentException("Invalid day of the week: " + day);
        }
    }
}
