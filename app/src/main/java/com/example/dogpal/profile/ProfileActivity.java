package com.example.dogpal.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.BaseActivity;
import com.example.dogpal.R;
import com.example.dogpal.adapter.DogAdapter;
import com.example.dogpal.models.Dog;
import com.example.dogpal.registering.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> editProfileLauncher;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView userName;
    private ImageView profileImage;
    private RecyclerView dogRecyclerView;
    private List<Dog> dogList;
    private DogAdapter dogAdapter;

    private ProgressBar progressBarLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);
        //getLayoutInflater().inflate(R.layout.profile_view, findViewById(R.id.container));
        // Highlight the Profile icon
        //bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userName = findViewById(R.id.userName);
        profileImage = findViewById(R.id.profileImage);
        dogRecyclerView = findViewById(R.id.dogListRecyclerView);

        // Initialize the RecyclerView for displaying the list of dogs
        dogList = new ArrayList<>();
        dogAdapter = new DogAdapter(this, dogList);
        dogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogRecyclerView.setAdapter(dogAdapter);

        progressBarLoading = findViewById(R.id.progressBarLoading);



        loadUserProfile();

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadUserProfile(); // Reload Firestore user & dog data
                    }
                }
        );

        //edit button
        findViewById(R.id.editProfileBtn).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
            editProfileLauncher.launch(intent); //  Launch with result
        });

        //logout button
        findViewById(R.id.logoutBtn).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, WelcomeActivity.class))
        );
    }

    private void loadUserProfile() {
        progressBarLoading.setVisibility(View.VISIBLE);

        String userId = auth.getCurrentUser().getUid();
        // Fetch user profile data (e.g. name and profile image) from users collection
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                userName.setText(name != null ? name : "User");

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {

                    Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile_placeholder)
                            .into(profileImage);
                }
            }
        });
        // Fetch user's dog data from the "dogs" subcollection
        userRef.collection("dogs").get().addOnSuccessListener(queryDocumentSnapshots -> {
            dogList.clear();
            // Convert each document into a Dog object and add to list
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Dog dog = doc.toObject(Dog.class);
                dogList.add(dog);
            }
            dogAdapter.notifyDataSetChanged();


            progressBarLoading.setVisibility(View.GONE);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load dogs", Toast.LENGTH_SHORT).show();
            progressBarLoading.setVisibility(View.GONE); // Hide progress bar even on failure
        });
    }

}
