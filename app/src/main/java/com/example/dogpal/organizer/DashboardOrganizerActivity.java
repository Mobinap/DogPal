package com.example.dogpal.organizer;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.BaseActivity;
import com.example.dogpal.R;
import com.example.dogpal.adapter.EventAdapter;
import com.example.dogpal.models.Event;
import com.example.dogpal.report.ReportActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardOrganizerActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> eventLauncher;
    RecyclerView eventsRecyclerView;
    Button btnUpcoming, btnCancelled, btnPassed;  // Buttons for the 3 categories
    FirebaseFirestore db;
    FirebaseUser user;
    String userId;
    EventAdapter adapter;
    List<Event> eventList;
    List<String> eventIds;
    List<Event> filteredEventsList;
    private Date eventDateTime ;
    private Date currentDateTime ;
    private String eventDateTimeString;
    private SimpleDateFormat dateFormat ;
    Intent intent;
    private TextView emptyMessage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_dashboard_organizer);
        //setupLayoutWithNav(R.layout.activity_dashboard_organizer);
       // bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        ImageView menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> {
            intent = new Intent(DashboardOrganizerActivity.this, ReportActivity.class);
            intent.putExtra("reportType", "combined");
            startActivity(intent);
        });

        emptyMessage = findViewById(R.id.emptyMessage);

        // Initialize Buttons
        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnCancelled = findViewById(R.id.btnCancelled);
        btnPassed = findViewById(R.id.btnPassed);

        // Initialize the launcher
        eventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                            fetchUserCreatedEvents(userId);  // Refresh the events
                    }
                });

        findViewById(R.id.btn_create_event).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardOrganizerActivity.this, CreateEventActivity.class);
            eventLauncher.launch(intent);
        });


        // RecyclerView setup
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // or getContext() if Fragment


        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();


        db = FirebaseFirestore.getInstance();
         eventList = new ArrayList<>();
        filteredEventsList = new ArrayList<>();

        adapter = new EventAdapter(this, eventList, userId, true);

        //adapter = new EventAdapter(this, eventList);
        eventsRecyclerView.setAdapter(adapter);

        // Set click listeners for buttons
        btnUpcoming.setOnClickListener(v -> changeTabSelection("Upcoming"));
        btnCancelled.setOnClickListener(v -> changeTabSelection("Cancelled"));
        btnPassed.setOnClickListener(v -> changeTabSelection("Passed"));

        fetchUserCreatedEvents(userId);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    private void fetchUserCreatedEvents(String userId) {

        db.collection("users").document(userId)
                .collection("eventsCreated")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                         eventIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            eventIds.add(document.getId());  // Get the eventId from each document
                        }
                        if (!eventIds.isEmpty()) {
                            Log.d("FirestoreDebug", "Events created by user: " + eventIds.toString());
                            fetchEventsByIds(eventIds);  // Fetch only the events the user created
                        } else {
                            Log.d("FirestoreDebug", "No events created by this user.");
                        }
                    } else {
                        Log.d("FirestoreDebug", "No eventsCreated subcollection for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error fetching eventsCreated subcollection", e);
                });
    }
    private void fetchEventsByIds(List<String> eventIds) {
        db.collection("events")
                .whereIn(FieldPath.documentId(), eventIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                            Log.d("EVENT", "Loaded event: " + event.getEventTitle() + " on " + event.getEventDate());
                            Log.d("EVENT", "Total events loaded: " + eventList.size());


                        }
                        changeTabSelection("Upcoming");  // Set default filter
                    } else {
                        Log.w("Firestore", "Error getting events", task.getException());
                    }
                });
    }


    // Method to handle button selection and changing colors
    private void changeTabSelection(String selectedStatus) {
        // Reset all buttons to unselected state
        resetTabs();

        // Update the UI based on the selected category
        switch (selectedStatus) {
            case "Upcoming":
                btnUpcoming.setSelected(true);
                btnUpcoming.setTextColor(getResources().getColor(android.R.color.white));
                btnUpcoming.setBackgroundResource(R.drawable.tab_button_bg);
                filterEvents("Upcoming");
                break;
            case "Cancelled":
                btnCancelled.setSelected(true);
                btnCancelled.setTextColor(getResources().getColor(android.R.color.white));
                btnCancelled.setBackgroundResource(R.drawable.tab_button_bg);
                filterEvents("Cancelled");
                break;
            case "Passed":
                btnPassed.setSelected(true);
                btnPassed.setTextColor(getResources().getColor(android.R.color.white));
                btnPassed.setBackgroundResource(R.drawable.tab_button_bg);
                filterEvents("Passed");
                break;
        }
    }

    private void resetTabs() {
        // Reset all buttons to unselected state
        btnUpcoming.setSelected(false);
        btnUpcoming.setTextColor(getResources().getColor(R.color.tab_button_text_selector));
        btnUpcoming.setBackgroundColor(getResources().getColor(android.R.color.white));

        btnCancelled.setSelected(false);
        btnCancelled.setTextColor(getResources().getColor(R.color.tab_button_text_selector));
        btnCancelled.setBackgroundColor(getResources().getColor(android.R.color.white));

        btnPassed.setSelected(false);
        btnPassed.setTextColor(getResources().getColor(R.color.tab_button_text_selector));
        btnPassed.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void filterEvents(String status) {
        filteredEventsList.clear();
        // Filter the events based on the category
        for (Event event : eventList) {
            switch (status) {
                case "Upcoming":  if (isUpcoming(event)) { filteredEventsList.add(event); } break;
                case "Cancelled":  if (isCancelled(event)) { filteredEventsList.add(event); } break;
                case "Passed":  if (isPassed(event)) { filteredEventsList.add(event); } break;
            }
        }
// Show/hide empty message
        if (filteredEventsList.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
        // Notify the adapter to update the RecyclerView with the filtered events
        //adapter.notifyDataSetChanged();
        adapter.updateEventList(filteredEventsList);

    }


    private boolean isUpcoming(Event event) {
        if (isCancelled(event)) {
            return false; // Don't consider canceled events as passed
        }
        eventDateTime = getEventDateTime(event);
         currentDateTime = new Date();
        return eventDateTime != null && eventDateTime.after(currentDateTime);
    }

    private boolean isCancelled(Event event) {
        // Logic to check if the event is cancelled (based on eventStatus)
        return event.getStatus().equalsIgnoreCase("Cancelled");
    }

    private boolean isPassed(Event event) {
        if (isCancelled(event)) {
            return false; // Don't consider canceled events as passed
        }
        eventDateTime = getEventDateTime(event);
         currentDateTime = new Date();
        return eventDateTime != null && eventDateTime.before(currentDateTime);
    }
    private Date getEventDateTime(Event event) {
        try {
             eventDateTimeString = event.getEventDate() + " " + event.getEventTime();
             dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            return dateFormat.parse(eventDateTimeString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




}
