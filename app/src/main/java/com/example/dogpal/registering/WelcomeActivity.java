package com.example.dogpal.registering;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        findViewById(R.id.tvLogin).setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class))
        );

        findViewById(R.id.btnSignup).setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class))
        );
    }
}
