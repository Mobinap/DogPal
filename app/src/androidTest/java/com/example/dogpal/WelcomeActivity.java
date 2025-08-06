package com.example.dogpal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dogpal.registering.LoginActivity;
import com.example.dogpal.registering.SignUpActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(com.example.dogpal.registering.WelcomeActivity.this, LoginActivity.class))
        );

        findViewById(R.id.btnSignup).setOnClickListener(v ->
                startActivity(new Intent(com.example.dogpal.registering.WelcomeActivity.this, SignUpActivity.class))
        );
    }
}
