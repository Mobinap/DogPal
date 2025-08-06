package com.example.dogpal;

import com.example.dogpal.models.Dog;
import com.example.dogpal.models.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EventJoinValidatorTest {

    @Test
    public void testAllDogsAllowed_NoLimit() {
        // Create a dog
        Dog dog = new Dog("Rocky", "Pitbull", 3, "Male", ""); // imageUrl can be empty
        List<Dog> selectedDogs = Arrays.asList(dog);

        // Create event with breed restriction OFF
        Event event = new Event();
        event.setBreedRestrictionEnabled(false);
        event.setLimitType("No Limit");
        event.setMaxParticipants(0);
        event.setMaxDogs(0);

        EventJoinValidator.ValidationResult result =
                EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);

        assertTrue(result.canJoin);
        assertEquals("", result.errorMessage);
    }

    @Test
    public void testDogBreedNotAllowed() {
        Dog dog = new Dog("Rocky", "Pitbull", 3, "Male", "");
        List<Dog> selectedDogs = Arrays.asList(dog);

        Event event = new Event();
        event.setBreedRestrictionEnabled(true);
        event.setAllowedBreeds(Arrays.asList("Labrador", "Golden Retriever"));
        event.setLimitType("No Limit");

        EventJoinValidator.ValidationResult result =
                EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);

        assertFalse(result.canJoin);
        assertTrue(result.errorMessage.contains("does not match the breed restrictions"));
    }

    @Test
    public void testMaxDogLimitReached() {
        Dog dog = new Dog("Rocky", "Labrador", 3, "Male", "");
        List<Dog> selectedDogs = Arrays.asList(dog);

        Event event = new Event();
        event.setBreedRestrictionEnabled(true);
        event.setAllowedBreeds(Arrays.asList("Labrador"));
        event.setLimitType("Limit by Dogs");
        event.setMaxDogs(2); // Max = 2

        int currentDogs = 2; // Already full

        EventJoinValidator.ValidationResult result =
                EventJoinValidator.validateJoin(event, selectedDogs, 0, currentDogs);

        assertFalse(result.canJoin);
        assertEquals("The event has reached the maximum number of dogs.", result.errorMessage);
    }

    @Test
    public void TC05_01_JoinWithoutSelectingAnyDogs() {
        Event event = new Event(true, Arrays.asList("All breeds allowed"), "No Limit", 10, 10);
        List<Dog> selectedDogs = new ArrayList<>();
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);
        assertFalse(result.canJoin);
        assertEquals("No dogs selected", result.reason);
    }

    @Test
    public void TC05_02_JoinWithNoDogsRegistered() {
        Event event = new Event(true, Arrays.asList("All breeds allowed"), "No Limit", 10, 10);
        List<Dog> selectedDogs = null;  // no dog list fetched
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);
        assertFalse(result.canJoin);
        assertEquals("No dogs selected", result.reason);
    }

    @Test
    public void TC05_03_JoinWith1Dog_NoRestrictions() {
        Event event = new Event(false, Arrays.asList("All breeds allowed"), "No Limit", 10, 10);
        List<Dog> selectedDogs = Arrays.asList(new Dog("Max", "Poodle"));
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);
        assertTrue(result.canJoin);
    }

    @Test
    public void TC05_04_JoinWithInvalidBreed() {
        Event event = new Event(true, Arrays.asList("Golden Retriever"), "No Limit", 10, 10);
        List<Dog> selectedDogs = Arrays.asList(new Dog("Husky", "Husky"));
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);
        assertFalse(result.canJoin);
        assertTrue(result.reason.contains("not allowed"));
    }

    @Test
    public void TC05_05_EventFullByOwners() {
        Event event = new Event(false, Arrays.asList("All breeds allowed"), "Limit by Dog Owners", 0, 10);
        List<Dog> selectedDogs = Arrays.asList(new Dog("Bella", "Poodle"));
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);
        assertFalse(result.canJoin);
        assertTrue(result.reason.contains("dog owners"));
    }

    @Test
    public void TC05_06_EventFullByDogs() {
        Event event = new Event(false, Arrays.asList("All breeds allowed"), "Limit by Dogs", 10, 0);
        List<Dog> selectedDogs = Arrays.asList(new Dog("Rex", "Poodle"));
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 5, 0);
        assertFalse(result.canJoin);
        assertTrue(result.reason.contains("dogs"));
    }

    @Test
    public void TC05_07_RoomForBothLimits() {
        Event event = new Event(false, Arrays.asList("All breeds allowed"), "Limit by Both", 5, 2);
        List<Dog> selectedDogs = Arrays.asList(new Dog("Luna", "Beagle"));
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 2, 1);
        assertTrue(result.canJoin);
    }

    @Test
    public void TC05_08_RoomForOwnerButDogLimitReached() {
        Event event = new Event(false, Arrays.asList("All breeds allowed"), "Limit by Both", 5, 1);
        List<Dog> selectedDogs = Arrays.asList(new Dog("Charlie", "Golden Retriever"));
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 3, 1);
        assertFalse(result.canJoin);
        assertTrue(result.reason.contains("dogs"));
    }

    @Test
    public void TC05_09_OneValidOneInvalidBreed() {
        Event event = new Event(true, Arrays.asList("Golden Retriever"), "No Limit", 10, 10);
        List<Dog> selectedDogs = Arrays.asList(
                new Dog("Buddy", "Golden Retriever"),
                new Dog("Milo", "Corgi")
        );
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 0, 0);
        assertFalse(result.canJoin);
        assertTrue(result.reason.contains("not allowed"));
    }

    @Test
    public void TC05_10_ValidDogs_OverDogLimit() {
        Event event = new Event(false, Arrays.asList("Poodle", "Labrador"), "Limit by Dogs", 10, 2);
        List<Dog> selectedDogs = Arrays.asList(
                new Dog("Leo", "Poodle"),
                new Dog("Coco", "Labrador"),
                new Dog("Max", "Poodle")
        );
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 3, 1);
        assertFalse(result.canJoin);
        assertTrue(result.reason.contains("dogs"));
    }

    @Test
    public void TC05_11_ValidDogs_WithinLimits() {
        Event event = new Event(false, Arrays.asList("Poodle", "Labrador"), "Limit by Both", 5, 3);
        List<Dog> selectedDogs = Arrays.asList(
                new Dog("Teddy", "Poodle"),
                new Dog("Daisy", "Labrador")
        );
        EventJoinValidator.ValidationResult result = EventJoinValidator.validateJoin(event, selectedDogs, 2, 1);
        assertTrue(result.canJoin);
    }

}
