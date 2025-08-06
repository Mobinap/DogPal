package com.example.dogpal.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.R;
import com.example.dogpal.models.Dog;
import com.example.dogpal.profile.ProfileEditActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class DogEditAdapter extends RecyclerView.Adapter<DogEditAdapter.DogEditViewHolder> {
    private Context context;
    private List<Dog> dogList;
    private OnDogImageClickListener listener;
    private ProfileEditActivity.OnDogRemoveListener removeListener;

    private RecyclerView dogListRecyclerView;

    private FirebaseFirestore db;
    private String userId;

    public DogEditAdapter(List<Dog> dogList, Context context, OnDogImageClickListener listener, ProfileEditActivity.OnDogRemoveListener removeListener, RecyclerView recyclerView) {
        this.dogList = dogList;
        this.context = context;
        this.listener = listener;
        this.dogListRecyclerView = recyclerView;
        this.removeListener = removeListener;  // Initialize removeListener here
        // Initialize Firestore and get the current user ID
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    @NonNull
    @Override
    public DogEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dog_item_edit, parent, false);
        return new DogEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogEditViewHolder holder, int position) {
        Dog dog = dogList.get(position);

        holder.editDogName.setText(dog.getName());
        holder.editDogAge.setText(String.valueOf(dog.getAge()));
//----------------------------------------------------------------------------------------------

        // Set up the spinner with gender options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.editDogGender.setAdapter(adapter);

        // Set current gender selection
        int genderPosition = adapter.getPosition(dog.getGender());
        holder.editDogGender.setSelection(genderPosition);
//----------------------------------------------------------------------------------------------
        // Set up the spinner with breed options
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.breed_options,
                android.R.layout.simple_spinner_item
        );
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.editDogBreed.setAdapter(breedAdapter);

// Set current breed selection
        int breedPosition = breedAdapter.getPosition(dog.getBreed());
        holder.editDogBreed.setSelection(breedPosition);

//----------------------------------------------------------------------------------------------

        Glide.with(context)
                .load(dog.getImageUri() != null ? dog.getImageUri() : dog.getImageUrl())
                .placeholder(R.drawable.dog_placeholder)
                .into(holder.editDogImage);

        // Set up logic for picking a new image for the dog
        holder.editDogImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDogImageClick(position);
            }
        });

//----------------------------------------------------------------------------------------------

        holder.btnRemoveDog.setOnClickListener(v -> {
            String dogId = dog.getDogId();
            if (dogId != null && removeListener != null) {
                // Remove from Firestore first
                db.collection("users").document(userId).collection("dogs").document(dogId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // Remove from the list and notify adapter
                            dogList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, dogList.size());
                            // Show success message
                            Toast.makeText(context, "Dog removed from database", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Show error message
                            Toast.makeText(context, "Failed to delete dog from database", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    static class DogEditViewHolder extends RecyclerView.ViewHolder {
        ImageView editDogImage;
        EditText editDogName, editDogAge;
        Spinner editDogGender, editDogBreed;
        Button btnRemoveDog;

        public DogEditViewHolder(@NonNull View itemView) {
            super(itemView);
            editDogImage = itemView.findViewById(R.id.editDogImage);
            editDogName = itemView.findViewById(R.id.editDogName);
            editDogBreed = itemView.findViewById(R.id.breed_spinner);
            editDogAge = itemView.findViewById(R.id.editDogAge);
            editDogGender = itemView.findViewById(R.id.gender_spinner);
            btnRemoveDog = itemView.findViewById(R.id.btnRemoveDog);
        }
    }

    public List<Dog> getUpdatedDogList() {
        for (int i = 0; i < dogList.size(); i++) {
            DogEditViewHolder holder = (DogEditViewHolder) dogListRecyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                Dog dog = dogList.get(i);
                dog.setName(holder.editDogName.getText().toString());
                dog.setAge(Integer.parseInt(holder.editDogAge.getText().toString()));
                dog.setGender(holder.editDogGender.getSelectedItem().toString());
                dog.setBreed(holder.editDogBreed.getSelectedItem().toString());
            }
        }
        return dogList;
    }

    public interface OnDogImageClickListener {
        void onDogImageClick(int position);
    }
}