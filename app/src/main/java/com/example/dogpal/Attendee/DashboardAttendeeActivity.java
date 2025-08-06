package com.example.dogpal.Attendee;

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

import com.example.dogpal.R;
import com.example.dogpal.adapter.AttendeeEventAdapter;
import com.example.dogpal.models.Event;
import com.example.dogpal.models.JoinedEventWrapper;
import com.example.dogpal.organizer.DashboardOrganizerActivity;
import com.example.dogpal.report.ReportActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardAttendeeActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> eventLauncher;
    RecyclerView eventsRecyclerView;
    Button btnUpcoming, btnCancelled, btnPassed;  // Buttons for the 3 categories
    FirebaseFirestore db;
    FirebaseUser user;
    String userId;
    List<JoinedEventWrapper> joinedEventList = new ArrayList<>();
    AttendeeEventAdapter adapter;
    List<Event> filteredEventsList;
    private Date eventDateTime ;
    private Date currentDateTime ;
    private String eventDateTimeString;
    private SimpleDateFormat dateFormat ;
    Intent intent;
    TextView emptyMessage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_dashboard_organizer);
       // setupLayoutWithNav(R.layout.activity_dashboard_organizer);
       // bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        ImageView menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setVisibility(View.GONE);

        TextView dashboardTitle = findViewById(R.id.dashboardTitle);
        dashboardTitle.setText("Your Attended Events");  // Update title for attendee

        LinearLayout btnCreateEvent = findViewById(R.id.btn_create_event);
        btnCreateEvent.setVisibility(View.GONE);  // Hide the button

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
                        fetchUserJoinedEvents(userId);  // Refresh the events
                    }
                });


        // RecyclerView setup
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // or getContext() if Fragment

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        db = FirebaseFirestore.getInstance();
        joinedEventList = new ArrayList<>();
        filteredEventsList = new ArrayList<>();

        adapter = new AttendeeEventAdapter(this, joinedEventList, userId, true);

        //adapter = new EventAdapter(this, eventList);
        eventsRecyclerView.setAdapter(adapter);

        // Set click listeners for buttons
        btnUpcoming.setOnClickListener(v -> changeTabSelection("Upcoming"));
        btnCancelled.setOnClickListener(v -> changeTabSelection("Cancelled"));
        btnPassed.setOnClickListener(v -> changeTabSelection("Passed"));

        fetchUserJoinedEvents(userId);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
    private void fetchUserJoinedEvents(String userId) {
        db.collection("users").document(userId)
                .collection("joinedEvents")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<JoinedEventWrapper> wrapperList = new ArrayList<>();
                    List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshots) {
                        String eventId = doc.getString("eventId");
                        String participationStatus = doc.getString("participationStatus");

                        if (eventId != null && participationStatus != null) {
                            Task<DocumentSnapshot> task = db.collection("events").document(eventId).get();
                            eventTasks.add(task);

                            task.addOnSuccessListener(eventDoc -> {
                                if (eventDoc.exists()) {
                                    Event event = eventDoc.toObject(Event.class);
                                    event.setEventId(eventDoc.getId());
                                    wrapperList.add(new JoinedEventWrapper(event, participationStatus));
                                }
                            });
                        }
                    }
                   // wait for all event fetch tasks to finish before updating UI
                    Tasks.whenAllComplete(eventTasks)
                            .addOnSuccessListener(tasks -> {
                                joinedEventList.clear();
                                joinedEventList.addAll(wrapperList);
                                changeTabSelection("Upcoming"); // Filter after data is ready
                            });
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch joinedEvents", e));
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
        List<JoinedEventWrapper> filteredList = new ArrayList<>();

        for (JoinedEventWrapper wrapper : joinedEventList) {
            Event event = wrapper.getEvent();
            String participationStatus = wrapper.getParticipationStatus();

            switch (status) {
                case "Upcoming":
                    if (isUpcoming(event, participationStatus)) filteredList.add(wrapper);
                    break;
                case "Cancelled":
                    if (isCancelled(event, participationStatus)) filteredList.add(wrapper);
                    break;
                case "Passed":
                    if (isPassed(event, participationStatus)) filteredList.add(wrapper);
                    break;
            }
        }
        // Show/hide empty message based on result
        if (filteredList.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            eventsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            eventsRecyclerView.setVisibility(View.VISIBLE);
        }

        adapter.updateJoinedEventList(filteredList);
    }



    private boolean isUpcoming(Event event, String participationStatus) {
        if (isCancelled(event, participationStatus)) return false;

         eventDateTime = getEventDateTime(event);
         currentDateTime = new Date();

        return eventDateTime != null && eventDateTime.after(currentDateTime)
                && "Attending".equalsIgnoreCase(participationStatus);
    }


    private boolean isCancelled(Event event, String participationStatus) {
        return "Cancelled".equalsIgnoreCase(event.getStatus())
                || "Cancelled".equalsIgnoreCase(participationStatus);
    }

    private boolean isPassed(Event event, String participationStatus) {
        if (isCancelled(event, participationStatus)) return false;

        Date eventDateTime = getEventDateTime(event);
        Date currentDateTime = new Date();

        return eventDateTime != null && eventDateTime.before(currentDateTime)
                && !"Cancelled".equalsIgnoreCase(participationStatus);
    }

    private Date getEventDateTime(Event event) {
        try {
            eventDateTimeString = event.getEventDate() + " " + event.getEventTime();
            dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            return dateFormat.parse(eventDateTimeString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }}

        // Method to update the event list in the adapter
        public void updateJoinedEventList(List<JoinedEventWrapper> updatedList) {
            this.joinedEventList.clear();
            this.joinedEventList.addAll(updatedList);
            adapter.notifyDataSetChanged();  // Notify adapter about data change
        }

}
