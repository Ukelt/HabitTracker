package com.example.mobiledevproject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = findViewById(R.id.editTextEmail);
        mPasswordField = findViewById(R.id.editTextPassword);
    }

    public void login(View view) {
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

        // Sign in the user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            Toast.makeText(LoginActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();

                            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_uid", uid);
                            editor.apply();
                            // Navigate to HabitsActivity
                            goToHabits();
                        } else {
                            // Login failed
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
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

    public void goToSignUp(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
        finish();
    }
}
