package com.example.dogpal.models;

import android.net.Uri;

public class Dog {
    private String dogId;
    private String name;
    private String breed;
    int age;
    private String gender;
    private String imageUrl;
    // This will hold the Uri for the dog image temporarily before uploading
    private String imageUri;


    public Dog(){

    }
    public Dog(String name, String breed) {
        this.name = name;
        this.breed = breed;
    }

    private String vaccineImageUrl;

    public String getVaccineImageUrl() {
        return vaccineImageUrl;
    }

    public void setVaccineImageUrl(String vaccineImageUrl) {
        this.vaccineImageUrl = vaccineImageUrl;
    }

    public Dog(String name, String breed, int age, String gender, String imageUrl) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getDogId() { return dogId; }
    public String getName() { return name; }
    public String getBreed() { return breed; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getImageUrl() { return imageUrl; }
    public String getImageUri() { return imageUri; }
    // Setters
    public void setDogId(String dogId) { this.dogId = dogId; }
    public void setName(String name) { this.name = name; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
