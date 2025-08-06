package com.example.dogpal.Attendee;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.MapsActivity;
import com.example.dogpal.R;
import com.example.dogpal.adapter.AttendeeAdapter;
import com.example.dogpal.adapter.DogSelectAdapter;
import com.example.dogpal.models.Dog;
import com.example.dogpal.models.Event;
import com.example.dogpal.models.User;
import com.example.dogpal.profile.UserProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {

    private TextView eventTitle, eventCategory, eventDescription, eventDate, eventTime, eventLocation, allowedBreeds, organizer, directionsText, participantsText;
    private ImageView eventImage;
    private Button joinEventButton, btnCancelParticipation;
    private String eventId, currentUserId,  organizerId;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private Event event;
    private double lat;
    private double lng;
    private String currentUserParticipationStatus = "none";  // Default = not joined


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });


        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();

        // Initialize UI elements
        eventTitle = findViewById(R.id.eventTitle);
        eventCategory = findViewById(R.id.eventCategory);
        eventDescription = findViewById(R.id.eventDescription);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        eventLocation = findViewById(R.id.eventAddress);
        allowedBreeds = findViewById(R.id.allowedBreeds);
        eventImage = findViewById(R.id.eventImage);
        organizer = findViewById(R.id.organizerName);
        joinEventButton = findViewById(R.id.joinEventButton);
        btnCancelParticipation = findViewById(R.id.btnCancelParticipation);
        participantsText = findViewById(R.id.participantsText);
        directionsText = findViewById(R.id.directionsText);


        // Get Event ID from Intent
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            Log.e("EventDetailActivity", "Event ID is null!");
            // Handle the case when eventId is null (e.g., show an error message or return)
        }

        // Fetch Event Details from Firestore
        fetchEventDetails(eventId);
        checkIfUserHasJoinedEvent();


        SpannableString underlineParticipants = new SpannableString("Who is participating?");
        underlineParticipants.setSpan(new UnderlineSpan(), 0, underlineParticipants.length(), 0);
        participantsText.setText(underlineParticipants);

        SpannableString underlineDirections = new SpannableString("Directions");
        underlineDirections.setSpan(new UnderlineSpan(), 0, underlineDirections.length(), 0);
        directionsText.setText(underlineDirections);


        // Handle Directions Button Click
        findViewById(R.id.directionsText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMaps();

            }
        });
        organizer.setOnClickListener(v -> {
            if (organizerId != null) {
                Intent intent = new Intent(EventDetailActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", organizerId);  // Pass the organizer's ID
                intent.putExtra("viewerParticipationStatus", "organizer");  // Special flag
                startActivity(intent);
            } else {
                Toast.makeText(this, "Organizer information is not available", Toast.LENGTH_SHORT).show();
            }
        });


        participantsText.setOnClickListener(v -> fetchAndShowAttendees(eventId));

        // Handle Join Button Click
        joinEventButton.setOnClickListener(v -> {
            // First check if the current user is the organizer
            if (currentUserId.equals(organizerId)) {
                Toast.makeText(EventDetailActivity.this,
                        "You are the organizer. You cannot join your own event.", Toast.LENGTH_LONG).show();
                return;
            }
            fetchUserDogsAndShowDialog();
        });

        btnCancelParticipation.setOnClickListener(v -> cancelParticipation());

    }

    private void fetchEventDetails(String eventId) {
        // Access the "events" collection in Firestore
        db.collection("events").document(eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Map Firestore document to Event object
                                 event = document.toObject(Event.class);

                                event.setEventId(document.getId());

                                // Populate the UI with event data
                                if (event != null) {
                                     lat = event.getLatitude();
                                     lng = event.getLongitude();
                                    eventTitle.setText(event.getEventTitle());
                                    eventCategory.setText(event.getEventCategory());
                                    eventDescription.setText(event.getEventDescription());
                                    eventDate.setText( event.getEventDate());
                                    eventTime.setText( event.getEventTime());
                                    eventLocation.setText(event.getEventLocation());

                                    // Display allowed breeds if any
                                    String breeds = event.isBreedRestrictionEnabled() ?
                                            "Allowed Breeds: " + String.join(", ", event.getAllowedBreeds()) :
                                            "No breed restrictions";
                                    allowedBreeds.setText(breeds);

                                    if (event.isBreedRestrictionEnabled()) {
                                        List<String> breedsList = event.getAllowedBreeds();
                                        if (breedsList != null && !breedsList.isEmpty()) {
                                            String formatted = String.join(", ", breedsList);
                                            allowedBreeds.setText("Allowed Breeds: " + formatted);
                                        } else {
                                            allowedBreeds.setText("Allowed Breeds: (none specified)");
                                        }
                                    } else {
                                        allowedBreeds.setText("Allowed Breeds: All Breeds Allowed");
                                    }
                                   // organizer.setText(event.getOrganizer());
                                     organizerId = event.getOrganizer();
                                    fetchOrganizerName(organizerId); // Fetch name using it
                                    if (currentUserId.equals(organizerId)) {
                                        currentUserParticipationStatus = "organizer";
                                    }

                                    // Load event image (use Glide, Picasso, or Firebase Storage if the image is from there)
                                    Glide.with(EventDetailActivity.this)
                                            .load(event.getImageUrl()) // Load image URL from Firestore if present
                                            .into(eventImage);

                                }
                            } else {
                                // Handle case where event does not exist
                                Toast.makeText(EventDetailActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle Firestore read failure
                            Toast.makeText(EventDetailActivity.this, "Error fetching event details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void openGoogleMaps() {
        Intent mapIntent = new Intent(EventDetailActivity.this, MapsActivity.class);
        // Attach latitude and longitude values as extras to open Intent
        mapIntent.putExtra("lat", lat);
        mapIntent.putExtra("lng", lng);
        startActivity(mapIntent); }
    private void fetchOrganizerName(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) {
                            SpannableString content = new SpannableString(name);
                            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                            organizer.setText(content);
                        } else {
                            organizer.setText("Unknown organizer");
                        }
                    } else {
                        organizer.setText("Organizer not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailActivity", "Failed to fetch organizer", e);
                    organizer.setText("Error loading organizer");
                });
    }

    private void checkBreedRestrictions(List<Dog> selectedDogs) {
        if (event.isBreedRestrictionEnabled()) {
            List<String> allowedBreeds = event.getAllowedBreeds();
            // Check if allowedBreeds list contains the "All breeds allowed" string just in case
            if (allowedBreeds.size() == 1 && allowedBreeds.get(0).equalsIgnoreCase("All breeds allowed")) {
              //  addAttendeeToEvent(); // No breed restriction therefor add attendee
                checkParticipationLimits(selectedDogs);
                return;
            }
            // If there are breed restrictions, check the user's dog breed
            for (Dog dog : selectedDogs) {
                if (!matchesAllowedBreed(dog.getBreed(), allowedBreeds)) {
                    Toast.makeText(this,
                            "The selected dog \"" + dog.getName() + "\" does not match the breed restrictions.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // All dogs matched the allowed breeds
            checkParticipationLimits(selectedDogs);
        }else {
            // No breed restriction
            checkParticipationLimits(selectedDogs);
        }
    }
    private boolean matchesAllowedBreed(String dogBreed, List<String> allowedBreeds) {
        for (String allowed : allowedBreeds) {
            if (allowed.equalsIgnoreCase(dogBreed)) {
                return true;
            }
        }
        return false;
    }

    private void checkParticipationLimits(List<Dog> selectedDogs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference attendeesRef = db.collection("events").document(eventId).collection("attendees");

        // Only count those who are actually attending
        attendeesRef.whereEqualTo("participationStatus", "attending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int currentOwners = snapshot.size();
                    int currentDogs = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long dogCount = doc.getLong("dogCount");
                        if (dogCount != null) {
                            currentDogs += dogCount;
                        }
                    }

                    int selectedDogCount = selectedDogs.size();

                    String limitType = event.getLimitType();
                    int maxOwners = event.getMaxParticipants();
                    int maxDogs = event.getMaxDogs();

                    boolean canJoin = true;
                    String errorMessage = "";

                    switch (limitType) {
                        case "No Limit":
                            canJoin = true;
                            break;
                        case "Limit by Dog Owners":
                            if (currentOwners >= maxOwners) {
                                canJoin = false;
                                errorMessage = "The event has reached the maximum number of dog owners.";
                            }
                            break;
                        case "Limit by Dogs":
                            if ((currentDogs + selectedDogCount) > maxDogs) {
                                canJoin = false;
                                errorMessage = "The event has reached the maximum number of dogs.";
                            }
                            break;
                        case "Limit by Both":
                            if (currentOwners >= maxOwners) {
                                canJoin = false;
                                errorMessage = "The event has reached the maximum number of dog owners.";
                            } else if ((currentDogs + selectedDogCount) > maxDogs) {
                                canJoin = false;
                                errorMessage = "The event has reached the maximum number of dogs.";
                            }
                            break;
                    }

                    if (canJoin) {
                        addAttendeeToEvent(selectedDogs);
                    } else {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check event limits. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    private void addAttendeeToEvent(List<Dog> selectedDogs) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference attendeeRef = db.collection("events").document(eventId)
                            .collection("attendees").document(currentUserId);
                    // Now prepare attendee data
                    Map<String, Object> attendeeData = new HashMap<>();
                    attendeeData.put("attendeeId", currentUserId);
                    attendeeData.put("participationStatus", "attending");
                    attendeeData.put("dogIds", getDogIds(selectedDogs));
                    attendeeData.put("dogCount", selectedDogs.size());


                    // Add the user to the event's attendees list
                    attendeeRef.set(attendeeData)
                        .addOnSuccessListener(aVoid -> {
                                //  Add the event to the user's joinedEvents list
                                Map<String, Object> joinedEventData = new HashMap<>();
                                joinedEventData.put("eventId", eventId);
                                joinedEventData.put("eventTitle", eventTitle.getText().toString());
                                joinedEventData.put("participationStatus", "attending");
                                joinedEventData.put("joinedAt", Timestamp.now()); // optional

                                // Add the event to the user's joinedEvents list
                                db.collection("users").document(currentUserId)
                                        .collection("joinedEvents")
                                        .document(eventId)
                                        .set(joinedEventData)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(EventDetailActivity.this, "You have successfully joined the event!", Toast.LENGTH_LONG).show();
                                            currentUserParticipationStatus = "attending";
                                            joinEventButton.setText("You have joined this event");
                                            joinEventButton.setEnabled(false);
                                            btnCancelParticipation.setVisibility(View.VISIBLE);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(EventDetailActivity.this, "Error saving event under your profile", Toast.LENGTH_SHORT).show();
                                        });

                            })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetailActivity.this, "Error joining the event", Toast.LENGTH_SHORT).show();
                });

    }

