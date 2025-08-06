package com.example.dogpal.report;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

import com.example.dogpal.R;
import com.example.dogpal.models.Event;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

// Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

// Java utility
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// MPAndroidChart
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

public class CombinedReportDemographicsFragment extends Fragment {

    private TextView textTotalDogs, textAvgDogsPerEvent, textMostCommonBreed, textMaleRatio, textFemaleRatio, text_breedPieChart, text_ageBarChart;
    private ProgressBar progressBar;
    private PieChart breedPieChart;
    private BarChart ageBarChart;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private int totalDogs = 0;
    private int totalEvents = 0;
    private Map<String, Integer> breedCountMap = new HashMap<>();
    private Map<String, Integer> ageGroups = new HashMap<>();
    private int maleCount = 0, femaleCount = 0;
    private static final String TAG = "CombinedDemoFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.report_combined_demographics_view, container, false);

        textTotalDogs = view.findViewById(R.id.textTotalDogs);
        textAvgDogsPerEvent = view.findViewById(R.id.textAvgDogsPerEvent);
        textMostCommonBreed = view.findViewById(R.id.textMostCommonBreed);
        textFemaleRatio = view.findViewById(R.id.textFemaleRatio);
        textMaleRatio = view.findViewById(R.id.textMaleRatio);
        progressBar = view.findViewById(R.id.progressBarLoading);
        breedPieChart = view.findViewById(R.id.breedPieChart);
        ageBarChart = view.findViewById(R.id.ageBarChart);

        text_breedPieChart = view.findViewById(R.id.text_breedPieChart);
        text_ageBarChart = view.findViewById(R.id.text_ageBarChart);


        TooltipCompat.setTooltipText(textTotalDogs, "Total number of attended dogs across all past events");
        TooltipCompat.setTooltipText(textAvgDogsPerEvent, "Average number of dogs per event");
        TooltipCompat.setTooltipText(textMostCommonBreed, "Most frequently appearing dog breed");
        TooltipCompat.setTooltipText(textFemaleRatio, "Percentage of female dogs");
        TooltipCompat.setTooltipText(textMaleRatio, "Percentage of male dogs");

        TooltipCompat.setTooltipText(text_breedPieChart, "Distribution of dog breeds across past events");
        TooltipCompat.setTooltipText(text_ageBarChart, "Dog age group distribution: puppy, adult, senior");


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreateView: Fragment created");

        loadDogDemographics();

