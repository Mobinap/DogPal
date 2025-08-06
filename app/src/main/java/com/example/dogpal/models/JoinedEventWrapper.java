package com.example.dogpal.models;

public class JoinedEventWrapper {
    private Event event;
    private String participationStatus;

    public JoinedEventWrapper() {}

    public JoinedEventWrapper(Event event, String participationStatus) {
        this.event = event;
        this.participationStatus = participationStatus;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(String participationStatus) {
        this.participationStatus = participationStatus;
    }
}

