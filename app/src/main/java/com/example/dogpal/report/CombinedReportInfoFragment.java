package com.example.dogpal.report;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

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

import com.example.dogpal.Attendee.EventDetailActivity;
import com.example.dogpal.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.example.dogpal.R;

public class CombinedReportInfoFragment extends Fragment {

    private TextView totalEventsView, passedEventsView, upcomingEventsView, cancelledEventsView,
            totalAttendeesView, totalJoinedView, averageAttendeesView, totalCancelled ,text_bar_chart_attendance, text_pie_chart_categories;
    private BarChart barChart;
    private PieChart pieChart;
    private FirebaseFirestore db;
    private String userId;
    private int totalEventsCount = 0, passed = 0, upcoming = 0, cancelled = 0, totalAttendees = 0, totalJoined = 0, totalCancelledCount=0;
    private List<Integer> attendeeCounts = new ArrayList<>();
    private Map<String, Integer> categoryCounts = new HashMap<>(){{
        put("Outdoor Adventure", 0);
        put("Training", 0);
        put("Social Events", 0);
    }};
    private Map<String, Integer> categoryAttendanceCounts = new HashMap<>() {{
        put("Outdoor Adventure", 0);
        put("Training", 0);
        put("Social Events", 0);
    }};

    private List<String> pastEventIds = new ArrayList<>();
    private List<String> pastEventTitles = new ArrayList<>();
    private List<Integer> pastEventAttendanceCounts = new ArrayList<>();
    private Map<String, int[]> dateToAttendeeCounts = new HashMap<>();
    private ProgressBar progressBarLoading;
    // Track how many events have finished loading (including attendees)
    private int eventsProcessed = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_combined_info_view, container, false);

        totalEventsView = view.findViewById(R.id.total_events);
        passedEventsView = view.findViewById(R.id.passed_events);
        upcomingEventsView = view.findViewById(R.id.upcoming_events);
        cancelledEventsView = view.findViewById(R.id.cancelled_events);
        totalJoinedView = view.findViewById(R.id.total_joined);
        totalCancelled = view.findViewById(R.id.totalCancelled);
        averageAttendeesView = view.findViewById(R.id.average_attendees);

        pieChart = view.findViewById(R.id.pie_chart_categories);
        barChart = view.findViewById(R.id.bar_chart_attendance);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        text_bar_chart_attendance = view.findViewById(R.id.text_bar_chart_attendance);
        text_pie_chart_categories = view.findViewById(R.id.text_pie_chart_categories);


        TooltipCompat.setTooltipText(totalEventsView, "Total events you created");
        TooltipCompat.setTooltipText(passedEventsView, "Events that have already happened");
        TooltipCompat.setTooltipText(upcomingEventsView, "Events scheduled for the future");
        TooltipCompat.setTooltipText(cancelledEventsView, "Events you canceled");
        TooltipCompat.setTooltipText(totalJoinedView, "Total participants who joined your events");
        TooltipCompat.setTooltipText(totalCancelled, "Total participants who canceled participation");
        TooltipCompat.setTooltipText(averageAttendeesView, "Average attendees per past event");

        TooltipCompat.setTooltipText(text_pie_chart_categories, "Breakdown of event categories you've hosted");
        TooltipCompat.setTooltipText(text_bar_chart_attendance, "Shows total attendance per category");


        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserEventIds();

        return view;
    }

    private void loadUserEventIds() {
        progressBarLoading.setVisibility(View.VISIBLE);

        db.collection("users").document(userId).collection("eventsCreated")
                .get().addOnSuccessListener(querySnapshot -> {
                    List<String> eventIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        eventIds.add(doc.getId());
                    }
                    totalEventsCount = eventIds.size();

                    if (totalEventsCount > 0) {
                        fetchEventDetails(eventIds);
                    } else {
                        // No events - update UI immediately
                        updateUI();
                        progressBarLoading.setVisibility(View.GONE);
                    }
                });
    }

    private void fetchEventDetails(List<String> eventIds) {
        Date now = new Date();  // current date/time as Date object

        for (String eventId : eventIds) {
            db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
                if (!eventDoc.exists()) {
                    checkIfAllProcessed(eventIds.size());
                    return;
                }

                String status = eventDoc.getString("status");
                Date eventDateTime = getEventDateTime(eventDoc);
                if ("cancelled".equalsIgnoreCase(status)) {
                    cancelled++;
                    // For cancelled events, do NOT fetch attendees since they didn't happen
                    checkIfAllProcessed(eventIds.size());

                } else if (eventDateTime != null) {
                    if (eventDateTime.after(now)) {
                        upcoming++;
                        // Upcoming event: do NOT fetch attendees because it hasn't happened yet
                        checkIfAllProcessed(eventIds.size());

                    } else {
                        // Past event: increment passed count AND fetch attendees for attendance stats
                        passed++;

                        String category = eventDoc.getString("eventCategory");
                        if (category != null && categoryCounts.containsKey(category)) {
                            int count = categoryCounts.get(category);
                            categoryCounts.put(category, count + 1);
                        }
                        String title = eventDoc.getString("eventTitle");
                        pastEventIds.add(eventDoc.getId());
                        pastEventTitles.add(title != null ? title : "Event " + eventDoc.getId());

                        fetchAttendees(eventDoc.getId(), eventDateTime.getTime(), eventIds.size());
                    }
                } else {
                    // If we can't parse the date, treat it as passed or handle gracefully
                    Log.e("fetchEventDetails", "Failed to parse event date/time for eventId: " + eventDoc.getId());
                    passed++;
                    fetchAttendees(eventDoc.getId(), 0, eventIds.size());


                }
            }).addOnFailureListener(e -> {
                checkIfAllProcessed(eventIds.size());
            });
        }
    }

    /**
     * Helper method to parse eventDate and eventTime fields from DocumentSnapshot into Date.
     * Format assumed: "d/M/yyyy HH:mm"
     */
    private Date getEventDateTime(DocumentSnapshot eventDoc) {
        String eventDateStr = eventDoc.getString("eventDate");  // e.g., "19/5/2025"
        String eventTimeStr = eventDoc.getString("eventTime");  // e.g., "14:30"

        if (eventDateStr == null) return null;

        try {
            String dateTimeStr = eventDateStr + " " + (eventTimeStr != null ? eventTimeStr : "00:00");
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void fetchAttendees(String eventId, long timestamp, int totalEventCount) {

        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            String category = eventDoc.getString("eventCategory");

        db.collection("events").document(eventId).collection("attendees")
                .get().addOnSuccessListener(querySnapshot -> {
                    int attending = 0; // Initialize attending count
                    int cancelled = 0;

                    for (DocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("participationStatus");
                        if ("attending".equals(status)) {
                            attending++;
                        } else if ("cancelled".equals(status)) {
                            cancelled++;
                        }}

                    totalJoined  += attending;
                    totalCancelledCount += cancelled;

                    // Add to category attendance
                    if (category != null && categoryAttendanceCounts.containsKey(category)) {
                        int current = categoryAttendanceCounts.get(category);
                        categoryAttendanceCounts.put(category, current + attending);
                    }

                    checkIfAllProcessed(totalEventCount);
                }).addOnFailureListener(e -> {
                    checkIfAllProcessed(totalEventCount);
                });
        });
    }
    private void checkIfAllProcessed(int totalEventCount) {
        eventsProcessed++;
        if (eventsProcessed >= totalEventCount) {
            // All events have been processed (including attendees)
            updateUI();
            progressBarLoading.setVisibility(View.GONE);
        }
    }
    private void updateUI() {
        totalEventsView.setText("Total Events Created: " + totalEventsCount);
        passedEventsView.setText("Passed: " + passed);
        upcomingEventsView.setText("Upcoming: " + upcoming);
        cancelledEventsView.setText("Cancelled: " + cancelled);
        totalJoinedView.setText(String.valueOf(totalJoined));
        totalCancelled.setText(String.valueOf(totalCancelledCount));

        // Calculate average attendees only on passed events, avoid div by zero
        int avg = passed > 0 ? totalJoined / passed : 0;
        averageAttendeesView.setText("Average Attendee: " + avg);

        setupCategoryPieChart();
        setupAttendancePerCategoryChart();


    }

    private void setupCategoryPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        for (String category : Arrays.asList("Outdoor Adventure", "Training", "Social Events")) {
            int count = categoryCounts.getOrDefault(category, 0);
            if (count > 0) {
                entries.add(new PieEntry(count, category));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No category data to display.");
            return;
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#5A6ACF"),
                Color.parseColor("#9590F2"),
                Color.parseColor("#C5C9E0")
        );
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);


        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Categories");
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getLegend().setTextSize(12f);
        pieChart.animateY(1000);
        pieChart.invalidate();

    }
    private void setupAttendancePerCategoryChart() {
        BarChart eventChart = barChart;

        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = Arrays.asList("Outdoor Adventure", "Training", "Social Events");

        for (int i = 0; i < categories.size(); i++) {
            int count = categoryAttendanceCounts.getOrDefault(categories.get(i), 0);
            entries.add(new BarEntry(i, count));
        }

        if (entries.isEmpty()) {
            eventChart.clear();
            eventChart.setNoDataText("No attendance data available by category");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Attendees per Category");
        dataSet.setColors(
                Color.parseColor("#5A6ACF"),
                Color.parseColor("#9590F2"),
                Color.parseColor("#C5C9E0")
        );
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        eventChart.setData(data);
        eventChart.setFitBars(true);
        eventChart.getAxisLeft().setAxisMinimum(0f);
        eventChart.getAxisRight().setEnabled(false);
        eventChart.getDescription().setEnabled(false);

        XAxis xAxis = eventChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-15);
        xAxis.setLabelCount(entries.size(), true);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(entries.size() - 0.5f);

        eventChart.getLegend().setTextSize(12f);
        eventChart.setExtraBottomOffset(10f);
        eventChart.setScaleEnabled(false);
        eventChart.setPinchZoom(false);
        eventChart.animateY(1000);
        eventChart.invalidate();
    }

    private void setBoldLabel(TextView textView, String label, String value) {
        SpannableString spannable = new SpannableString(label + value);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

}
