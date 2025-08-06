package com.example.dogpal.models;

import com.google.firebase.Timestamp;

public class Notification {
        private String title;
        private String message;
        private Timestamp timestamp;

        public Notification() {} // Required for Firestore

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public Timestamp getTimestamp() { return timestamp; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}



