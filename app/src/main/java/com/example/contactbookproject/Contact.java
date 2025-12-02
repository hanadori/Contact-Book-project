package com.example.contactbookproject;

import java.io.Serializable;

public class Contact implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String gender; // "Male" or "Female"
    private String birthday; // e.g., "01/01/2000"

    // Default constructor is required for Firebase
    public Contact() {
    }

    public Contact(String id, String name, String phone, String email, String gender, String birthday) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}