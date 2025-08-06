package com.example.dogpal.profile;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.CloudinaryUploader;
import com.example.dogpal.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dogpal.adapter.DogAdapter;
import com.example.dogpal.adapter.DogEditAdapter;
import com.example.dogpal.models.Dog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ProfileEditActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPhone;
    private ImageView profileImage;
    private Uri profileUri;
    private String profileImageUrl, userId, userEmail;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView dogListRecyclerView;
    private Button btnAddDog, btnFinish;
    private DogEditAdapter dogEditAdapter;
    private List<Dog> dogList = new ArrayList<>();
    private ActivityResultLauncher<Intent> dogImageLauncher;
    private int pendingDogImagePosition = -1;
    private Uri dogImageUri;
    private ProgressBar progressBarLoading;
    private List<String> removedDogIds = new ArrayList<>();
    private ActivityResultLauncher<Intent> profileImageLauncher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        profileImage = findViewById(R.id.profileImage);
        dogListRecyclerView = findViewById(R.id.dogListRecyclerView);
        btnAddDog = findViewById(R.id.btnAddDog);
        btnFinish = findViewById(R.id.btnFinish);
        progressBarLoading = findViewById(R.id.progressBarLoading);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        userId = user.getUid();
        userEmail = user.getEmail();

//------------------------------user profile launcher--------------------------

        profileImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                profileUri = result.getData().getData();
                Glide.with(this).load(profileUri).into(profileImage);
            }
        });

        profileImage.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            profileImageLauncher.launch(pickImage);
        });

        loadUserInfo();
