package com.example.dogpal.report;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;



public class SingleEventInfoFragment extends Fragment {
    private TextView eventTitle, eventDate, eventTime, eventDateCreation, eventLocation, report_info_chart,
            totalAttendees, cancelledCount;
    private ProgressBar progressBarLoading;
    private BarChart attendanceBarChart;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_event_info_view, container, false);

        // Initialize views
        eventTitle = view.findViewById(R.id.eventTitle);
        eventDate = view.findViewById(R.id.eventDate);
        eventTime = view.findViewById(R.id.eventTime);
        eventDateCreation = view.findViewById(R.id.eventDateCreation);
        eventLocation = view.findViewById(R.id.eventLocation);
        totalAttendees = view.findViewById(R.id.totalAttendees);
        cancelledCount = view.findViewById(R.id.cancelledCount);
        attendanceBarChart = view.findViewById(R.id.attendanceBarChart);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        report_info_chart = view.findViewById(R.id.report_info_chart);

        //hover over for explanation
        TooltipCompat.setTooltipText(eventTitle, "Title of the event");
        TooltipCompat.setTooltipText(eventDate, "Date of the event");
        TooltipCompat.setTooltipText(eventTime, "Time of the event");
        TooltipCompat.setTooltipText(eventDateCreation, "Creation date of the event");
        TooltipCompat.setTooltipText(eventLocation, "Location of the event");
        TooltipCompat.setTooltipText(totalAttendees, "Number of attendees over maximum allowed participants");
        TooltipCompat.setTooltipText(cancelledCount, "Number of attendees who cancelled their participation");
        TooltipCompat.setTooltipText(report_info_chart, "See how many joined, attended, or canceled participation to event");


        // Get event ID
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            String eventId = args.getString("eventId");
            loadEventInfo(eventId);
        }

        return view;
    }

    private void loadEventInfo(String eventId) {
        progressBarLoading.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("eventTitle");
                        String date = documentSnapshot.getString("eventDate");
                        String time = documentSnapshot.getString("eventTime");
                        Timestamp createdAtTimestamp = documentSnapshot.getTimestamp("createdAt");
                        String createdAt = createdAtTimestamp != null
                                ? new SimpleDateFormat("d-M-yyyy HH:mm", Locale.getDefault()).format(createdAtTimestamp.toDate())
                                : "N/A";

                        String location = documentSnapshot.getString("eventLocation");
                        Long maxParticipants = documentSnapshot.getLong("maxParticipants");

                        // Now fetch attendees subcollection
                        db.collection("events").document(eventId).collection("attendees").get()
                                .addOnSuccessListener(querySnapshot -> {
                                    int total = querySnapshot.size();// Total number of attendee documents
                                    int attending = 0; // Count of attendees with status 'attending'
                                    int cancelled = 0; // Count of attendees with status 'cancelled'

                                    for (DocumentSnapshot doc : querySnapshot) {
                                        String status = doc.getString("participationStatus");
                                        if ("attending".equalsIgnoreCase(status)) {
                                            attending++; // increment count only for those attendees who are attending

                                        } else if ("cancelled".equalsIgnoreCase(status)) {
                                            cancelled++; // Increment cancelled count
                                        }
                                    }

                                    // Set UI
                                    eventTitle.setText(title != null ? title : "N/A");
                                    eventDate.setText(date != null ? date : "N/A");
                                    eventTime.setText(time != null ? time : "N/A");
                                    eventLocation.setText(location != null ? location : "N/A");
                                    eventDateCreation.setText(createdAt != null ? createdAt : "N/A");


                                    // Show attended/max format directly
                                    if (maxParticipants != null && maxParticipants > 0) {
                                        totalAttendees.setText(attending + " / " + maxParticipants.intValue());
                                    } else {
                                        totalAttendees.setText(String.valueOf(attending));
                                    }

                                    // Show cancelled count
                                    cancelledCount.setText(String.valueOf(cancelled));


                                    setupAttendanceBarChart(total, attending, cancelled);

                                    progressBarLoading.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle error loading attendees
                                    progressBarLoading.setVisibility(View.GONE);
                                });
                    } else {
                        progressBarLoading.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error loading event
                    progressBarLoading.setVisibility(View.GONE);
                });
    }


    private void setupAttendanceBarChart(int totalJoined, int attending, int cancelled) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, totalJoined));   // Total Joined
        entries.add(new BarEntry(1, attending));     // Attending
        entries.add(new BarEntry(2, cancelled));     // Cancelled

        BarDataSet dataSet = new BarDataSet(entries, "Event Participation");
        dataSet.setColors(
                Color.parseColor("#5A6ACF"),
                Color.parseColor("#9590F2"),
                Color.parseColor("#C5C9E0"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        attendanceBarChart.setData(barData);

        XAxis xAxis = attendanceBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Joined", "Attending", "Cancelled"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);



        attendanceBarChart.getAxisRight().setEnabled(false);
        attendanceBarChart.getDescription().setEnabled(false);
        attendanceBarChart.animateY(1000);
        attendanceBarChart.invalidate();
    }

    private void setBoldLabel(TextView textView, String label, String value) {
        SpannableString spannable = new SpannableString(label + value);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }
}