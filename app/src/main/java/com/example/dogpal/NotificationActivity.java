package com.example.dogpal;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.adapter.NotificationAdapter;
import com.example.dogpal.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> markNotificationsAsReadAndFinish());

        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Notification note = doc.toObject(Notification.class);
                            notificationList.add(note);
                        }
                        adapter.notifyDataSetChanged();
                    });
        }
    }

    private void markNotificationsAsReadAndFinish() {
        if (currentUser == null) {
            finish();
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit().addOnCompleteListener(task -> finish());
                })
                .addOnFailureListener(e -> finish());
    }

}

