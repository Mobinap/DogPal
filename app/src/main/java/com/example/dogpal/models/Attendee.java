package com.example.dogpal.models;

public class Attendee {
    private String userId;
    private String participationStatus;

    public Attendee(){

    }
    public Attendee(String userId, String participationStatus) {
        this.userId = userId;
        this.participationStatus = participationStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(String participationStatus) {
        this.participationStatus = participationStatus;
    }
}
