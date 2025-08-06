package com.example.dogpal.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.R;
import com.example.dogpal.models.Feedback;
import com.example.dogpal.profile.UserProfileActivity;

import java.util.List;

public class FeedbackViewAdapter extends RecyclerView.Adapter<FeedbackViewAdapter.FeedbackViewHolder> {

    private Context context;
    private List<Feedback> feedbackList;
    private String userId;

    public FeedbackViewAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        holder.userName.setText(feedback.getUserName());
        holder.userRating.setRating(feedback.getRating());
        holder.userComment.setText(feedback.getComment());

        // Load user profile image
        Glide.with(context)
                .load(feedback.getUserProfileUrl())
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.userProfileImage);

        // Click listener to view user profile
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", feedback.getUserId()); // Pass userId
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage;
        TextView userName, userComment;
        RatingBar userRating;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            userName = itemView.findViewById(R.id.userName);
            userComment = itemView.findViewById(R.id.feedbackComment);
            userRating = itemView.findViewById(R.id.ratingBar);
        }
    }
}
