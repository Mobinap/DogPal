package com.example.dogpal.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.R;
import com.example.dogpal.adapter.DogAdapter;
import com.example.dogpal.models.Dog;
import com.example.dogpal.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView userName;
    private ImageView profileImage;
    private RecyclerView dogRecyclerView;
    private List<Dog> dogList;
    private DogAdapter dogAdapter;
    private String userId;
    private User user;
    private FirebaseUser currentUser;
    private String viewerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);


        // Get userId passed from intent
        userId = getIntent().getStringExtra("userId");
        // get the user viewing the profiles participation status
        viewerStatus = getIntent().getStringExtra("viewerParticipationStatus");

        if (userId == null) {
            Toast.makeText(this, "User ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        db = FirebaseFirestore.getInstance();
         currentUser = FirebaseAuth.getInstance().getCurrentUser();


        // Views
        userName = findViewById(R.id.userName);
        profileImage = findViewById(R.id.profileImage);
        dogRecyclerView = findViewById(R.id.dogListRecyclerView);
        ImageView backButton = findViewById(R.id.backButton);

        // Hide edit & logout
        findViewById(R.id.editProfileBtn).setVisibility(View.GONE);
        findViewById(R.id.logoutBtn).setVisibility(View.GONE);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Dog list setup
        dogList = new ArrayList<>();
        dogAdapter = new DogAdapter(this, dogList);
        dogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogRecyclerView.setAdapter(dogAdapter);




        // Load user data
        loadUserProfile();
    }

    private void loadUserProfile() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                user = documentSnapshot.toObject(User.class);

                String name = documentSnapshot.getString("name");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                userName.setText(name != null ? name : "User");

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile_placeholder)
                            .into(profileImage);
                }

                if (currentUser != null && !currentUser.getUid().equals(userId)
                        && ("attending".equalsIgnoreCase(viewerStatus) || "organizer".equalsIgnoreCase(viewerStatus))
                ) {
                    // Only show contact button if viewing someone else's profile AND their participation status is "attending"

                    Button contactBtn = new Button(this);
                    contactBtn.setText("Contact");
                    contactBtn.setTextColor(Color.WHITE);
                    contactBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00ED14")));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 24, 0, 0);
                    contactBtn.setLayoutParams(params);

                    LinearLayout scrollContent = findViewById(R.id.scrollContent);
                    scrollContent.addView(contactBtn);

                    contactBtn.setOnClickListener(v -> {
                        // Get the raw phone number of the user being viewed
                        String rawPhone = user.getPhone();
                        String phoneNumber;
                        // If the phone number starts with "0", convert it to Malaysia's international format
                        if (rawPhone != null && rawPhone.startsWith("0")) {
                            phoneNumber = "6" + rawPhone.substring(1); // Convert to Malaysia international format
                        } else { // Use the phone number as-is
                            phoneNumber = rawPhone;
                        }
                        // Create the WhatsApp API URL with the phone number and encoded message
                        String message = "Hi, I saw you on DogPal!";
                        String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
                        // Create an intent to open a URL to WhatsApp
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        try {
                            v.getContext().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(v.getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        userRef.collection("dogs").get().addOnSuccessListener(queryDocumentSnapshots -> {
            dogList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Dog dog = doc.toObject(Dog.class);
                dogList.add(dog);
            }
            dogAdapter.notifyDataSetChanged();
        });
    }
}
