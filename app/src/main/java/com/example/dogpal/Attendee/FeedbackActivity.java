package com.example.dogpal.Attendee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.R;
import com.example.dogpal.organizer.OrganizerFeedbackActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText commentBox;
    private Button submitButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        ratingBar = findViewById(R.id.ratingBar);
        commentBox = findViewById(R.id.commentBox);
        submitButton = findViewById(R.id.submitButton);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get eventId passed via Intent
        eventId = getIntent().getStringExtra("eventId");

        submitButton.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comment = commentBox.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || eventId == null) {
            Toast.makeText(this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> feedback = new HashMap<>();
        feedback.put("userId", currentUser.getUid());
        feedback.put("rating", rating);
        feedback.put("comment", comment);
        feedback.put("timestamp", FieldValue.serverTimestamp());

        // Add with auto-generated ID
        db.collection("events")
                .document(eventId)
                .collection("feedback")
                .add(feedback)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FeedbackActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    // Navigate to ViewAllFeedbackActivity (same as OrganizerFeedbackActivity)
                    Intent intent = new Intent(FeedbackActivity.this, OrganizerFeedbackActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                    finish(); // close the feedback form
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FeedbackActivity.this, "Failed to submit feedback.", Toast.LENGTH_SHORT).show();
                });
    }
}
