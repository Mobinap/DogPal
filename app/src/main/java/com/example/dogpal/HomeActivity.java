package com.example.dogpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.organizer.DashboardOrganizerActivity;
import com.example.dogpal.profile.ProfileActivity;
import com.example.dogpal.Attendee.DashboardAttendeeActivity;
import com.example.dogpal.Attendee.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends BaseActivity {

    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View notificationDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //setContentView(R.layout.activity_home);

        setupLayoutWithNav(R.layout.activity_home);
       //bottomNavigationView.setSelectedItemId(R.id.nav_home);

        findViewById(R.id.ivProfile).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class))
        );
        findViewById(R.id.btnSearchEvents).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class))
        );
        findViewById(R.id.btnOrganizeEvents).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, DashboardOrganizerActivity.class))
        );
        findViewById(R.id.notificationIcon).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class))
        );
        notificationDot = findViewById(R.id.notification_dot);

        // for greeting
        tvGreeting = findViewById(R.id.tvGreeting);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String name = document.getString("name");
                        tvGreeting.setText("Hi, " + name + "!");
                    } else {
                        tvGreeting.setText("Hi!");
                    }
                } else {
                    tvGreeting.setText("Hi!");
                }
            });

            // Initial notification dot check (optional, can also just rely on onResume)
            checkUnreadNotifications(userId);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        // Refresh notification dot when returning to this screen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            checkUnreadNotifications(userId);
        }
    }

    private void checkUnreadNotifications(String userId) {
        db.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        notificationDot.setVisibility(View.VISIBLE);
                    } else {
                        notificationDot.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    notificationDot.setVisibility(View.GONE);
                });
    }
}
