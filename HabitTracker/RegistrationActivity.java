package com.example.mobiledevproject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = findViewById(R.id.editTextEmail);
        mPasswordField = findViewById(R.id.editTextPassword);
    }

    public void signUp(View view) {
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        // Validate email and password
        if (!isValidEmail(email)) {
            mEmailField.setError("Invalid email address");
            return;
        }

        if (!isValidPassword(password)) {
            mPasswordField.setError("Password must be at least 6 characters long");
            return;
        }

        // Sign up the user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration success
                            Toast.makeText(RegistrationActivity.this, "Registration success.",
                                    Toast.LENGTH_SHORT).show();

                            // Save user credentials to device's password manager
                            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_uid", uid);
                            editor.apply();

                            // Create default habits for the user
                            createDefaultHabits();

                            // Navigate to HabitsActivity
                            goToHabits();
                        } else {
                            // Registration failed
                            Toast.makeText(RegistrationActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    public void goToHabits() {
        Intent intent = new Intent(this, HabitsActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void createDefaultHabits() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create default habits document for the user
        Map<String, Object> showerHabit = new HashMap<>();
        showerHabit.put("HabitName", "Shower");
        showerHabit.put("HabitType", "Daily");
        showerHabit.put("Checked", false);

        // Add the "Shower" habit document
        db.collection("users").document(uid).collection("habits").document("shower")
                .set(showerHabit)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        } else {
                        }
                    }
                });

        Map<String, Object> brushTeethHabit = new HashMap<>();
        brushTeethHabit.put("HabitName", "Brush Teeth");
        brushTeethHabit.put("HabitType", "Daily");
        brushTeethHabit.put("Checked", false);

        // Add the "Brush Teeth" habit document
        db.collection("users").document(uid).collection("habits").document("brush_teeth")
                .set(brushTeethHabit)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        } else {
                        }
                    }
                });
    }

}

