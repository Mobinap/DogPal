package com.example.dogpal.registering;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dogpal.CloudinaryUploader;
import com.example.dogpal.HomeActivity;
import com.example.dogpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadVaccineActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private ImageView imgDogPhoto;
    private Button btnUploadVaccineFinish;
    private Uri selectedImageUri;
    private String dogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_uploadvaccine);

        checkPermissions();


        imgDogPhoto = findViewById(R.id.imgDogPhoto);
        btnUploadVaccineFinish = findViewById(R.id.btnUploadVaccineFinish);

        imgDogPhoto.setOnClickListener(v -> openGallery());

        dogId = getIntent().getStringExtra("dogId");

        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(UploadVaccineActivity.this, "Please complete the registration first.", Toast.LENGTH_SHORT).show();
                // Don't call finish() or super — this blocks going back
            }
        });


        btnUploadVaccineFinish.setOnClickListener(v -> {
            if (selectedImageUri != null && dogId != null && !dogId.isEmpty()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    CloudinaryUploader.uploadImageToCloudinary(
                            UploadVaccineActivity.this,
                            selectedImageUri,
                            "users",     // Firestore root collection
                            userId,                  // User's document ID
                            "dogs",                  // Subcollection (dogs)
                            dogId, // Specific dog document ID
                            "vaccineUrl",
                            new CloudinaryUploader.ImageUploadCallback() {  // callback
                                @Override
                                public void onSuccess(String imageUrl) {
                                    // Handle success - Save the image URL to Firestore or use it in your app
                                    Log.d("UploadVaccine", "Image uploaded successfully: " + imageUrl);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Handle failure
                                    Toast.makeText(UploadVaccineActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );


                    Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadVaccineActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an image and ensure Dog ID is valid", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Check and request the storage permission
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgDogPhoto.setImageURI(selectedImageUri);
        }
    }

}

