package com.example.dogpal.report;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

import com.example.dogpal.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleEventDemographicsFragment extends Fragment {

    private TextView textBreedRestriction, textTotalDogs, textMaleRatio, textFemaleRatio, text_ageBarChart, text_breedPieChart;
    private BarChart ageBarChart;
    private PieChart breedPieChart;
    private ProgressBar progressBarLoading;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_event_demographics_view, container, false);

        // Initialize views
        textBreedRestriction = view.findViewById(R.id.textBreedRestriction);
        textTotalDogs = view.findViewById(R.id.textTotalDogs);
        textFemaleRatio = view.findViewById(R.id.textFemaleRatio);
        textMaleRatio = view.findViewById(R.id.textMaleRatio);
        breedPieChart = view.findViewById(R.id.breedPieChart);
        ageBarChart = view.findViewById(R.id.ageBarChart);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        text_ageBarChart = view.findViewById(R.id.text_ageBarChart);
        text_breedPieChart = view.findViewById(R.id.text_breedPieChart);


        //hover over for explanation
        TooltipCompat.setTooltipText(textBreedRestriction, "Shows which dog breeds were allowed for this event.");
        TooltipCompat.setTooltipText(textTotalDogs, "Total number of dogs attending.");
        TooltipCompat.setTooltipText(textMaleRatio, "Percentage of male dogs attending.");
        TooltipCompat.setTooltipText(textFemaleRatio, "Percentage of female dogs attending.");
        TooltipCompat.setTooltipText(text_breedPieChart, "Pie chart showing distribution of dog breeds that joined this event.");
        TooltipCompat.setTooltipText(text_ageBarChart, "Bar chart showing number of attended dogs by age group: puppy, adult, and senior.");


        // Get eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            if (eventId != null) {
                loadDogDemographics(eventId);
            }
        }

        return view;
    }

    private void loadDogDemographics(String eventId) {
        progressBarLoading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get().addOnSuccessListener(eventSnap -> {
            if (eventSnap.exists()) {
                boolean breedRestricted = eventSnap.getBoolean("breedRestrictionEnabled") != null && eventSnap.getBoolean("breedRestrictionEnabled");
                List<String> allowedBreeds = (List<String>) eventSnap.get("allowedBreeds");

                String restrictionText = breedRestricted
                        ? (allowedBreeds != null ? allowedBreeds.toString() : "None")
                        : "All Breeds Allowed";
                textBreedRestriction.setText(restrictionText);


                db.collection("events").document(eventId).collection("attendees")
                        .whereEqualTo("participationStatus", "attending")
                        .get().addOnSuccessListener(attendeeSnap -> {

                            int totalDogs = 0;
                            Map<String, String> attendeeUserMap = new HashMap<>();

                            for (DocumentSnapshot doc : attendeeSnap.getDocuments()) {
                                int dogCount = doc.getLong("dogCount") != null ? doc.getLong("dogCount").intValue() : 0;
                                totalDogs += dogCount;
                                attendeeUserMap.put(doc.getId(), doc.getId()); // attendeeId == userId
                            }
                            Long maxDogsLimit = eventSnap.getLong("maxDogs");
                            String totalDisplay = (maxDogsLimit != null && maxDogsLimit > 0)
                                    ? totalDogs + "/" + maxDogsLimit
                                    : String.valueOf(totalDogs);

                            textTotalDogs.setText(totalDisplay);


                            Map<String, Integer> genderCount = new HashMap<>();
                            Map<String, Integer> breedCount = new HashMap<>();
                            Map<String, Integer> ageGroupCount = new HashMap<>();
                            genderCount.put("male", 0);
                            genderCount.put("female", 0);
                            ageGroupCount.put("puppy", 0);
                            ageGroupCount.put("adult", 0);
                            ageGroupCount.put("senior", 0);


                            List<Task<QuerySnapshot>> tasks = new ArrayList<>();

                            for (String userId : attendeeUserMap.values()) {
                                Task<QuerySnapshot> task = db.collection("users").document(userId).collection("dogs").get();

                                task.addOnSuccessListener(dogSnap -> {
                                    for (DocumentSnapshot dogDoc : dogSnap) {
                                        String gender = dogDoc.getString("gender");
                                        String breed = dogDoc.getString("breed");
                                        Long age = dogDoc.getLong("age");

                                        if ("male".equalsIgnoreCase(gender)) {
                                            genderCount.put("male", genderCount.get("male") + 1);
                                        } else if ("female".equalsIgnoreCase(gender)) {
                                            genderCount.put("female", genderCount.get("female") + 1);
                                        }

                                        if (breed != null) {
                                            breedCount.put(breed, breedCount.getOrDefault(breed, 0) + 1);
                                        }

                                        if (age != null) {
                                            if (age <= 2) {
                                                ageGroupCount.put("puppy", ageGroupCount.get("puppy") + 1);
                                            } else if (age <= 7) {
                                                ageGroupCount.put("adult", ageGroupCount.get("adult") + 1);
                                            } else {
                                                ageGroupCount.put("senior", ageGroupCount.get("senior") + 1);
                                            }
                                        }
                                    }
                                });

                                tasks.add(task); // Now it's a valid Task<QuerySnapshot>
                            }


                            Tasks.whenAllComplete(tasks).addOnSuccessListener(v -> {
                                int totalGender = genderCount.get("male") + genderCount.get("female");
                                int malePercent = totalGender > 0 ? (genderCount.get("male") * 100 / totalGender) : 0;
                                int femalePercent = 100 - malePercent;
                                textMaleRatio.setText(malePercent + "%");
                                textFemaleRatio.setText(femalePercent + "%");

                                setupBreedChart(breedCount);
                                setupAgeGroupChart(ageGroupCount);
                                progressBarLoading.setVisibility(View.GONE);
                            });

                        });
            }
        });
    }

    private void setupBreedChart(Map<String, Integer> breedCount) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : breedCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
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
        breedPieChart.setData(data);
        breedPieChart.getDescription().setEnabled(false);
        breedPieChart.setEntryLabelColor(Color.BLACK);
        breedPieChart.setCenterText("Breed Distribution");
        breedPieChart.setCenterTextSize(16f);
        breedPieChart.animateY(1000);
        breedPieChart.invalidate();  // refresh
    }

    private void setupAgeGroupChart(Map<String, Integer> ageGroupCount) {
        List<BarEntry> entries = new ArrayList<>();
        // Order: puppy (0), adult (1), senior (2)
        entries.add(new BarEntry(0, ageGroupCount.getOrDefault("puppy", 0)));
        entries.add(new BarEntry(1, ageGroupCount.getOrDefault("adult", 0)));
        entries.add(new BarEntry(2, ageGroupCount.getOrDefault("senior", 0)));

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
        ageBarChart.setFitBars(true);
        ageBarChart.getDescription().setEnabled(false);
        ageBarChart.getAxisRight().setEnabled(false);

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
        ageBarChart.invalidate(); // refresh
    }


    private void setBoldLabel(TextView textView, String label, String value) {
        SpannableString spannable = new SpannableString(label + value);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }
}

