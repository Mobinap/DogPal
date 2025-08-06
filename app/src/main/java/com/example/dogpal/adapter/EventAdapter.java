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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.R;
import com.example.dogpal.models.Event;
import com.example.dogpal.organizer.CreateEventActivity;
import com.example.dogpal.Attendee.EventDetailActivity;
import com.example.dogpal.organizer.OrganizerFeedbackActivity;
import com.example.dogpal.report.ReportActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList;
    private Context context;
    private Intent intent;
    private String currentUserId;
    private boolean isDashboard;


    public EventAdapter(Context context, List<Event> eventList, String currentUserId, boolean isDashboard) {
        this.context = context;
        this.eventList = eventList;
        this.currentUserId = currentUserId;
        this.isDashboard = isDashboard;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventTitle.setText(event.getEventTitle());
        holder.eventCategory.setText(event.getEventCategory());
        holder.eventLocation.setText(event.getEventLocation());
        holder.eventDate.setText(event.getEventDate());

        // Load image with Glide or Picasso
        Glide.with(context)
                .load(event.getImageUrl())
                .placeholder(R.drawable.dog_placeholder) // Optional
                .into(holder.eventImage);

        String eventStatus = getEventStatus(event);

        // Only show the menu if this is the dashboard and the user is the organizer
        if (isDashboard && event.getOrganizer().equals(currentUserId)) {
            holder.eventMenuBtn.setVisibility(View.VISIBLE);

            holder.eventMenuBtn.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.eventMenuBtn);

                // Adjust menu based on event status (Upcoming or Passed)
                if ("Upcoming".equals(eventStatus)) {
                    // Add specific menu items for upcoming events
                    popup.getMenu().add(Menu.NONE, R.id.menu_update_event, Menu.NONE, "Update Event");
                } else if ("Passed".equals(eventStatus)) {
                    // Add specific menu items for passed events
                    popup.getMenu().add(Menu.NONE, R.id.menu_view_ratings, Menu.NONE, "View Feedbacks");
                    popup.getMenu().add(Menu.NONE, R.id.menu_report, Menu.NONE, "Generate Report");
                }

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_update_event:
                             intent = new Intent(context, CreateEventActivity.class);
                            intent.putExtra("eventId", event.getEventId());
                            context.startActivity(intent);
                            return true;
                        case R.id.menu_view_ratings:
                             intent = new Intent(context, OrganizerFeedbackActivity.class);
                            intent.putExtra("eventId", event.getEventId());
                            context.startActivity(intent);
                            return true;
                        case R.id.menu_report:
                            intent = new Intent(context, ReportActivity.class);
                            intent.putExtra("reportType", "single");
                            intent.putExtra("eventId", event.getEventId());
                            context.startActivity(intent);

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
        holder.itemView.setOnClickListener(v -> {
            String eventId = event.getEventId();
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("eventId", eventId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
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

    // Method to update the event list and notify the adapter
public void updateEventList(List<Event> events) {
    this.eventList = events;
    notifyDataSetChanged();
}

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventCategory, eventLocation, eventDate;
        ImageButton eventMenuBtn;

        public EventViewHolder(View itemView) {
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

