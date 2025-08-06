package com.example.dogpal.report;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dogpal.R;

import com.example.dogpal.report.SingleEventInfoFragment;
import com.example.dogpal.report.SingleEventDemographicsFragment;
import com.example.dogpal.report.SingleEventFeedbackFragment;
import com.example.dogpal.report.CombinedReportInfoFragment;
import com.example.dogpal.report.CombinedReportDemographicsFragment;
import com.example.dogpal.report.CombinedReportFeedbackFragment;



public class ReportActivity extends AppCompatActivity {

    Button btnInfo, btnDemographics, btnFeedback;
    TextView reportTitle;
    Fragment fragment = null;
    String reportType, eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

         reportType = getIntent().getStringExtra("reportType");
         eventId = getIntent().getStringExtra("eventId");


        reportTitle = findViewById(R.id.reportTitle);
        if ("single".equals(reportType)) {
            reportTitle.setText("Event Report");
        } else {
            reportTitle.setText("General Report");
        }

        btnInfo = findViewById(R.id.btnInfo);
        btnDemographics = findViewById(R.id.btnDemographics);
        btnFeedback = findViewById(R.id.btnFeedback);

        changeTabSelection("Info");  // Set default filter

        // Set click listeners for buttons
        btnInfo.setOnClickListener(v -> changeTabSelection("Info"));
        btnDemographics.setOnClickListener(v -> changeTabSelection("Demographics"));
        btnFeedback.setOnClickListener(v -> changeTabSelection("Feedback"));


    }
    private void changeTabSelection(String selectedStatus) {
        // Reset all buttons to unselected state
        resetTabs();

        // Update the UI based on the selected category
        switch (selectedStatus) {
            case "Info":
                btnInfo.setSelected(true);
                btnInfo.setTextColor(getResources().getColor(android.R.color.white));
                btnInfo.setBackgroundResource(R.drawable.tab_button_bg);
                //handle
                fragment = reportType.equals("single") ? new SingleEventInfoFragment() : new CombinedReportInfoFragment();
                break;
            case "Demographics":
                btnDemographics.setSelected(true);
                btnDemographics.setTextColor(getResources().getColor(android.R.color.white));
                btnDemographics.setBackgroundResource(R.drawable.tab_button_bg);
                //handle
                fragment = reportType.equals("single") ? new SingleEventDemographicsFragment() : new CombinedReportDemographicsFragment();
                break;
            case "Feedback":
                btnFeedback.setSelected(true);
                btnFeedback.setTextColor(getResources().getColor(android.R.color.white));
                btnFeedback.setBackgroundResource(R.drawable.tab_button_bg);
                //handle
                fragment = reportType.equals("single") ? new SingleEventFeedbackFragment() : new CombinedReportFeedbackFragment();

                break;
        }
        if (fragment != null) {
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            fragment.setArguments(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.reportContainer, fragment)
                    .commit();
        }
    }

    private void resetTabs() {
        // Reset all buttons to unselected state
        btnInfo.setSelected(false);
        btnInfo.setTextColor(getResources().getColor(R.color.tab_button_text_selector));
        btnInfo.setBackgroundColor(getResources().getColor(android.R.color.white));

        btnDemographics.setSelected(false);
        btnDemographics.setTextColor(getResources().getColor(R.color.tab_button_text_selector));
        btnDemographics.setBackgroundColor(getResources().getColor(android.R.color.white));

        btnFeedback.setSelected(false);
        btnFeedback.setTextColor(getResources().getColor(R.color.tab_button_text_selector));
        btnFeedback.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

}