private List<String> getDogIds(List<Dog> selectedDogs) {
    List<String> dogIds = new ArrayList<>();
    for (Dog dog : selectedDogs) {
        dogIds.add(dog.getDogId());
    }
    return dogIds;
}

    private void showAttendeesDialog(List<User> attendeeNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_attendees, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView recyclerView = dialogView.findViewById(R.id.attendeesRecyclerView);
        TextView emptyMessage = dialogView.findViewById(R.id.emptyMessage);

        if (attendeeNames == null || attendeeNames.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new AttendeeAdapter(this, attendeeNames, currentUserParticipationStatus));
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        dialogView.findViewById(R.id.closeButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.closeIcon).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void fetchAndShowAttendees(String eventId) {
        db.collection("events").document(eventId).collection("attendees")
                .whereEqualTo("participationStatus", "attending").get()
                .addOnSuccessListener(attendeesSnapshot -> {
                    List<User> attendees = new ArrayList<>();
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : attendeesSnapshot.getDocuments()) {
                        String userId = doc.getId();
                        Task<DocumentSnapshot> userTask = db.collection("users").document(userId).get();
                        userTasks.add(userTask);
                    }

                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(results -> {
                        for (Object result : results) {
                            DocumentSnapshot userDoc = (DocumentSnapshot) result;
                            String name = userDoc.getString("name");
                            String userId = userDoc.getId();
                            String phone = userDoc.getString("phone");

                            String profileImageUrl = userDoc.getString("profileImageUrl");

                            // Create a User object and add it to the list
                            if (name != null && userId != null) {
                                User user = new User(userId, name, phone, profileImageUrl);  // Adjust constructor if needed
                                attendees.add(user);
                            }
                        }
                        // Pass List<User> to the dialog
                        showAttendeesDialog(attendees);
                    });
                });
    }
    private void cancelParticipation() {
        // update the user status from the event's attendees collection
        db.collection("events").document(eventId)
                .collection("attendees")
                .document(currentUserId)
                .update("participationStatus", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    // Remove the event from the user's joinedEvents list
                    db.collection("users").document(currentUserId)
                            .collection("joinedEvents")
                            .document(eventId)
                            .update("participationStatus", "cancelled")
                            .addOnSuccessListener(unused -> {
                                // Success: Update UI and show a message
                                Toast.makeText(EventDetailActivity.this, "You have successfully cancelled participation", Toast.LENGTH_LONG).show();
                                currentUserParticipationStatus = "cancelled";
                                joinEventButton.setText("Join Event");
                                joinEventButton.setEnabled(true);
                                btnCancelParticipation.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                // Handle error when removing from joined events
                                Toast.makeText(EventDetailActivity.this, "Error removing event from your profile", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle error when removing from attendees
                    Toast.makeText(EventDetailActivity.this, "Error cancelling participation", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkIfUserHasJoinedEvent() {
        // Check if the current user has joined this event
        db.collection("events").document(eventId)
                .collection("attendees")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String participationStatus = documentSnapshot.getString("participationStatus");
                        if ("attending".equalsIgnoreCase(participationStatus)){
                            currentUserParticipationStatus = "attending";
                            // User has already joined and attending, show cancel button and disable join button
                            joinEventButton.setText("You have joined this event");
                            joinEventButton.setEnabled(false);
                            btnCancelParticipation.setVisibility(View.VISIBLE);  // Show cancel button
                        } else if ("cancelled".equalsIgnoreCase(participationStatus)){
                            currentUserParticipationStatus = "cancelled";
                            joinEventButton.setText("Join Event");
                            joinEventButton.setEnabled(true);
                            btnCancelParticipation.setVisibility(View.GONE);  // Hide cancel button
                        }else {
                            currentUserParticipationStatus = "unknown";
                            // Unknown status - default to join
                            joinEventButton.setText("Join Event");
                            joinEventButton.setEnabled(true);
                            btnCancelParticipation.setVisibility(View.GONE);
                        }
                    } else {
                        currentUserParticipationStatus = "none";
                        // User has not joined, show join button
                        joinEventButton.setText("Join Event");
                        joinEventButton.setEnabled(true);
                        btnCancelParticipation.setVisibility(View.GONE);  // Hide cancel button
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailActivity", "Error checking user participation", e);
                });
}

    private void showDogSelectionDialog(List<Dog> dogList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_dogs, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView recyclerView = dialogView.findViewById(R.id.dogsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DogSelectAdapter adapter = new DogSelectAdapter(this, dogList);
        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.closeIcon).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            List<Dog> selectedDogs = adapter.getSelectedDogs();
            if (selectedDogs.isEmpty()) {
                Toast.makeText(this, "Please select at least one dog", Toast.LENGTH_SHORT).show();
            } else {

                dialog.dismiss();
                // Proceed with selected dogs
                checkBreedRestrictions(selectedDogs);
            }
        });

        dialog.show();
    }
    private void fetchUserDogsAndShowDialog() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("dogs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Dog> dogList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Dog dog = doc.toObject(Dog.class);
                        dog.setDogId(doc.getId()); // if needed
                        dogList.add(dog);
                    }

                    if (!dogList.isEmpty()) {
                        showDogSelectionDialog(dogList); // Show dialog with dogs
                    } else {
                        Toast.makeText(this, "You don’t have any dogs registered!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch your dogs.", Toast.LENGTH_SHORT).show();
                });
    }

}

