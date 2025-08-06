package com.example.dogpal.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.R;
import com.example.dogpal.models.User;
import com.example.dogpal.profile.UserProfileActivity;

import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private List<User> attendees;
    private Context context;
    private String viewerParticipationStatus;
    public AttendeeAdapter(Context context, List<User> attendees, String viewerParticipationStatus) {
        this.context = context;
        this.attendees = attendees;
        this.viewerParticipationStatus = viewerParticipationStatus;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;

        public ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.attendeeName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = attendees.get(position);
        holder.nameView.setText(user.getName());
        //holder.nameView.setText(attendees.get(position));

        // Set up a click listener on the name
        holder.nameView.setOnClickListener(v -> {
            // When an attendee name is clicked, open their profile
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", user.getUserId());  // Pass the user ID
            intent.putExtra("viewerParticipationStatus", viewerParticipationStatus);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }
}
