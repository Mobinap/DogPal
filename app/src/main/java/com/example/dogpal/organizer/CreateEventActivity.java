package com.example.dogpal.organizer;


import static com.example.dogpal.CloudinaryUploader.uploadImageToCloudinary;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Grid;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.dogpal.CloudinaryUploader;

import com.example.dogpal.MapsActivity;
import com.example.dogpal.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

   private TextView createTitle, textSelectAllowedBreeds, estimationView , textViewLabelMaxParticipants, textViewLabelMaxDogs;
    private EditText editEventTitle, editEventDescription, editEventLocation, editTextDate, editTextTime, maxParticipantsField, maxDogsField;
    private Spinner categorySpinner, spinnerLimitType;
    private ImageView imgEventPhoto;
    private Uri selectedImageUri;
    private Button btnCreateEvent, btnCancelEvent;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_LOCATION = 2;
    // location
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    private CheckBox checkboxLimitParticipants, checkboxLimitBreeds;
    private LinearLayout layoutBreeds;
    private GridLayout breedGridLayout;
    private String eventId ,userId, eventName;
    private String originalImageUrl = "";

    boolean isUpdateMode;
    private ProgressBar progressBarLoading;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCancelEvent = findViewById(R.id.btnCancelEvent);
        createTitle = findViewById(R.id.createTitle);
        progressBarLoading = findViewById(R.id.progressBarLoading);

        eventId = getIntent().getStringExtra("eventId");
        isUpdateMode = eventId != null;

        // If in update mode, prefill the event details and update button text
        if (isUpdateMode) {
            // Prefill event data if eventId is not null
            fetchAndPrefillEventData(eventId);
            createTitle.setText("Update Event");
            btnCreateEvent.setText("Update Event");
            btnCancelEvent.setVisibility(View.VISIBLE); // Show cancel button if in update mode
        }
        //-------------------- Initialize views-------------------------------------------------------
        editEventTitle = findViewById(R.id.edit_EventTitle);
        editEventDescription = findViewById(R.id.edit_EventDescription);

        editEventLocation = findViewById(R.id.edit_EventLocation);
        editEventLocation.setFocusable(false);
        editEventLocation.setClickable(true); // still allow clicking to launch map

        editTextDate = findViewById(R.id.editText_date);
        editTextTime = findViewById(R.id.editText_time);
        categorySpinner = findViewById(R.id.spinner_category);
        imgEventPhoto = findViewById(R.id.imgEventPhoto);

        //event restrictions
        checkboxLimitBreeds = findViewById(R.id.checkbox_limit_breeds);
        layoutBreeds = findViewById(R.id.Layout_breeds);
        textSelectAllowedBreeds = findViewById(R.id.text_select_allowed_breeds);
        breedGridLayout = findViewById(R.id.gridLayout_breeds);
        spinnerLimitType = findViewById(R.id.spinner_limit_type);
         maxParticipantsField = findViewById(R.id.editText_maxParticipants);
         maxDogsField = findViewById(R.id.editText_maxDogs);
         estimationView = findViewById(R.id.textView_limit_estimation);
        textViewLabelMaxDogs = findViewById(R.id.textView_label_maxDogs);
         textViewLabelMaxParticipants = findViewById(R.id.textView_label_maxParticipants);

        //populate the breed restriction check box
        String[] dogBreeds = {
                "Retriever", "Shepherd", "Poodle", "Husky", "Chihuahua",
                "Shih Tzu", "Bulldog", "Pug", "Corgi", "Maltese",
                "Beagle", "Boxer", "Doberman", "Rottweiler", "Samoyed",
                "Collie", "Other"
        };

        // Dynamically create CheckBoxes and add to GridLayout
        for (String breed : dogBreeds) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(breed);
            checkBox.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            breedGridLayout.addView(checkBox);
        }


        // initilize spinner for participation limit
        ArrayAdapter<CharSequence> adapterParticipation = ArrayAdapter.createFromResource(
                this,
                R.array.participation_limit_options,
                android.R.layout.simple_spinner_item
        );
        adapterParticipation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLimitType.setAdapter(adapterParticipation);


        // Step 3: Add text change listeners
        maxParticipantsField.addTextChangedListener(new TextWatcherAdapter() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEstimationMessage(spinnerLimitType.getSelectedItem().toString());
            }
        });

        maxDogsField.addTextChangedListener(new TextWatcherAdapter() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEstimationMessage(spinnerLimitType.getSelectedItem().toString());
            }
        });

        //get user id
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        //-------------------------clicking on the event details---------------

       // event photo
        imgEventPhoto.setOnClickListener(v -> openGallery());

        // spinner event categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.event_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // event date and time
        editTextDate.setOnClickListener(v -> openDatePicker());
        editTextTime.setOnClickListener(v -> openTimePicker());

        // Open MapActivity to allow location selection
        editEventLocation.setOnClickListener(v -> {

            Intent mapIntent = new Intent(CreateEventActivity.this, MapsActivity.class);
            if (isUpdateMode) {
                mapIntent.putExtra("lat", selectedLat);
                mapIntent.putExtra("lng", selectedLng);
            }
            startActivityForResult(mapIntent, REQUEST_LOCATION);

        });
        //---------------------------click on event restrictions-----------------------

        // Set listeners to manage visibility based on checkbox state
        checkboxLimitBreeds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle visibility based on the checkbox state
            if (isChecked) {
                // Show the breed selection layout
                layoutBreeds.setVisibility(View.VISIBLE);
            } else {
                // Hide the breed selection layout
                layoutBreeds.setVisibility(View.GONE);
            }
        });

        spinnerLimitType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // No Limit
                        maxParticipantsField.setVisibility(View.GONE);
                        textViewLabelMaxParticipants.setVisibility(View.GONE);
                        maxDogsField.setVisibility(View.GONE);
                        textViewLabelMaxDogs.setVisibility(View.GONE);
                        estimationView.setVisibility(View.GONE);
                        break;
                    case 1: // Limit by Dog Owners
                        maxParticipantsField.setVisibility(View.VISIBLE);
                        textViewLabelMaxParticipants.setVisibility(View.VISIBLE);
                        maxDogsField.setVisibility(View.GONE);
                        textViewLabelMaxDogs.setVisibility(View.GONE);
                        estimationView.setVisibility(View.VISIBLE);
                        break;
                    case 2: // Limit by Dogs
                        maxParticipantsField.setVisibility(View.GONE);
                        textViewLabelMaxParticipants.setVisibility(View.GONE);
                        maxDogsField.setVisibility(View.VISIBLE);
                        textViewLabelMaxDogs.setVisibility(View.VISIBLE);
                        estimationView.setVisibility(View.VISIBLE);
                        break;
                    case 3: // Limit by Both
                        maxParticipantsField.setVisibility(View.VISIBLE);
                        textViewLabelMaxParticipants.setVisibility(View.VISIBLE);
                        maxDogsField.setVisibility(View.VISIBLE);
                        textViewLabelMaxDogs.setVisibility(View.VISIBLE);
                        estimationView.setVisibility(View.VISIBLE);
                        break;
                }
                updateEstimationMessage(spinnerLimitType.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        //---------------------------------------finish create button---------------------
        btnCreateEvent.setOnClickListener(v -> {
            btnCreateEvent.setEnabled(false); //  disable to prevent double taps

            handleCreateEvent();
        });

        btnCancelEvent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                builder.setTitle("Cancel Event");
                builder.setMessage("Are you sure you want to cancel this event?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Update the event status to "Canceled" in Firestore
                        // Redirect back to the previous activity (dashboard)
                        cancelEvent(eventId, eventName);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Dismiss the dialog if "No" is clicked
                    }
                });
                builder.show();
            }
        });


        checkPermissions();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgEventPhoto.setImageURI(selectedImageUri);
        }
        else if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0.0);
            selectedLng = data.getDoubleExtra("lng", 0.0);
            String address = data.getStringExtra("address");

            Log.d("MapsActivity", "Selected address: " + address);

            editEventLocation.setText(address);
        }
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }


    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editTextDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void openTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editTextTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void updateEstimationMessage(String limitType) {
        String maxOwnersStr = maxParticipantsField.getText().toString().trim();
        String maxDogsStr = maxDogsField.getText().toString().trim();

        String message = "";

        switch (limitType) {
            case "Limit by Dog Owners":
                if (!maxOwnersStr.isEmpty()) {
                    int maxOwners = Integer.parseInt(maxOwnersStr);
                    message = "Minimum expected dogs: " + maxOwners;
                }
                break;

            case "Limit by Dogs":
                if (!maxDogsStr.isEmpty()) {
                    int maxDogs = Integer.parseInt(maxDogsStr);
                    message = "Maximum allowed dog owners: " + maxDogs;
                }
                break;

            case "Limit by Both":
                if (!maxOwnersStr.isEmpty() && !maxDogsStr.isEmpty()) {
                    int maxOwners = Integer.parseInt(maxOwnersStr);
                    int maxDogs = Integer.parseInt(maxDogsStr);

                    if (maxDogs < maxOwners) {
                        message = "⚠️ Max dogs must be ≥ max dog owners.";
                    } else {
                        message = "Up to " + maxOwners + " owners and " + maxDogs + " dogs allowed.";
                    }
                }
                break;
        }

        if (message.isEmpty()) {
            estimationView.setVisibility(View.GONE);
        } else {
            estimationView.setText(message);
            estimationView.setVisibility(View.VISIBLE);
        }
    }

    private void handleCreateEvent() {
        progressBarLoading.setVisibility(View.VISIBLE);
        // Get data from input fields
        String eventTitle = editEventTitle.getText().toString().trim();
        String eventDescription = editEventDescription.getText().toString().trim();
        String eventLocation = editEventLocation.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String eventDate = editTextDate.getText().toString();
        String eventTime = editTextTime.getText().toString();


        // Check if any field is empty
        if (eventTitle.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() ||
                eventDate.isEmpty() || eventTime.isEmpty() || (!isUpdateMode && selectedImageUri == null) ||  // Require image in Create mode
                (isUpdateMode && selectedImageUri == null && (originalImageUrl == null || originalImageUrl.isEmpty()))) {

            Toast.makeText(CreateEventActivity.this, "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show();
            progressBarLoading.setVisibility(View.GONE); // Hide on error
            btnCreateEvent.setEnabled(true); // Unlock button on error
            return;
        }

        // Combine date and time into a single datetime and check if it's in the future
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date selectedDateTime = sdf.parse(eventDate + " " + eventTime);
            if (selectedDateTime != null && selectedDateTime.before(new Date())) {
                Toast.makeText(this, "Event date and time must be in the future.", Toast.LENGTH_SHORT).show();
                progressBarLoading.setVisibility(View.GONE); // Hide on error
                btnCreateEvent.setEnabled(true); // Unlock button on error
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date or time format.", Toast.LENGTH_SHORT).show();
            progressBarLoading.setVisibility(View.GONE); // Hide on error
            btnCreateEvent.setEnabled(true); // Unlock button on error
            return;
        }

        // Validate coordinates from map
        if (selectedLat == 0.0 && selectedLng == 0.0) {
            Toast.makeText(this, "Please pick a valid event location from the map.", Toast.LENGTH_SHORT).show();
            progressBarLoading.setVisibility(View.GONE); // Hide on error
            btnCreateEvent.setEnabled(true); // Unlock button on error
            return;
        }

        // If all checks passed, upload image and save event
        Log.d("EVENT", "All fields valid. Uploading image...");

        if (isUpdateMode){
            updateEventInFirestore(eventTitle, eventDescription, eventLocation, selectedCategory, eventDate, eventTime, null);

        } else{
            saveEventToFirestore(eventTitle, eventDescription, eventLocation, selectedCategory, eventDate, eventTime, null);

        }

    }

    private void saveEventToFirestore(String eventTitle, String eventDescription, String eventLocation,
                                      String eventCategory, String eventDate, String eventTime, @Nullable String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Collect all event details into eventData
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("organizer", userId);
        eventData.put("eventTitle", eventTitle);
        eventData.put("eventDescription", eventDescription);
        eventData.put("eventCategory", eventCategory);
        eventData.put("eventDate", eventDate);
        eventData.put("eventTime", eventTime);
        eventData.put("eventLocation", eventLocation);
        eventData.put("imageUrl", imageUrl);
        eventData.put("latitude", selectedLat);
        eventData.put("longitude", selectedLng);
        eventData.put("status", "upcoming");
        eventData.put("createdAt", FieldValue.serverTimestamp());

        // 2. Handle breed restriction
        if (checkboxLimitBreeds.isChecked()) {
            List<String> selectedBreeds = getSelectedBreeds();
            if (!selectedBreeds.isEmpty()) {
                eventData.put("breedRestrictionEnabled", true);
                eventData.put("allowedBreeds", selectedBreeds); // Save selected breeds
            } else {
                eventData.put("breedRestrictionEnabled", true);
                eventData.put("allowedBreeds", new ArrayList<>()); // Empty = error or warning later
            }
        } else {
            eventData.put("breedRestrictionEnabled", false);
            eventData.put("allowedBreeds", Collections.singletonList("All breeds allowed"));
        }

        // 3. Handle participation limit settings
        String limitType = spinnerLimitType.getSelectedItem().toString();
        eventData.put("limitType", limitType); // Save the label, e.g., "No Limit"

        if (limitType.equals("No Limit")) {
            eventData.put("maxParticipants", -1); // -1 means unlimited
            eventData.put("maxDogs", -1);
        } else if (limitType.equals("Limit by Dog Owners")) {
            String maxOwnersStr = maxParticipantsField.getText().toString().trim();
            if (maxOwnersStr.isEmpty()) {
                Toast.makeText(this, "Please enter max dog owners.", Toast.LENGTH_SHORT).show();
                progressBarLoading.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                return;
            }
            int maxOwners = Integer.parseInt(maxOwnersStr);
            eventData.put("maxParticipants", maxOwners);
            eventData.put("maxDogs", -1); // not limited
        } else if (limitType.equals("Limit by Dogs")) {
            String maxDogsStr = maxDogsField.getText().toString().trim();
            if (maxDogsStr.isEmpty()) {
                Toast.makeText(this, "Please enter max dogs.", Toast.LENGTH_SHORT).show();
                progressBarLoading.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                return;
            }
            int maxDogs = Integer.parseInt(maxDogsStr);
            eventData.put("maxParticipants", -1); // not limited
            eventData.put("maxDogs", maxDogs);
        } else if (limitType.equals("Limit by Both")) {
            String maxOwnersStr = maxParticipantsField.getText().toString().trim();
            String maxDogsStr = maxDogsField.getText().toString().trim();

            if (maxOwnersStr.isEmpty() || maxDogsStr.isEmpty()) {
                Toast.makeText(this, "Please enter both max dog owners and max dogs.", Toast.LENGTH_SHORT).show();
                progressBarLoading.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                return;
            }

            int owners = Integer.parseInt(maxOwnersStr);
            int dogs = Integer.parseInt(maxDogsStr);

            if (dogs < owners) {
                Toast.makeText(this, "Max dogs must be greater than or equal to max dog owners.", Toast.LENGTH_SHORT).show();
                progressBarLoading.setVisibility(View.GONE);
                btnCreateEvent.setEnabled(true);
                return;
            }

            eventData.put("maxParticipants", owners);
            eventData.put("maxDogs", dogs);
        }

        //save image to cloudinary
        db.collection("events").add(eventData)
                .addOnSuccessListener(documentReference -> {
                    String eventId = documentReference.getId();
                    eventData.put("eventId", eventId);
                    // Now upload the image and attach URL to this event
                    uploadImageToCloudinary(
                            CreateEventActivity.this,
                            selectedImageUri,
                            "events",
                            eventId, // Pass the newly created event ID
                            null,
                            null,
                            "imageUrl",
                            new CloudinaryUploader.ImageUploadCallback() {
                                @Override
                                public void onSuccess(String imageUrl) {
                                    Log.d("Cloudinary", "Image uploaded and saved to Firestore");
                                    addEventToUserSubfolder(userId, eventId);

                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(CreateEventActivity.this, "Image upload failed. Event saved without image.", Toast.LENGTH_SHORT).show();
                                    runOnUiThread(() -> {
                                        progressBarLoading.setVisibility(View.GONE);
                                        btnCreateEvent.setEnabled(true);
                                    });
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding event", e);
                    Toast.makeText(CreateEventActivity.this, "Error creating event.", Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE); // Hide on error
                    btnCreateEvent.setEnabled(true); // Unlock button on error
                });
    }
    private List<String> getSelectedBreeds() {
        List<String> selectedBreeds = new ArrayList<>();


        for (int i = 0; i < breedGridLayout.getChildCount(); i++) {
            View view = breedGridLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    selectedBreeds.add(checkBox.getText().toString());
                }
            }
        }
        return selectedBreeds;
    }
    private void addEventToUserSubfolder(String userId, String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("eventsCreated").document(eventId)
                .set(Collections.singletonMap("eventId", eventId))

                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEventActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    progressBarLoading.setVisibility(View.GONE);
                    btnCreateEvent.setEnabled(true); // Unlock button
                    finish(); // close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Event created but failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE); // Hide on error
                    btnCreateEvent.setEnabled(true); // Unlock button on error
                });
    }

    private void fetchAndPrefillEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        editEventTitle.setText(documentSnapshot.getString("eventTitle"));
                        eventName = documentSnapshot.getString("eventTitle");
                        editEventDescription.setText(documentSnapshot.getString("eventDescription"));
                        editEventLocation.setText(documentSnapshot.getString("eventLocation"));
                        editTextDate.setText(documentSnapshot.getString("eventDate"));
                        editTextTime.setText(documentSnapshot.getString("eventTime"));

                        // Set category spinner
                        String category = documentSnapshot.getString("eventCategory");
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
                        int position = adapter.getPosition(category);
                        categorySpinner.setSelection(position);


                        // Breed restriction
                        Boolean restrict = documentSnapshot.getBoolean("breedRestrictionEnabled");
                        if (Boolean.TRUE.equals(restrict)) {
                            checkboxLimitBreeds.setChecked(true);
                            layoutBreeds.setVisibility(View.VISIBLE);
                            List<String> allowedBreeds = (List<String>) documentSnapshot.get("allowedBreeds");

                            for (int i = 0; i < breedGridLayout.getChildCount(); i++) {
                                View view = breedGridLayout.getChildAt(i);
                                if (view instanceof CheckBox) {
                                    CheckBox checkBox = (CheckBox) view;
                                    if (allowedBreeds != null && allowedBreeds.contains(checkBox.getText().toString())) {
                                        checkBox.setChecked(true);
                                    }
                                }
                            }
                        }


                        // Participation limits
                        String limitType = documentSnapshot.getString("limitType");
                        int spinnerPos = 0; // default to "No Limit"
                        if (limitType != null) {
                            switch (limitType) {
                                case "No Limit":
                                    spinnerPos = 0;
                                    break;
                                case "Limit by Dog Owners":
                                    spinnerPos = 1;
                                    break;
                                case "Limit by Dogs":
                                    spinnerPos = 2;
                                    break;
                                case "Limit by Both":
                                    spinnerPos = 3;
                                    break;
                            }
                        }
                        spinnerLimitType.setSelection(spinnerPos);

                        // Set the fields based on what's stored
                        Long maxOwners = documentSnapshot.getLong("maxParticipants");
                        Long maxDogs = documentSnapshot.getLong("maxDogs");

                        if (maxOwners != null && maxOwners != -1) {
                            maxParticipantsField.setText(String.valueOf(maxOwners));
                            maxParticipantsField.setVisibility(View.VISIBLE);
                        } else {
                            maxParticipantsField.setVisibility(View.GONE);
                        }

                        if (maxDogs != null && maxDogs != -1) {
                            maxDogsField.setText(String.valueOf(maxDogs));
                            maxDogsField.setVisibility(View.VISIBLE);
                        } else {
                            maxDogsField.setVisibility(View.GONE);
                        }

                        if (spinnerPos == 0) {
                            estimationView.setVisibility(View.GONE);
                        } else {
                            estimationView.setVisibility(View.VISIBLE);
                        }

                        updateEstimationMessage(limitType != null ? limitType : "No Limit");


                        // Coordinates
                        selectedLat = documentSnapshot.getDouble("latitude");
                        selectedLng = documentSnapshot.getDouble("longitude");

                        // Load image using Glide/Picasso (optional)
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(imgEventPhoto); // You'll need to add Glide dependency
                            originalImageUrl = documentSnapshot.getString("imageUrl");

                        }

                    } else {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event data.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void cancelEvent(String eventId, String eventName) {
        progressBarLoading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Mark event as cancelled
        db.collection("events").document(eventId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {

                    // Step 2: Fetch all attendees
                    db.collection("events").document(eventId).collection("attendees")
                            .whereEqualTo("participationStatus", "attending") // only notify those who joined
                            .get()
                            .addOnSuccessListener(querySnapshots -> {
                                List<Task<DocumentReference>> notificationTasks = new ArrayList<>();

                                for (QueryDocumentSnapshot doc : querySnapshots) {
                                    String attendeeId = doc.getId();

                                    Map<String, Object> note = new HashMap<>();
                                    note.put("title", "Event Cancelled");
                                    note.put("isRead", false);
                                    note.put("message", "The event '" + eventName + "' has been cancelled.");
                                    note.put("timestamp", FieldValue.serverTimestamp());

                                    Task<DocumentReference> task = db.collection("users").document(attendeeId)
                                            .collection("notifications")
                                            .add(note);
                                    notificationTasks.add(task);
                                }

                                Tasks.whenAllComplete(notificationTasks)
                                        .addOnSuccessListener(tasks -> {
                                            // After sending notifications
                                            Toast.makeText(CreateEventActivity.this, "Event canceled and attendees notified!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateEventActivity.this, DashboardOrganizerActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            progressBarLoading.setVisibility(View.GONE);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(CreateEventActivity.this, "Cancelled, but failed to notify some attendees.", Toast.LENGTH_SHORT).show();
                                            progressBarLoading.setVisibility(View.GONE);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CreateEventActivity.this, "Cancelled, but failed to notify attendees.", Toast.LENGTH_SHORT).show();
                                progressBarLoading.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventActivity.this, "Error canceling event.", Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE);
                });
    }


    private void updateEventInFirestore(String eventTitle, String eventDescription, String eventLocation,
                                      String eventCategory, String eventDate, String eventTime, @Nullable String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        progressBarLoading.setVisibility(View.VISIBLE);

        // 1. Collect all event details into eventData
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("organizer", userId);
        eventData.put("eventTitle", eventTitle);
        eventData.put("eventDescription", eventDescription);
        eventData.put("eventCategory", eventCategory);
        eventData.put("eventDate", eventDate);
        eventData.put("eventTime", eventTime);
        eventData.put("eventLocation", eventLocation);
       // eventData.put("imageUrl", imageUrl);
        eventData.put("latitude", selectedLat);
        eventData.put("longitude", selectedLng);
        eventData.put("status", "upcoming");
        eventData.put("updatedAt", FieldValue.serverTimestamp()); // Firebase server timestamp


        // 2. Handle breed restriction
        if (checkboxLimitBreeds.isChecked()) {
            List<String> selectedBreeds = getSelectedBreeds();
            if (!selectedBreeds.isEmpty()) {
                eventData.put("breedRestrictionEnabled", true);
                eventData.put("allowedBreeds", selectedBreeds); // Save selected breeds
            } else {
                eventData.put("breedRestrictionEnabled", true);
                eventData.put("allowedBreeds", new ArrayList<>()); // Empty = error or warning later
            }
        } else {
            eventData.put("breedRestrictionEnabled", false);
            eventData.put("allowedBreeds", Collections.singletonList("All breeds allowed"));
        }

        // 3. Handle participation limits
        String selectedLimitType = spinnerLimitType.getSelectedItem().toString();
        eventData.put("limitType", selectedLimitType);

        switch (selectedLimitType) {
            case "No Limit":
                eventData.put("maxParticipants", -1);
                eventData.put("maxDogs", -1);
                break;
            case "Limit by Dog Owners":
                try {
                    int maxOwners = Integer.parseInt(maxParticipantsField.getText().toString());
                    eventData.put("maxParticipants", maxOwners);
                    eventData.put("maxDogs", -1);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number for dog owners limit.", Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE);
                    return;
                }
                break;
            case "Limit by Dogs":
                try {
                    int maxDogs = Integer.parseInt(maxDogsField.getText().toString());
                    eventData.put("maxParticipants", -1);
                    eventData.put("maxDogs", maxDogs);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number for dogs limit.", Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE);
                    return;
                }
                break;
            case "Limit by Both":
                try {
                    int maxOwners = Integer.parseInt(maxParticipantsField.getText().toString());
                    int maxDogs = Integer.parseInt(maxDogsField.getText().toString());
                    eventData.put("maxParticipants", maxOwners);
                    eventData.put("maxDogs", maxDogs);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers for both limits.", Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE);
                    return;
                }
                break;
        }

        // Step 4: If image is selected, upload first and then update Firestore
                    if (selectedImageUri != null) {
                    uploadImageToCloudinary(
                            CreateEventActivity.this,
                            selectedImageUri,
                            "events",
                            eventId, // Pass the newly created event ID
                            null,
                            null,
                            "imageUrl",
                            new CloudinaryUploader.ImageUploadCallback() {
                                @Override
                                public void onSuccess(String imageUrl) {
                                                eventData.put("imageUrl", imageUrl);
                                                updateFirestoreEvent(db, eventData);
                                            }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(CreateEventActivity.this, "Image upload failed. Event saved without image.", Toast.LENGTH_SHORT).show();
                                        progressBarLoading.setVisibility(View.GONE);
                                        btnCreateEvent.setEnabled(true);
                                    });
                                }
                            }); } else {
                        // No new image selected — just update event data as is
                        updateFirestoreEvent(db, eventData);
                    }
    }

    // Helper to update event in Firestore
    private void updateFirestoreEvent(FirebaseFirestore db, Map<String, Object> eventData) {
        db.collection("events").document(eventId)
                .update(eventData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEventActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating event", e);
                    Toast.makeText(CreateEventActivity.this, "Error updating event.", Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE);
                    btnCreateEvent.setEnabled(true);
                });
    }

    abstract class TextWatcherAdapter implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
    private void navigateToDashboard() {
        progressBarLoading.setVisibility(View.GONE);
        btnCreateEvent.setEnabled(true);
        Intent intent = new Intent(CreateEventActivity.this, DashboardOrganizerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
