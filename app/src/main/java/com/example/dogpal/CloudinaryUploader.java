
package com.example.dogpal;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.dogpal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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

public class CloudinaryUploader {

    private static String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/dnyuanlgn/image/upload";
    private static String UPLOAD_PRESET = "unsigned_preset";

    // Define the ImageUploadCallback interface
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);  // This will be called when the image upload succeeds
        void onFailure(Exception e);     // This will be called when the image upload fails
    }


    // Method to upload image to Cloudinary
    public static void uploadImageToCloudinary(Context context, Uri imageUri,
                                               String collectionPath, String documentId,
                                               @Nullable String subCollection, @Nullable String subDocId, String fieldName, ImageUploadCallback callback) {
        Log.d("CloudinaryUploader", "Image URI: " + imageUri);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytes(inputStream);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url(CLOUDINARY_UPLOAD_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                        Log.e("CloudinaryUploader", "Upload failed", e);
                        callback.onFailure(e);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            String imageUrl = jsonObject.getString("secure_url");
                            Log.d("CloudinaryUploader", "Image URL: " + imageUrl);
                            // Save to Firestore with dynamic path
                            saveUrlToFirestore(imageUrl, fieldName, collectionPath, documentId, subCollection, subDocId);
                           // callback.onSuccess(imageUrl);  // Notify success

                            ((Activity) context).runOnUiThread(() -> {
                                callback.onSuccess(imageUrl);  // Run on UI thread
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Activity) context).runOnUiThread(() -> {
                                callback.onFailure(e);  // Also safe
                            });
                        }
                    } else {
                        Log.e("CloudinaryUploader", "Cloudinary upload failed: " + response.message());
                      //  callback.onFailure(new Exception("Cloudinary upload failed"));
                        ((Activity) context).runOnUiThread(() -> {
                            callback.onFailure(new Exception("Cloudinary upload failed"));
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(e);
        }
    }

    // Convert InputStream to byte array
    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Method to save the image URL to Firestore
    public static void saveUrlToFirestore(String imageUrl,String fieldName, String collectionPath,
                                          String documentId, String subCollection, String subDocId) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef;
        // Determine the document reference path (either nested or top-level)
        if (subCollection != null && subDocId != null) {
            docRef = db.collection(collectionPath)
                    .document(documentId)
                    .collection(subCollection)
                    .document(subDocId);
        } else {    // Path: collectionPath/documentId
            docRef = db.collection(collectionPath).document(documentId);
        }

        Map<String, Object> data = new HashMap<>();  // Prepare the data to be saved (image URL under a specific field)
        data.put(fieldName, imageUrl);
        // Merge the new field with the existing document
        docRef.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Image URL saved successfully."))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving image URL: ", e));
    }


}
