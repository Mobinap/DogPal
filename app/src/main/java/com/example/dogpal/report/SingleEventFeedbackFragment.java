package com.example.dogpal.report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dogpal.R;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



import java.util.Locale;

public class SingleEventFeedbackFragment extends Fragment {

    private TextView averageRatingText, totalFeedbackCount;
    private ProgressBar progressBar5, progressBar4, progressBar3, progressBar2, progressBar1, progressBarLoading;
    private TextView count5, count4, count3, count2, count1;
    private String eventId ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_event_feedback_view, container, false);



        averageRatingText = view.findViewById(R.id.averageRatingText);
        totalFeedbackCount = view.findViewById(R.id.totalFeedbackCount);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);

        progressBar5 = view.findViewById(R.id.progressBar5Stars);
        progressBar4 = view.findViewById(R.id.progressBar4Stars);
        progressBar3 = view.findViewById(R.id.progressBar3Stars);
        progressBar2 = view.findViewById(R.id.progressBar2Stars);
        progressBar1 = view.findViewById(R.id.progressBar1Star);

        count5 = view.findViewById(R.id.count5Stars);
        count4 = view.findViewById(R.id.count4Stars);
        count3 = view.findViewById(R.id.count3Stars);
        count2 = view.findViewById(R.id.count2Stars);
        count1 = view.findViewById(R.id.count1Star);

        // Get event ID
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
             eventId = args.getString("eventId");
            fetchFeedbackAndPopulate(eventId);
        }

        return view;

    }

    private void fetchFeedbackAndPopulate(String eventId) {
        progressBarLoading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .document(eventId)
                .collection("feedback")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No feedback found, set defaults
                        setDefaults();
                        return;
                    }

                    int totalCount = 0;
                    int sumRatings = 0;

                    // Counters for each star rating
                    int count1Star = 0;
                    int count2Star = 0;
                    int count3Star = 0;
                    int count4Star = 0;
                    int count5Star = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Long ratingLong = doc.getLong("rating");
                        if (ratingLong == null) continue; // skip if no rating

                        int rating = ratingLong.intValue();
                        sumRatings += rating;
                        totalCount++;

                        switch (rating) {
                            case 1: count1Star++; break;
                            case 2: count2Star++; break;
                            case 3: count3Star++; break;
                            case 4: count4Star++; break;
                            case 5: count5Star++; break; }}

                    if (totalCount == 0) {
                        setDefaults();
                        return; }

                    // Calculate average rating
                    float averageRating = (float) sumRatings / totalCount;

                    // Create final copies for lambda usage
                    final float avg = averageRating;
                    final int total = totalCount;
                    final int c1 = count1Star, c2 = count2Star, c3 = count3Star, c4 = count4Star, c5 = count5Star;

                    requireActivity().runOnUiThread(() -> {
                        updateUI(avg, total, c1, c2, c3, c4, c5);
                        progressBarLoading.setVisibility(View.GONE);
                    });


                })
                .addOnFailureListener(e -> {
                    setDefaults();
                });
    }

    private void setDefaults() {
        requireActivity().runOnUiThread(() -> {
            averageRatingText.setText("Average Rating: N/A");
            totalFeedbackCount.setText("Total Feedbacks: 0");

            progressBar1.setProgress(0);
            progressBar2.setProgress(0);
            progressBar3.setProgress(0);
            progressBar4.setProgress(0);
            progressBar5.setProgress(0);

            count1.setText("0");
            count2.setText("0");
            count3.setText("0");
            count4.setText("0");
            count5.setText("0");
        });
    }

    private void updateUI(float average, int totalCount,
                          int c1, int c2, int c3, int c4, int c5) {

        averageRatingText.setText(String.format(Locale.getDefault(), "Average Rating: %.1f / 5", average));
        totalFeedbackCount.setText("Total Feedbacks: " + totalCount);

        // Calculate percentages for progress bars
        int p1 = (int) ((c1 * 100f) / totalCount);
        int p2 = (int) ((c2 * 100f) / totalCount);
        int p3 = (int) ((c3 * 100f) / totalCount);
        int p4 = (int) ((c4 * 100f) / totalCount);
        int p5 = (int) ((c5 * 100f) / totalCount);

        progressBar1.setProgress(p1);
        progressBar2.setProgress(p2);
        progressBar3.setProgress(p3);
        progressBar4.setProgress(p4);
        progressBar5.setProgress(p5);

        count1.setText(String.valueOf(c1));
        count2.setText(String.valueOf(c2));
        count3.setText(String.valueOf(c3));
        count4.setText(String.valueOf(c4));
        count5.setText(String.valueOf(c5));
    }
}
