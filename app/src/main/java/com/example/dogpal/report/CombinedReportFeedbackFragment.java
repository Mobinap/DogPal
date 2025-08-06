package com.example.dogpal.report;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


// Firebase Firestore
import com.bumptech.glide.Glide;
import com.example.dogpal.Attendee.EventDetailActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.dogpal.R;

// MPAndroidChart for BarChart
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

// Utility classes
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class CombinedReportFeedbackFragment extends Fragment {

    private TextView globalAverageRatingText;
    private TextView count5Stars, count4Stars, count3Stars, count2Stars, count1Star;
    private ProgressBar progressBar5Stars, progressBar4Stars, progressBar3Stars, progressBar2Stars, progressBar1Star, progressBarLoading;
    private ImageView eventImage1, eventImage2, eventImage3;
    private TextView eventTitle1, eventTitle2, eventTitle3;
    private TextView eventRating1, eventRating2, eventRating3;
    private ImageView worstEventImage;
    private TextView worstEventTitle, worstEventCategory, worstEventRating;
    private LinearLayout worstRatedEventCard;

    private BarChart ratingsBarChart;

    private FirebaseFirestore db;

    private Date eventDate = null;

    private final DecimalFormat df = new DecimalFormat("#.#");

    private float globalRatingSum = 0f;
    private int globalRatingCount = 0;

    // Top 3 best-rated event variables
    private String bestTitle1 = "", bestCategory1 = "", bestImageUrl1 = "";
    private float highestAvg1 = -1;
    private int bestRatingCount1 = 0;

    private String bestTitle2 = "", bestCategory2 = "", bestImageUrl2 = "";
    private float highestAvg2 = -1;
    private int bestRatingCount2 = 0;

    private String bestTitle3 = "", bestCategory3 = "", bestImageUrl3 = "";
    private float highestAvg3 = -1;
    private int bestRatingCount3 = 0;

    private String bestEventId1 = "", bestEventId2 = "", bestEventId3 = "", worstEventId= "";

    // worst rated event
    private String worstTitle = "", worstCategory = "", worstImageUrl = "";
    private float lowestAvg = Float.MAX_VALUE;
    private int worstRatingCount = 0;

    private Map<Integer, Integer> starCountMap = new HashMap<>();
    private Map<String, List<Float>> ratingsByEvent = new LinkedHashMap<>();
    private List<String> ratingEventIds = new ArrayList<>();


    // Track completed feedback queries for all events
    private int totalEvents = 0;
    private int processedEvents = 0;
    private List<String> recentEventIds = new ArrayList<>();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.report_combined_feedback_view, container, false);

        db = FirebaseFirestore.getInstance();

        // Init Views
        globalAverageRatingText = view.findViewById(R.id.globalAverageRatingText);
        count5Stars = view.findViewById(R.id.count5Stars);
        count4Stars = view.findViewById(R.id.count4Stars);
        count3Stars = view.findViewById(R.id.count3Stars);
        count2Stars = view.findViewById(R.id.count2Stars);
        count1Star = view.findViewById(R.id.count1Star);

        progressBar5Stars = view.findViewById(R.id.progressBar5Stars);
        progressBar4Stars = view.findViewById(R.id.progressBar4Stars);
        progressBar3Stars = view.findViewById(R.id.progressBar3Stars);
        progressBar2Stars = view.findViewById(R.id.progressBar2Stars);
        progressBar1Star = view.findViewById(R.id.progressBar1Star);

        eventImage1 = view.findViewById(R.id.eventImage1);
        eventTitle1 = view.findViewById(R.id.eventTitle1);
        eventRating1 = view.findViewById(R.id.eventRating1);

        eventImage2 = view.findViewById(R.id.eventImage2);
        eventTitle2 = view.findViewById(R.id.eventTitle2);
        eventRating2 = view.findViewById(R.id.eventRating2);

        eventImage3 = view.findViewById(R.id.eventImage3);
        eventTitle3 = view.findViewById(R.id.eventTitle3);
        eventRating3 = view.findViewById(R.id.eventRating3);

        worstEventImage = view.findViewById(R.id.worstEventImage);
        worstEventTitle = view.findViewById(R.id.worstEventTitle);
        worstEventCategory = view.findViewById(R.id.worstEventCategory);
        worstEventRating = view.findViewById(R.id.worstEventRating);
        worstRatedEventCard = view.findViewById(R.id.worstRatedEventCard);

        ratingsBarChart = view.findViewById(R.id.weeklyRatingsBarChart);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        eventImage1.setOnClickListener(v -> {
            if (!bestEventId1.isEmpty()) {
                Intent intent = new Intent(getContext(), EventDetailActivity.class);
                intent.putExtra("eventId", bestEventId1);
                startActivity(intent);
            }
        });

        eventImage2.setOnClickListener(v -> {
            if (!bestEventId2.isEmpty()) {
                Intent intent = new Intent(getContext(), EventDetailActivity.class);
                intent.putExtra("eventId", bestEventId2);
                startActivity(intent);
            }
        });

        eventImage3.setOnClickListener(v -> {
            if (!bestEventId3.isEmpty()) {
                Intent intent = new Intent(getContext(), EventDetailActivity.class);
                intent.putExtra("eventId", bestEventId3);
                startActivity(intent);
            }
        });
        worstRatedEventCard.setOnClickListener(v -> {
            if (!worstEventId.isEmpty()) {
                Intent intent = new Intent(getContext(), EventDetailActivity.class);
                intent.putExtra("eventId", worstEventId);
                startActivity(intent);
            }
        });


        // Initialize starCountMap keys with zero
        for (int i = 1; i <= 5; i++) starCountMap.put(i, 0);

        loadFeedbackInsights();

        return view;
    }
    private void loadFeedbackInsights() {
        progressBarLoading.setVisibility(View.VISIBLE);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .collection("eventsCreated")
                .get()
                .addOnSuccessListener(createdEventSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (DocumentSnapshot doc : createdEventSnapshots) {
                        eventIds.add(doc.getId());
                    }

                    totalEvents = eventIds.size();
                    processedEvents = 0;

                    if (totalEvents == 0) {
                        checkIfAllProcessed();
                    }

                    for (String eventId : eventIds) {
                        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
                            if (!eventDoc.exists()) {
                                processedEvents++;
                                checkIfAllProcessed();
                                return;
                            }

                            String title = eventDoc.getString("eventTitle");
                            String category = eventDoc.getString("eventCategory");
                            String imageUrl = eventDoc.getString("imageUrl");
                            String eventDateString = eventDoc.getString("eventDate");

                            if (eventDateString != null) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                                    eventDate = sdf.parse(eventDateString);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    eventDate = null;
                                }
                            }

                db.collection("events").document(eventId)
                        .collection("feedback")
                        .get()
                        .addOnSuccessListener(feedbacks -> {
                            float eventSum = 0;
                            int eventCount = 0;

                            for (DocumentSnapshot feedback : feedbacks) {
                                Long ratingLong = feedback.getLong("rating");
                                if (ratingLong != null) {
                                    int rating = ratingLong.intValue();
                                    globalRatingSum += rating;
                                    globalRatingCount++;

                                    starCountMap.put(rating, starCountMap.get(rating) + 1);
                                    eventSum += rating;
                                    eventCount++;

                                    // Group by event title
                                    if (!ratingsByEvent.containsKey(title)) {
                                        ratingsByEvent.put(title, new ArrayList<>());
                                        ratingEventIds.add(eventId);  // maintain order with title
                                    }
                                    ratingsByEvent.get(title).add((float) rating);


                                }
                            }

                            // Update best-rated event
                            if (eventCount > 0) {
                                float avg = eventSum / eventCount;
                                if (avg > highestAvg1) {
                                    // Shift 2nd to 3rd
                                    highestAvg3 = highestAvg2; bestEventId3 = bestEventId2;
                                    bestTitle3 = bestTitle2; bestCategory3 = bestCategory2; bestImageUrl3 = bestImageUrl2; bestRatingCount3 = bestRatingCount2;
                                    // Shift 1st to 2nd
                                    highestAvg2 = highestAvg1; bestEventId2 = bestEventId1;
                                    bestTitle2 = bestTitle1; bestCategory2 = bestCategory1; bestImageUrl2 = bestImageUrl1; bestRatingCount2 = bestRatingCount1;
                                    // Set new 1st
                                    highestAvg1 = avg;  bestEventId1 = eventId;
                                    bestTitle1 = title; bestCategory1 = category; bestImageUrl1 = imageUrl; bestRatingCount1 = eventCount;

                                } else if (avg > highestAvg2) {
                                    // Shift 2nd to 3rd
                                    highestAvg3 = highestAvg2; bestEventId3 = bestEventId2;
                                    bestTitle3 = bestTitle2; bestCategory3 = bestCategory2; bestImageUrl3 = bestImageUrl2; bestRatingCount3 = bestRatingCount2;
                                    // Set new 2nd
                                    highestAvg2 = avg;  bestEventId2 = eventId;
                                    bestTitle2 = title; bestCategory2 = category; bestImageUrl2 = imageUrl; bestRatingCount2 = eventCount;

                                } else if (avg > highestAvg3) {
                                    // Shift 2nd to 3rd
                                    highestAvg3 = avg;  bestEventId3 = eventId;
                                    bestTitle3 = title; bestCategory3 = category; bestImageUrl3 = imageUrl; bestRatingCount3 = eventCount;
                                }// Least rated event
                                if (avg < lowestAvg) {
                                    worstEventId = eventId;
                                    lowestAvg = avg; worstTitle = title; worstCategory = category; worstImageUrl = imageUrl; worstRatingCount = eventCount;
                                }
                            }

                            processedEvents++;
                            checkIfAllProcessed();
                        });
                        });
                    } loadRecentEventFeedbackChart(currentUserId);
                });
    }

    private void updateGlobalAverage(float sum, int count) {
        if (count > 0) {
            float avg = sum / count;
            globalAverageRatingText.setText("Global Average Rating: " + df.format(avg) + "★");
        } else {
            globalAverageRatingText.setText("No ratings available");
        }
    }

    private void updateStarDistribution(Map<Integer, Integer> starCounts) {
        int total = 0;
        for (int count : starCounts.values()) total += count;

        count5Stars.setText(String.valueOf(starCounts.get(5)));
        count4Stars.setText(String.valueOf(starCounts.get(4)));
        count3Stars.setText(String.valueOf(starCounts.get(3)));
        count2Stars.setText(String.valueOf(starCounts.get(2)));
        count1Star.setText(String.valueOf(starCounts.get(1)));

        progressBar5Stars.setProgress(getPercent(starCounts.get(5), total));
        progressBar4Stars.setProgress(getPercent(starCounts.get(4), total));
        progressBar3Stars.setProgress(getPercent(starCounts.get(3), total));
        progressBar2Stars.setProgress(getPercent(starCounts.get(2), total));
        progressBar1Star.setProgress(getPercent(starCounts.get(1), total));
    }

    private int getPercent(int count, int total) {
        return total == 0 ? 0 : Math.round((count * 100f) / total);
    }

    private void updateTopThreeBestRatedEvents() {
        eventTitle1.setText(bestTitle1);
        eventRating1.setText(String.format(Locale.getDefault(), "%.1f ★ (%d ratings)", highestAvg1, bestRatingCount1));
        Glide.with(getContext()).load(bestImageUrl1).into(eventImage1);

        if (!bestTitle2.isEmpty()) {
            eventTitle2.setText(bestTitle2);
            eventRating2.setText(String.format(Locale.getDefault(), "%.1f ★ (%d ratings)", highestAvg2, bestRatingCount2));
            Glide.with(getContext()).load(bestImageUrl2).into(eventImage2);
        }

        if (!bestTitle3.isEmpty()) {
            eventTitle3.setText(bestTitle3);
            eventRating3.setText(String.format(Locale.getDefault(), "%.1f ★ (%d ratings)", highestAvg3, bestRatingCount3));
            Glide.with(getContext()).load(bestImageUrl3).into(eventImage3);
        }

        progressBarLoading.setVisibility(View.GONE);
    }

    private void loadRecentEventFeedbackChart(String userId) {
        Log.d("RecentEvents", "Loading recent events for user: " + userId);

        db.collection("events")
                .whereEqualTo("organizer", userId)
                .orderBy("eventDate", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<EventInfo> eventList = new ArrayList<>();
                    Date today = new Date();

                    for (DocumentSnapshot doc : snapshot) {
                        String id = doc.getId();
                        String title = doc.getString("eventTitle");
                        String status = doc.getString("status");
                        String dateStr = doc.getString("eventDate");

                        if (title == null || status == null || dateStr == null) continue;

                        try {
                            Date eventDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).parse(dateStr);
                            if (eventDate != null && eventDate.before(today) && !status.equalsIgnoreCase("cancelled")) {
                                eventList.add(new EventInfo(id, title, 0)); // avgRating will be filled later
                            }
                        } catch (ParseException e) {
                            Log.e("RecentEvents", "Date parse error: " + dateStr);
                        }

                        if (eventList.size() == 10) break;
                    }

                    if (eventList.isEmpty()) {
                        Log.d("RecentEvents", "No passed & not cancelled events.");
                        return;
                    }

                    AtomicInteger processed = new AtomicInteger(0);

                    for (EventInfo event : eventList) {
                        db.collection("events").document(event.id).collection("feedback").get()
                                .addOnSuccessListener(feedbacks -> {
                                    float sum = 0;
                                    int count = 0;
                                    for (DocumentSnapshot f : feedbacks) {
                                        Long r = f.getLong("rating");
                                        if (r != null) {
                                            sum += r;
                                            count++;
                                        }
                                    }
                                    event.avgRating = count > 0 ? sum / count : 0f;

                                    if (processed.incrementAndGet() == eventList.size()) {
                                        updateRecentEventBarChart(eventList);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("RecentEvents", "Error fetching recent events", e));
    }


    private void updateRecentEventBarChart(List<EventInfo> events) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        recentEventIds.clear();

        int index = 0;
        for (EventInfo e : events) {
            entries.add(new BarEntry(index, e.avgRating));
            // Shorten long labels
            String label = e.title.length() > 12 ? e.title.substring(0, 10) + "…" : e.title;
            labels.add(label);
            recentEventIds.add(e.id);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Recent Event Ratings");
        dataSet.setColors(Color.parseColor("#5A6ACF"));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        // Show the average rating on top
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f★", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        ratingsBarChart.setData(barData);
        ratingsBarChart.setFitBars(true);

        // X Axis (BOTTOM axis for vertical chart)
        XAxis xAxis = ratingsBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextSize(10f);

        // Optional: remove padding at sides for tighter spacing
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(events.size() - 0.5f);

        // Y Axis
        ratingsBarChart.getAxisLeft().setAxisMinimum(0f);
        ratingsBarChart.getAxisLeft().setAxisMaximum(5f);
        ratingsBarChart.getAxisRight().setEnabled(false);


        ratingsBarChart.getDescription().setEnabled(false);
        ratingsBarChart.animateY(800);
        ratingsBarChart.invalidate();

        // Chart click interaction
        ratingsBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int idx = (int) e.getX();
                if (idx >= 0 && idx < recentEventIds.size()) {
                    String selectedEventId = recentEventIds.get(idx);
                    Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                    intent.putExtra("eventId", selectedEventId);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {}
        });
    }


    private void updateLowestRatedEvent() {
        if (!worstTitle.isEmpty()) {
            worstEventTitle.setText(worstTitle);
            worstEventCategory.setText(worstCategory);
            worstEventRating.setText(String.format(Locale.getDefault(), "%.1f ★ (%d ratings)", lowestAvg, worstRatingCount));
            Glide.with(getContext()).load(worstImageUrl).into(worstEventImage);
            worstRatedEventCard.setVisibility(View.VISIBLE);
        } else {
            worstRatedEventCard.setVisibility(View.GONE);
        }
    }

    private void checkIfAllProcessed() {
            if (processedEvents == totalEvents) {
                updateGlobalAverage(globalRatingSum, globalRatingCount);
                updateStarDistribution(starCountMap);
                updateTopThreeBestRatedEvents();
                updateLowestRatedEvent();


                progressBarLoading.setVisibility(View.GONE);
            }
        }
    private static class EventInfo {
        String id;
        String title;
        float avgRating;

        EventInfo(String id, String title, float avgRating) {
            this.id = id;
            this.title = title;
            this.avgRating = avgRating;
        }
    }

    }