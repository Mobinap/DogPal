package com.example.dogpal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.R;
import com.example.dogpal.models.Dog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DogSelectAdapter extends RecyclerView.Adapter<DogSelectAdapter.DogViewHolder> {

    private Context context;
    private List<Dog> dogList;
    private Set<String> selectedDogIds = new HashSet<>(); // Store selected dog IDs

    public DogSelectAdapter(Context context, List<Dog> dogList) {
        this.context = context;
        this.dogList = dogList;
    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dog_select, parent, false);
        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog dog = dogList.get(position);

        holder.nameText.setText(dog.getName());
        holder.breedText.setText("Breed: " + dog.getBreed());
        holder.ageText.setText("Age: " + dog.getAge());
        holder.genderText.setText("Gender: " + dog.getGender());

        Glide.with(context).load(dog.getImageUrl()).into(holder.dogImage);

        // Checkbox logic
        holder.checkbox.setChecked(selectedDogIds.contains(dog.getDogId()));

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedDogIds.add(dog.getDogId());
            } else {
                selectedDogIds.remove(dog.getDogId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            boolean newChecked = !holder.checkbox.isChecked();
            holder.checkbox.setChecked(newChecked);
        });
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    // Get selected dog objects
    public List<Dog> getSelectedDogs() {
        List<Dog> selected = new ArrayList<>();
        for (Dog dog : dogList) {
            if (selectedDogIds.contains(dog.getDogId())) {
                selected.add(dog);
            }
        }
        return selected;
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {
        ImageView dogImage;
        TextView nameText, breedText, ageText, genderText;
        CheckBox checkbox;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = itemView.findViewById(R.id.dogImage);
            nameText = itemView.findViewById(R.id.dogName);
            breedText = itemView.findViewById(R.id.dogBreed);
            ageText = itemView.findViewById(R.id.dogAge);
            genderText = itemView.findViewById(R.id.dogGender);
            checkbox = itemView.findViewById(R.id.dogCheckBox);
        }
    }
}
