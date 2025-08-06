package com.example.dogpal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.R;
import com.example.dogpal.models.Notification;

import java.text.DateFormat;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NoteViewHolder> {

    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Notification note = notifications.get(position);
        holder.title.setText(note.getTitle());
        holder.message.setText(note.getMessage());
        holder.timestamp.setText(DateFormat.getDateTimeInstance().format(note.getTimestamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, timestamp;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            message = itemView.findViewById(R.id.noteMessage);
            timestamp = itemView.findViewById(R.id.noteTime);
        }
    }
}
