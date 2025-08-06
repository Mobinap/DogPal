package com.example.dogpal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogpal.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LatLng latLng;
    private String addressText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve latitude and longitude from the intent (if available)
        double latitude = getIntent().getDoubleExtra("lat", 0);
        double longitude = getIntent().getDoubleExtra("lng", 0);

        // Create a LatLng object from the passed latitude and longitude
        if (latitude != 0 && longitude != 0) {
            latLng = new LatLng(latitude, longitude);
        }


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });
        TextView btnDone = findViewById(R.id.mapsDoneButton);

        btnDone.setOnClickListener(v -> {
            if (latLng != null){
                // Return data to previous activity
                Toast.makeText(this, "Event's location selected successfully.", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", latLng.latitude);
                resultIntent.putExtra("lng", latLng.longitude);
                resultIntent.putExtra("address", addressText); // <-- Add address here
                setResult(RESULT_OK, resultIntent);
                finish();
            }else{
                Toast.makeText(this, "Please tap on the map to select a location.", Toast.LENGTH_SHORT).show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Clear previous markers
        mMap = googleMap;

        // Center the map initially
        LatLng center = new LatLng(3.1390, 101.6869); // Kuala Lumpur
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10f));

        if (latLng != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Current Event Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }

//________________________________zoom in/out --------------------------------
        // Handle zoom in/out buttons
        ImageButton zoomIn = findViewById(R.id.zoom_in_button);
        ImageButton zoomOut = findViewById(R.id.zoom_out_button);

        zoomIn.setOnClickListener(v -> {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        });

        zoomOut.setOnClickListener(v -> {
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

//------------------set on map click listener---------------------------------
        // Listen for map taps
        mMap.setOnMapClickListener(latLng -> {
            // Clear previous markers
            mMap.clear();

            // Get address using Geocoder
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            String addressText = "";

            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressText = address.getAddressLine(0); // Full address
                } else {
                    addressText = "Unknown location";
                }
            } catch (IOException e) {
                e.printStackTrace();
                addressText = "Could not fetch address";
            }

            // Add a marker with the address as the title
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(addressText)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );

            // Show the info window immediately
            if (marker != null) {
                marker.showInfoWindow();
            }


            // Save location and address
            this.latLng = latLng;
            this.addressText = addressText;

        });

    }}