package com.example.dogpal.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.R;
import com.example.dogpal.adapter.FeedbackViewAdapter;
import com.example.dogpal.models.Feedback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrganizerFeedbackActivity extends AppCompatActivity {

    private RecyclerView feedbackRecyclerView;
    private FeedbackViewAdapter adapter;
    private List<Feedback> feedbackList = new ArrayList<>();
    private String eventId;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedbacks);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        eventId = getIntent().getStringExtra("eventId");

        feedbackRecyclerView = findViewById(R.id.rvFeedbackList);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyMessage = findViewById(R.id.emptyMessage);

        adapter = new FeedbackViewAdapter(this, feedbackList);
        feedbackRecyclerView.setAdapter(adapter);

        fetchFeedbacks();
    }

    private void fetchFeedbacks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .document(eventId)
                .collection("feedback")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    feedbackList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        // No feedback at all
                        emptyMessage.setVisibility(View.VISIBLE);
                        feedbackRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Hide the empty message since we have feedback to load
                        emptyMessage.setVisibility(View.GONE);
                        feedbackRecyclerView.setVisibility(View.VISIBLE);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Feedback feedback = doc.toObject(Feedback.class);
                        if (feedback != null) {
                            // Now fetch the user's profile information based on userId
                            String userId = feedback.getUserId(); // Assuming you store the userId in Feedback
                            fetchUserProfile(userId, feedback);
                        }
                      }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load feedback", Toast.LENGTH_SHORT).show());
    }

    private void fetchUserProfile(String userId, Feedback feedback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        String userProfileUrl = documentSnapshot.getString("profileImageUrl");

                        // Set the user's name and profile URL to the feedback object
                        feedback.setUserName(userName);
                        feedback.setUserProfileUrl(userProfileUrl);
                        // Add feedback to the list and notify the adapter
                        feedbackList.add(feedback);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show());
    }
}
