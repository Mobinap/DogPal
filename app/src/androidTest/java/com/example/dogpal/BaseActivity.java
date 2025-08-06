package com.example.dogpal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.organizer.CreateEventActivity;
import com.example.dogpal.profile.ProfileActivity;
import com.example.dogpal.Attendee.SearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(this, HomeActivity.class));
                    return true;
                case R.id.nav_search:
                    startActivity(new Intent(this, SearchActivity.class));
                    return true;
                case R.id.nav_create:
                    startActivity(new Intent(this, CreateEventActivity.class));
                    return true;
                case R.id.nav_dashboard:
                    startActivity(new Intent(this, ChooseDashboardActivity.class));
                    return true;
                case R.id.nav_profile:
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
            }
            return false;
        });
    }
}
