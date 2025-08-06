package com.example.dogpal;

import com.example.dogpal.models.Dog;
import com.example.dogpal.models.Event;

import java.util.List;

    public class EventJoinValidator {

        public static class ValidationResult {
            public boolean canJoin;
            public String errorMessage;
            public String reason; // <-- used for test assertions

            public ValidationResult(boolean canJoin, String errorMessage, String reason) {
                this.canJoin = canJoin;
                this.errorMessage = errorMessage;
                this.reason = reason;
            }

            // Optional convenience constructor
            public ValidationResult(boolean canJoin, String errorMessage) {
                this(canJoin, errorMessage, "");
            }
        }

        public static ValidationResult validateJoin(Event event, List<Dog> selectedDogs,
                                                    int currentOwners, int currentDogs) {
            // 1. No dogs selected
            if (selectedDogs == null || selectedDogs.isEmpty()) {
                return new ValidationResult(false, "No dogs selected", "No dogs selected");
            }

            // 2. Check breed restriction
            if (event.isBreedRestrictionEnabled()) {
                List<String> allowedBreeds = event.getAllowedBreeds();
                if (allowedBreeds != null && !(allowedBreeds.size() == 1 && allowedBreeds.get(0).equalsIgnoreCase("All breeds allowed"))) {
                    for (Dog dog : selectedDogs) {
                        if (!allowedBreeds.contains(dog.getBreed())) {
                            return new ValidationResult(false,
                                    "The selected dog \"" + dog.getName() + "\" does not match the breed restrictions.",
                                    "not allowed");
                        }
                    }
                }
            }

            // 3. Check participation limits
            String limitType = event.getLimitType();
            int maxOwners = event.getMaxParticipants();
            int maxDogs = event.getMaxDogs();
            int selectedDogCount = selectedDogs.size();

            switch (limitType) {
                case "No Limit":
                    return new ValidationResult(true, "", "");

                case "Limit by Dog Owners":
                    if (currentOwners >= maxOwners) {
                        return new ValidationResult(false, "The event has reached the maximum number of dog owners.", "dog owners");
                    }
                    break;

                case "Limit by Dogs":
                    if ((currentDogs + selectedDogCount) > maxDogs) {
                        return new ValidationResult(false, "The event has reached the maximum number of dogs.", "dogs");
                    }
                    break;

                case "Limit by Both":
                    if (currentOwners >= maxOwners) {
                        return new ValidationResult(false, "The event has reached the maximum number of dog owners.", "dog owners");
                    }
                    if ((currentDogs + selectedDogCount) > maxDogs) {
                        return new ValidationResult(false, "The event has reached the maximum number of dogs.", "dogs");
                    }
                    break;
            }

            // 4. If all checks passed
            return new ValidationResult(true, "", "");
        }
    }

