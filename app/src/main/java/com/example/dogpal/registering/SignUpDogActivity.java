package com.example.dogpal.registering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpDogActivity extends AppCompatActivity {

    private Spinner spinnerGender, spinnerBreed;
    private EditText etDogName, etAge;
    private Button btnUploadVaccine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_dog);

        // Find the views
        spinnerGender = findViewById(R.id.spinnerGender);
        etDogName = findViewById(R.id.etDogName);
        etAge = findViewById(R.id.etAge);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        btnUploadVaccine = findViewById(R.id.btnUploadVaccine);


        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(SignUpDogActivity.this, "Please complete the registration first.", Toast.LENGTH_SHORT).show();
                // Don't call finish() or super — this blocks going back
            }
        });

        // Set up the spinner with gender options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Set up the spinner with breed options
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.breed_options,
                android.R.layout.simple_spinner_item
        );
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);


        // Handle register dog button click
        btnUploadVaccine.setOnClickListener(v -> {
            String dogName = etDogName.getText().toString();
            String dogAge = etAge.getText().toString();
            String dogBreed = spinnerBreed.getSelectedItem().toString();
            String dogGender = spinnerGender.getSelectedItem().toString();

            // Check if any field is empty
            if (dogName.isEmpty() || dogAge.isEmpty() || dogBreed.isEmpty() || dogGender.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if dog age is a valid number
            int dogAgeInt;
            try {
                dogAgeInt = Integer.parseInt(dogAge); // Try converting the dog age to an integer
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number for dog age.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dogAgeInt < 1 || dogAgeInt > 25) {
                Toast.makeText(this, "A dog that old? I wish that too! :)🐶💫", Toast.LENGTH_LONG).show();
                return;
            }

            // Save the dog's information in the database
            saveDogInfo(dogName, dogAgeInt, dogBreed , dogGender);
        });
    }
    private void saveDogInfo(String name, int  age, String breed, String gender) {
        // This is where you save the dog's details to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Assuming you have the current user's UID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> dogInfo = new HashMap<>();
        dogInfo.put("name", name);
        dogInfo.put("age", age);
        dogInfo.put("breed", breed);
        dogInfo.put("gender", gender);
        // Save to Firestore under "users" collection, with a sub-collection for dogs
        db.collection("users").document(userId).collection("dogs")
                .add(dogInfo)
                .addOnSuccessListener(documentReference -> {
                    // Success - Dog info is saved
                    String dogId = documentReference.getId(); // Get the dog document ID

                    Toast.makeText(SignUpDogActivity.this, "Good! Now upload Vaccination.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpDogActivity.this, UploadVaccineActivity.class);
                    intent.putExtra("dogId", dogId); // Pass the dog ID
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error - Handle failure
                    Toast.makeText(SignUpDogActivity.this, "Error registering dog", Toast.LENGTH_SHORT).show();
                });
    }
}
