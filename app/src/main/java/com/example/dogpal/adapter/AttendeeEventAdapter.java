package com.example.dogpal.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.R;
import com.example.dogpal.models.Event;
import com.example.dogpal.models.JoinedEventWrapper;
import com.example.dogpal.Attendee.EventDetailActivity;
import com.example.dogpal.Attendee.FeedbackActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AttendeeEventAdapter extends RecyclerView.Adapter<AttendeeEventAdapter.AttendeeEventViewHolder> {
    private List<JoinedEventWrapper> joinedEventList;
    private Context context;
    private String currentUserId;
    private boolean isDashboard;


    public AttendeeEventAdapter(Context context, List<JoinedEventWrapper> joinedEventList, String currentUserId, boolean isDashboard) {
        this.context = context;
        this.joinedEventList = joinedEventList;
        this.currentUserId = currentUserId;
        this.isDashboard = isDashboard;
    }

    @NonNull
    @Override
    public AttendeeEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new AttendeeEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeEventViewHolder holder, int position) {

        JoinedEventWrapper wrapper = joinedEventList.get(position);
        Event event = wrapper.getEvent();
        String participationStatus = wrapper.getParticipationStatus();

        holder.eventTitle.setText(event.getEventTitle());
        holder.eventCategory.setText(event.getEventCategory());
        holder.eventLocation.setText(event.getEventLocation());
        holder.eventDate.setText(event.getEventDate());

        // Load image with Glide or Picasso
        Glide.with(context)
                .load(event.getImageUrl())
                .placeholder(R.drawable.dog_placeholder) // Optional
                .into(holder.eventImage);

        // Determine event status
        String eventStatus = getEventStatus(event);

        // Show menu button only if on attendee dashboard and user is attending
        if (isDashboard && "Attending".equalsIgnoreCase(participationStatus)) {
            holder.eventMenuBtn.setVisibility(View.VISIBLE);

            holder.eventMenuBtn.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.eventMenuBtn);

// Dynamically add menu items based on event status
                if ("Upcoming".equals(eventStatus)) {
                   // popup.getMenu().add(Menu.NONE, R.id.menu_leave_event, Menu.NONE, "Leave Event");
                } else if ("Passed".equals(eventStatus)) {
                    popup.getMenu().add(Menu.NONE, R.id.menu_feedback, Menu.NONE, "Give Feedback");
                }

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_leave_event:
                            Toast.makeText(context, "Leave Event clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.menu_feedback:
                            Intent feedbackIntent = new Intent(context, FeedbackActivity.class);
                            feedbackIntent.putExtra("eventId", event.getEventId());
                            feedbackIntent.putExtra("userId", currentUserId);
                            context.startActivity(feedbackIntent);
                           // Toast.makeText(context, "Give Feedback", Toast.LENGTH_SHORT).show();
                            return true;
                        default:
                            return false;
                    }
                });
                popup.show();
            });
        } else {
            holder.eventMenuBtn.setVisibility(View.GONE);
        }


        // Set up the click listener for each event item
        // Navigate to event details on item click
        holder.itemView.setOnClickListener(v -> {
            String eventId = event.getEventId();
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("eventId", eventId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return joinedEventList.size();
    }

    // Method to update the event list and notify the adapter
    public void updateJoinedEventList(List<JoinedEventWrapper> list) {
        this.joinedEventList = list;
        notifyDataSetChanged();
    }

    // Method to get the event status (Upcoming or Passed)
    private String getEventStatus(Event event) {
        // Compare the event's date with the current date to determine if the event is Upcoming or Passed
        String dateString = event.getEventDate(); // e.g., "12/5/2025"
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        try {
            Date eventDate = dateFormat.parse(dateString);
            Date currentDate = new Date();
            if (eventDate.after(currentDate)) {
                return "Upcoming";
            } else return "Passed";
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    public static class AttendeeEventViewHolder  extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventCategory, eventLocation, eventDate;
        ImageButton eventMenuBtn;

        public AttendeeEventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventCategory = itemView.findViewById(R.id.eventCategory);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventMenuBtn = itemView.findViewById(R.id.eventMenuBtn);
        }
    }
}

