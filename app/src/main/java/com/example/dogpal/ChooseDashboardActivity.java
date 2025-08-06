package com.example.dogpal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.organizer.DashboardOrganizerActivity;
import com.example.dogpal.Attendee.DashboardAttendeeActivity;

public class ChooseDashboardActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      // setContentView(R.layout.choose_dashboards);
       setupLayoutWithNav(R.layout.choose_dashboards);
       // bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();  });

        findViewById(R.id.btnOrganizeEvents).setOnClickListener(v ->
                startActivity(new Intent(ChooseDashboardActivity.this, DashboardOrganizerActivity.class)));
        findViewById(R.id.btnSearchEvents).setOnClickListener(v ->
                startActivity(new Intent(ChooseDashboardActivity.this, DashboardAttendeeActivity.class)));
    }
}