//------------------------------dog profile launcher--------------------------

        // Initialize ActivityResultLauncher for dog image selection
        dogImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && pendingDogImagePosition != -1) {
                dogImageUri = result.getData().getData();

                // Update the dog object with the selected image URI
                Dog dog = dogList.get(pendingDogImagePosition);
                dog.setImageUri(dogImageUri.toString());

                // Notify the adapter to refresh the dog image preview in RecyclerView
                dogEditAdapter.notifyItemChanged(pendingDogImagePosition);
            }
        });

        //------------------------------dog adapter --------------------------

        dogListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //dog adapter initialize
        dogEditAdapter = new DogEditAdapter(dogList, this, position -> {
            pendingDogImagePosition = position;
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            dogImageLauncher.launch(pickImage);
        }, dogId -> {  // Pass the listener for removing dogs
            removedDogIds.add(dogId); // Mark for deletion
        }, dogListRecyclerView );
        dogListRecyclerView.setAdapter(dogEditAdapter);



        //------------------------------buttons --------------------------

        btnAddDog.setOnClickListener(v -> {
            dogList.add(new Dog());
            dogEditAdapter.notifyItemInserted(dogList.size() - 1);
            Toast.makeText(ProfileEditActivity.this, "New dog form added", Toast.LENGTH_SHORT).show();
        });

        btnFinish.setOnClickListener(v -> {

            progressBarLoading.setVisibility(View.VISIBLE);
            btnFinish.setEnabled(false);

            saveAllData(() -> {
                progressBarLoading.setVisibility(View.GONE);
                btnFinish.setEnabled(true);

                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });
        });
    }


    private void loadUserInfo() {
        db.collection("users").document(userId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                editUsername.setText(snapshot.getString("name"));
                editPhone.setText(snapshot.getString("phone"));
                editEmail.setText(userEmail);

                profileImageUrl = snapshot.getString("profileImageUrl");
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    runOnUiThread(() -> Glide.with(ProfileEditActivity.this).load(profileImageUrl).into(profileImage));                }

                loadDogs();
            }
        });
    }

    private void loadDogs() {
        db.collection("users").document(userId).collection("dogs").get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Dog dog = doc.toObject(Dog.class);
                        dog.setDogId(doc.getId());
                        dogList.add(dog);
                    }
                    dogEditAdapter.notifyDataSetChanged();
                });
    }

    private void saveAllData(Runnable onComplete) {
        // Make sure the dogList has the latest data from UI
        dogList = dogEditAdapter.getUpdatedDogList();
        // Save User Profile first
        saveUserProfile(() -> {
            // After user profile is saved, save dog data
            saveDogToFirestore(onComplete);
        });
    }

    private void saveUserProfile(Runnable onComplete) {
        // Save user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", editUsername.getText().toString());
        userData.put("phone", editPhone.getText().toString());

        // If there is a new profile image, upload it to Cloudinary
        if (profileUri != null) {
            CloudinaryUploader.uploadImageToCloudinary(this, profileUri, "users", userId, null, null, "profileImageUrl", new CloudinaryUploader.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    userData.put("profileImageUrl", imageUrl);  // Update the profile image URL after upload
                    db.collection("users").document(userId).update(userData)
                            .addOnSuccessListener(aVoid -> onComplete.run()) // Call onComplete once user data is saved
                            .addOnFailureListener(e -> {
                                runOnUiThread(() ->
                                        Toast.makeText(ProfileEditActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
                                );                            });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProfileEditActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If no new profile image is selected, just save the user data without updating the profile image URL
            db.collection("users").document(userId).update(userData)
                    .addOnSuccessListener(aVoid -> onComplete.run()) // Call onComplete once user data is saved
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileEditActivity.this, "Failed to save user profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveDogToFirestore(Runnable onComplete) {
        //delete removed dogs first
        int totalDogs = dogList.size();
        if (totalDogs == 0) {
            deleteRemovedDogs(onComplete);
            return;
        }
        final int[] remainingTasks = {totalDogs};
        //Loop through each dog in the list:
        for (Dog dog : dogList) {
            if (dog.getImageUri() != null) {
                CloudinaryUploader.uploadImageToCloudinary(this, Uri.parse(dog.getImageUri()),
                        "users", userId, "dogs", dog.getDogId(), "imageUrl",
                        new CloudinaryUploader.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        dog.setImageUrl(imageUrl);  // Update dog image URL
                        saveDog(dog); // Save dog data
                        checkIfDone(remainingTasks, onComplete);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure to upload dog image
                        saveDog(dog); // Save dog data without image URL
                        checkIfDone(remainingTasks, onComplete);
                    }
                });
            } else {
                saveDog(dog); // Save dog data without image upload
                checkIfDone(remainingTasks, onComplete);
            }
        }
    }

    private void saveDog(Dog dog) {
        // Check if the dog has an image URI that needs to be uploaded
        String dogName = dog.getName();
        String dogBreed = dog.getBreed();
        String dogGender= dog.getGender();
        int dogAge = dog.getAge();
        String dogImageUrl = dog.getImageUrl();

        // If there is no image URL, we keep it as an empty string
        if (dogImageUrl == null) {
            dogImageUrl = "";
        }

        // Create a map for the dog data if not using a Dog object directly
        Map<String, Object> dogData = new HashMap<>();
        dogData.put("name", dogName);
        dogData.put("breed", dogBreed);
        dogData.put("gender", dogGender);
        dogData.put("age", dogAge);
        dogData.put("imageUrl", dogImageUrl);

        String dogId = dog.getDogId() == null || dog.getDogId().isEmpty() ?
                db.collection("users").document(userId).collection("dogs").document().getId() :
                dog.getDogId();

        // Save the dog data to Firestore
        db.collection("users").document(userId).collection("dogs").document(dogId).set(dogData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileEditActivity", "Dog saved successfully: " + dogId);
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() ->
                            Toast.makeText(ProfileEditActivity.this, "Failed to save dog info", Toast.LENGTH_SHORT).show()
                    );                    Log.e("ProfileEditActivity", "Error saving dog", e);
                });
    }


    private void checkIfDone(int[] counter, Runnable onComplete) {
        counter[0]--;
        if (counter[0] == 0) {
            deleteRemovedDogs(onComplete);
        }
    }

    private void deleteRemovedDogs(Runnable onComplete) {
        if (removedDogIds.isEmpty()) {
            runOnUiThread(onComplete);
            return;
        }
        for (String dogId : removedDogIds) {
            db.collection("users").document(userId).collection("dogs").document(dogId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() ->
                                Toast.makeText(ProfileEditActivity.this, "Dog deleted", Toast.LENGTH_SHORT).show()
                        );
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() ->
                                Toast.makeText(ProfileEditActivity.this, "Dog deleted", Toast.LENGTH_SHORT).show()
                        );
                    });
        }
        removedDogIds.clear();

        // Once all dogs are deleted, call onComplete
        onComplete.run();
    }

    public interface OnDogRemoveListener {
        void onDogRemoved(String dogId);
    }


}