package com.example.dogpal.registering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dogpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etPhoneNumber;
    Button btnRegisterDog;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnRegisterDog = findViewById(R.id.btnRegisterDog);

        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(SignUpActivity.this, "Please complete the registration first.", Toast.LENGTH_SHORT).show();
                // Don't call finish() or super — this blocks going back
            }
        });


        btnRegisterDog.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name = etFullName.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.length() > 25) {
                Toast.makeText(SignUpActivity.this, "Name is too long. Max 25 characters allowed.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, "Password should be at least 6 characters.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!phone.matches("^012\\d{7}$")) {
                Toast.makeText(SignUpActivity.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
                return;
            }
            registerUser(email, password, name, phone);
        });
        findViewById(R.id.tvLoginPrompt).setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class))
        );
    }
    private void registerUser(String email, String password, String name, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registered successfully
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user, name, email, phone);
                        }
                    } else {
                        // Registration failed
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Signing up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }                    }
                });
    }

    private void saveUserData(FirebaseUser user, String name, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);

        // Save user data to Firestore under the 'users' collection
        firestore.collection("users").document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration and data upload successful
                        Toast.makeText(SignUpActivity.this, "Good! Now register your dog.", Toast.LENGTH_SHORT).show();
                        // Navigate to the next activity (e.g., Dog Registration)
                        Intent intent = new Intent(SignUpActivity.this, SignUpDogActivity.class);
                        intent.putExtra("userUid", user.getUid()); // You can pass UID if needed
                        startActivity(intent);
                        finish(); // Optional: closes the SignUpActivity

                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to save user data: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
        });
    }


}
