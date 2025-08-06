package com.example.dogpal;

import java.util.List;

public class Event {
    private boolean breedRestrictionEnabled;
    private List<String> allowedBreeds;
    private String limitType;
    private int maxParticipants;
    private int maxDogs;

    public Event(boolean breedRestrictionEnabled, List<String> allowedBreeds,
                 String limitType, int maxParticipants, int maxDogs) {
        this.breedRestrictionEnabled = breedRestrictionEnabled;
        this.allowedBreeds = allowedBreeds;
        this.limitType = limitType;
        this.maxParticipants = maxParticipants;
        this.maxDogs = maxDogs;
    }

    public boolean isBreedRestrictionEnabled() {
        return breedRestrictionEnabled;
    }

    public List<String> getAllowedBreeds() {
        return allowedBreeds;
    }

    public String getLimitType() {
        return limitType;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getMaxDogs() {
        return maxDogs;
    }
}
