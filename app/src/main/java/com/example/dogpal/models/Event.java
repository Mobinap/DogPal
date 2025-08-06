package com.example.dogpal.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Event {
    private String eventId, organizer, eventTitle, eventDescription,
            eventCategory,eventDate, eventTime, eventLocation, status, imageUrl;
    private double latitude;
    private double longitude;

    private String limitType; // NEW: "No Limit", "Limit by Dog Owners", "Limit by Dogs", "Limit by Both"
    private int maxParticipants; // Max dog owners
    private int maxDogs; // NEW: Max dogs allowed
    private boolean breedRestrictionEnabled;
    private List<String> allowedBreeds;
    private Timestamp createdAt;

    // Default constructor
    public Event() {
    }
    public Event(boolean breedRestrictionEnabled, List<String> allowedBreeds, String limitType, int maxParticipants, int maxDogs) {
        this.breedRestrictionEnabled = breedRestrictionEnabled;
        this.allowedBreeds = allowedBreeds;
        this.limitType = limitType;
        this.maxParticipants = maxParticipants;
        this.maxDogs = maxDogs;
    }

    // Constructor with all fields
    public Event(String eventId, String organizer, String eventTitle, String eventDescription, String eventCategory,
                 String eventDate, String eventTime, String eventLocation, double latitude, double longitude,
                 String imageUrl, String limitType, int maxParticipants, int maxDogs, boolean breedRestrictionEnabled, List<String> allowedBreeds) {
        this.eventId = eventId;
        this.organizer = organizer;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventCategory = eventCategory;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventLocation = eventLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.limitType = limitType;
        this.maxParticipants = maxParticipants;
        this.maxDogs = maxDogs;
        this.breedRestrictionEnabled = breedRestrictionEnabled;
        this.allowedBreeds = allowedBreeds;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isBreedRestrictionEnabled() {
        return breedRestrictionEnabled;
    }

    public void setBreedRestrictionEnabled(boolean breedRestrictionEnabled) {
        this.breedRestrictionEnabled = breedRestrictionEnabled;
    }

    public List<String> getAllowedBreeds() {
        return allowedBreeds;
    }

    public void setAllowedBreeds(List<String> allowedBreeds) {
        this.allowedBreeds = allowedBreeds;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public int getMaxDogs() {
        return maxDogs;
    }

    public void setMaxDogs(int maxDogs) {
        this.maxDogs = maxDogs;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}