        return view;
    }
    private void loadDogDemographics() {
        progressBar.setVisibility(View.VISIBLE);

        String userId = auth.getCurrentUser().getUid();
        ageGroups.put("Puppy", 0);
        ageGroups.put("Adult", 0);
        ageGroups.put("Senior", 0);

        db.collection("users").document(userId).collection("eventsCreated").get()
                .addOnSuccessListener(createdEventSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (DocumentSnapshot doc : createdEventSnapshots) {
                        eventIds.add(doc.getId());
                    }

                    if (eventIds.isEmpty()) {
                        updateUI();
                        return;
                    }

                    db.collection("events")
                            .whereIn(FieldPath.documentId(), eventIds)
                            .get()
                            .addOnSuccessListener(eventsSnapshot -> {
                                List<DocumentSnapshot> pastEvents = new ArrayList<>();
                                for (DocumentSnapshot eventDoc : eventsSnapshot) {
                                    String status = eventDoc.getString("status");
                                    String date = eventDoc.getString("eventDate");
                                    String time = eventDoc.getString("eventTime");

                                    if (!"cancelled".equalsIgnoreCase(status) && isEventInPast(date, time)) {
                                        pastEvents.add(eventDoc);
                                    }
                                }

                                totalEvents = pastEvents.size();
                                if (totalEvents == 0) {
                                    updateUI();
                                    return;
                                }

                                List<Task<Void>> eventTasks = new ArrayList<>();

                                for (DocumentSnapshot eventDoc : pastEvents) {
                                    String eventId = eventDoc.getId();

                                    Task<Void> task = db.collection("events").document(eventId)
                                            .collection("attendees")
                                            .whereEqualTo("participationStatus", "attending")
                                            .get()
                                            .continueWithTask(attendeesTask -> {
                                                List<Task<DocumentSnapshot>> dogFetchTasks = new ArrayList<>();

                                                for (DocumentSnapshot attendeeDoc : attendeesTask.getResult()) {
                                                    String attendeeId = attendeeDoc.getId();
                                                    List<String> dogIDs = (List<String>) attendeeDoc.get("dogIds");
                                                    Long dogCount = attendeeDoc.getLong("dogCount");
                                                    totalDogs += dogCount != null ? dogCount.intValue() : 0;

                                                    if (dogIDs != null) {
                                                        for (String dogId : dogIDs) {
                                                            Task<DocumentSnapshot> dogTask = db.collection("users")
                                                                    .document(attendeeId)
                                                                    .collection("dogs")
                                                                    .document(dogId)
                                                                    .get()
                                                                    .addOnSuccessListener(dogDoc -> {
                                                                        if (dogDoc.exists()) {
                                                                            String breed = dogDoc.getString("breed");
                                                                            String gender = dogDoc.getString("gender");
                                                                            Long age = dogDoc.getLong("age");

                                                                            if (breed != null) {
                                                                                breedCountMap.put(breed, breedCountMap.getOrDefault(breed, 0) + 1);
                                                                            }

                                                                            if ("male".equalsIgnoreCase(gender)) maleCount++;
                                                                            else if ("female".equalsIgnoreCase(gender)) femaleCount++;

                                                                            if (age != null) {
                                                                                if (age <= 2) ageGroups.put("Puppy", ageGroups.get("Puppy") + 1);
                                                                                else if (age <= 7) ageGroups.put("Adult", ageGroups.get("Adult") + 1);
                                                                                else ageGroups.put("Senior", ageGroups.get("Senior") + 1);
                                                                            }
                                                                        }
                                                                    });

                                                            dogFetchTasks.add(dogTask);
                                                        }
                                                    }
                                                }

                                                return Tasks.whenAll(dogFetchTasks);
                                            });

                                    eventTasks.add(task);
                                }

                                Tasks.whenAllComplete(eventTasks)
                                        .addOnSuccessListener(allTasks -> {
                                            Log.d(TAG, "All event tasks completed");
                                            updateUI();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to process event tasks: ", e);
                                            updateUI();
                                        });

                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to fetch events: ", e);
                                updateUI();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch eventsCreated: ", e);
                    updateUI();
                });
    }

    private boolean isEventInPast(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            Date eventDate = sdf.parse(dateStr + " " + timeStr);
            return eventDate != null && eventDate.before(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateUI() {
        Log.d(TAG, "updateUI: totalDogs = " + totalDogs + ", totalEvents = " + totalEvents);
        Log.d(TAG, "Breed count map: " + breedCountMap);
        Log.d(TAG, "Age groups: " + ageGroups);
        Log.d(TAG, "Gender counts - Male: " + maleCount + ", Female: " + femaleCount);

        progressBar.setVisibility(View.GONE);

        textTotalDogs.setText(String.valueOf(totalDogs));

        int avgDogs = totalEvents > 0 ? totalDogs / totalEvents : 0;
        setBoldLabel(textAvgDogsPerEvent, "Average Dogs per Event: ", String.valueOf(avgDogs));

        String mostCommonBreed = "-";
        if (!breedCountMap.isEmpty()) {
            mostCommonBreed = Collections.max(breedCountMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        setBoldLabel(textMostCommonBreed, "Most Common Breed: ", mostCommonBreed);

        int totalGender = maleCount + femaleCount;
        int malePercent = totalGender > 0 ? (maleCount * 100 / totalGender) : 0;
        int femalePercent = 100 - malePercent;
        textMaleRatio.setText(malePercent + "%");
        textFemaleRatio.setText(femalePercent + "%");

        setBreedPieChart();
        setAgeBarChart();
    }


    private void setBreedPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : breedCountMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        if (entries.isEmpty()) {
            breedPieChart.clear();
            breedPieChart.setNoDataText("No Breed data available");
            return;
        }
        PieDataSet dataSet = new PieDataSet(entries, "Breeds");
        dataSet.setColors(
                Color.parseColor("#5A6ACF"),
                Color.parseColor("#9590F2"),
                Color.parseColor("#C5C9E0")
        );
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);


        PieData data = new PieData(dataSet);
        breedPieChart.getDescription().setEnabled(false);
        breedPieChart.setData(data);
        breedPieChart.setEntryLabelColor(Color.BLACK);
        breedPieChart.animateY(1000);
        breedPieChart.invalidate();
    }

    private void setAgeBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, ageGroups.get("Puppy")));
        entries.add(new BarEntry(1, ageGroups.get("Adult")));
        entries.add(new BarEntry(2, ageGroups.get("Senior")));

        BarDataSet dataSet = new BarDataSet(entries, "Age Groups");
        dataSet.setColors(
                Color.parseColor("#5A6ACF"),
                Color.parseColor("#9590F2"),
                Color.parseColor("#C5C9E0")
        );
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        ageBarChart.setData(data);

        // Set labels for X axis
        XAxis xAxis = ageBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                switch ((int) value) {
                    case 0: return "Puppy";
                    case 1: return "Adult";
                    case 2: return "Senior";
                    default: return "";
                }
            }
        });

        ageBarChart.animateY(1000);
        ageBarChart.getDescription().setEnabled(false);
        ageBarChart.invalidate();
    }


    private void setBoldLabel(TextView tv, String label, String value) {
        String text = label + value;
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(spannable);
    }

}

