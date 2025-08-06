package com.example.dogpal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpal.R;
import com.example.dogpal.models.Dog;

import java.util.List;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {
    private Context context;
    private List<Dog> dogList;


    public DogAdapter(Context context, List<Dog> dogList) {
        this.context = context;
        this.dogList = dogList;
    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dog_item, parent, false);
        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog dog = dogList.get(position);

        holder.dogName.setText("Name: " + dog.getName());
        holder.dogBreed.setText("Breed: " + dog.getBreed());
        holder.dogAge.setText("Age: " + dog.getAge());
        holder.dogGender.setText("Gender: " + dog.getGender());


        Glide.with(context)
                .load(dog.getImageUrl())
                .placeholder(R.drawable.dog_placeholder)
                .into(holder.dogImage);
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {
        ImageView dogImage;
        TextView dogName, dogBreed, dogAge, dogGender;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = itemView.findViewById(R.id.dogImage);
            dogName = itemView.findViewById(R.id.dogName);
            dogBreed = itemView.findViewById(R.id.dogBreed);
            dogAge = itemView.findViewById(R.id.dogAge);
            dogGender = itemView.findViewById(R.id.dogGender);
        }
    }
}


