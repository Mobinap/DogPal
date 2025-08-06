package com.example.dogpal.Attendee;

import static android.text.format.DateUtils.isToday;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.BaseActivity;
import com.example.dogpal.R;
import com.example.dogpal.adapter.EventAdapter;
import com.example.dogpal.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends BaseActivity {

    private RecyclerView recyclerView;
    ImageView backButton;
    private boolean isInSearchMode = false;
    private EditText searchBar;
    private TextView noResultsText, searchTitleCategory, searchTitleUpcoming;
    private FrameLayout socialBtn, outdoorBtn, trainingBtn;
    private EventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private String eventDateTimeString;
    private SimpleDateFormat dateFormat ;
    private String selectedCategory;
    Button timeFilterBtn, breedFilterBtn, clearFilterBtn;
    String selectedTime = null;
    String selectedBreed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_search_event);
        setupLayoutWithNav(R.layout.activity_search_event);
        //bottomNavigationView.setSelectedItemId(R.id.nav_search);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            if (isInSearchMode) {
                // Reset to show all events
                selectedCategory = null; // Reset category
                searchTitleCategory.setText("Category");
                searchTitleUpcoming.setVisibility(View.VISIBLE);
                fetchEvents(); // Show all events again

                // Set the flag back to false
                isInSearchMode = false;
            } else {
                // Go to home page
                onBackPressed();  // or navigate to home screen
            }
        });

        timeFilterBtn = findViewById(R.id.timeFilterButton);
        breedFilterBtn = findViewById(R.id.breedFilterButton);
        clearFilterBtn = findViewById(R.id.clearFilterButton);

        timeFilterBtn.setOnClickListener(v -> showPopupMenu(v, R.menu.menu_time_filter, "time"));
        breedFilterBtn.setOnClickListener(v -> showPopupMenu(v, R.menu.menu_breed_filter, "breed"));
        clearFilterBtn.setOnClickListener(v -> {
            selectedTime = null;
            selectedBreed = null;
            timeFilterBtn.setText("Time ▼");
            breedFilterBtn.setText("Restrictions ▼");
            fetchEvents(); // refresh all events
        });
        noResultsText = findViewById(R.id.noResultsText);

        recyclerView = findViewById(R.id.recyclerViewUpcomingEvents);
        searchBar = findViewById(R.id.editTextSearch);
        socialBtn = findViewById(R.id.socialEventBtn);
        outdoorBtn = findViewById(R.id.outdoorAdventureBtn);
        trainingBtn = findViewById(R.id.trainingBtn);
        searchTitleUpcoming = findViewById(R.id.searchTitleUpcoming);
        searchTitleCategory = findViewById(R.id.searchTitleCategory);

        selectedCategory = getIntent().getStringExtra("category");
        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            searchTitleCategory.setText("Category: " + selectedCategory);
            searchTitleUpcoming.setVisibility(View.GONE); // Hide "Upcoming Events"
        }


        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new EventAdapter(this, eventList, currentUserId, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        fetchEvents();

        // Search filtering
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        socialBtn.setOnClickListener(v -> {
            selectedCategory = "Social Events";
            searchTitleCategory.setText("Category: " + selectedCategory);
            searchTitleCategory.setVisibility(View.VISIBLE);
            searchTitleUpcoming.setVisibility(View.GONE);
            fetchEvents(); // Reload with new category
        });


        outdoorBtn.setOnClickListener(v -> {
            selectedCategory = "Outdoor Adventure";
            searchTitleCategory.setText("Category: " + selectedCategory);
            searchTitleCategory.setVisibility(View.VISIBLE);
            searchTitleUpcoming.setVisibility(View.GONE);
            fetchEvents(); // Reload with new category
        });

        trainingBtn.setOnClickListener(v -> {
            selectedCategory = "Training";
            searchTitleCategory.setText("Category: " + selectedCategory);
            searchTitleCategory.setVisibility(View.VISIBLE);
            searchTitleUpcoming.setVisibility(View.GONE);
            fetchEvents(); // Reload with new category
        });
    }

    private void fetchEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        event.setEventId(doc.getId());

                        // Category filter
                        boolean matchesCategory = selectedCategory == null || selectedCategory.isEmpty()
                                || selectedCategory.equalsIgnoreCase(event.getEventCategory());


                        // Convert eventDate and eventTime into LocalDateTime
                        String dateStr = event.getEventDate(); // e.g., "2025-05-20"
                        String timeStr = event.getEventTime(); // e.g., "14:00"
                        Date eventDateTime = parseEventDateTime(dateStr, timeStr);


                        // Time filter
                        boolean matchesTime = true;
                        if (selectedTime != null) {
                            if (selectedTime.equals("today")) {
                                matchesTime = isToday(eventDateTime);
                            } else if (selectedTime.equals("this_week")) {
                                matchesTime = isThisWeek(eventDateTime); }}

                        // Breed restriction filter
                        boolean matchesBreed = true;
                        if (selectedBreed != null) {
                            if (selectedBreed.equals("with")) {
                                matchesBreed = event.isBreedRestrictionEnabled();
                            } else if (selectedBreed.equals("without")) {
                                matchesBreed = !event.isBreedRestrictionEnabled();}}

                        // Final filter check
                        if (matchesCategory && matchesTime && matchesBreed
                                && isUpcoming(event)
                                && !"cancelled".equalsIgnoreCase(event.getStatus())) {
                            eventList.add(event);
                        }

                    }
                    filterBySearch(searchBar.getText().toString()); // Show filtered or all
                })
          .addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isUpcoming(Event event) {
        try {

            eventDateTimeString = event.getEventDate() + " " + event.getEventTime();
            dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            Date eventDate = dateFormat.parse(eventDateTimeString);

            return eventDate != null && eventDate.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private void filterBySearch(String query) {
        filteredEvents.clear();
        if (query.isEmpty()) {
            filteredEvents.addAll(eventList);
        } else {
            for (Event event : eventList) {
                if (event.getEventTitle().toLowerCase().contains(query.toLowerCase()) ||
                        event.getEventDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
        // Show or hide "No Results" message
        if (filteredEvents.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            noResultsText.setVisibility(View.GONE);
        }
    }

    private void showPopupMenu(View anchor, int menuRes, String type) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (type.equals("time")) {
                switch (item.getItemId()) {
                    case R.id.time_today:
                        selectedTime = "today";
                        timeFilterBtn.setText("Today ▼");
                        break;
                    case R.id.time_this_week:
                        selectedTime = "this_week";
                        timeFilterBtn.setText("This Week ▼");
                        break;
                }
            } else if (type.equals("breed")) {
                switch (item.getItemId()) {
                    case R.id.breed_with:
                        selectedBreed = "with";
                        breedFilterBtn.setText("With Restriction ▼");
                        break;
                    case R.id.breed_without:
                        selectedBreed = "without";
                        breedFilterBtn.setText("No Restriction ▼");
                        break;
                }
            }
            fetchEvents(); // refresh with filters
            return true;
        });

        popup.show();
    }
    private Date parseEventDateTime(String dateStr, String timeStr) {
        try {
            String fullDateTime = dateStr + " " + timeStr; // e.g., "2025-05-20 14:00"
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(fullDateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean isToday(Date eventDateTime) {
        if (eventDateTime == null) return false;
        // Create a Calendar instance for the event date
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(eventDateTime);
        // Create a Calendar instance for the current date
        Calendar today = Calendar.getInstance();
        // Compare the year and day of year to determine if it's today
        return eventCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                eventCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isThisWeek(Date eventDateTime) {
        if (eventDateTime == null) return false;

        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(eventDateTime);

        Calendar now = Calendar.getInstance();
        int currentWeek = now.get(Calendar.WEEK_OF_YEAR);
        int currentYear = now.get(Calendar.YEAR);

        int eventWeek = eventCal.get(Calendar.WEEK_OF_YEAR);
        int eventYear = eventCal.get(Calendar.YEAR);

        return eventYear == currentYear && eventWeek == currentWeek;
    }


}
