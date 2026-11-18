package com.example.f1widget2.models;

import com.google.gson.annotations.SerializedName;

public class DriverStanding {
    @SerializedName("driver_number")
    private int driverNumber;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("name_acronym")
    private String nameAcronym;

    @SerializedName("team_name")
    private String teamName;

    @SerializedName("team_colour")
    private String teamColour;

    @SerializedName("points")
    private double points;

    @SerializedName("position")
    private int position;

    // Constructeur qui accepte un Driver
    public DriverStanding() {
    }

    public DriverStanding(Driver driver) {
        this.driverNumber = driver.getDriverNumber();
        this.fullName = driver.getFullName();
        this.nameAcronym = driver.getNameAcronym();
        this.teamName = driver.getTeamName();
        this.teamColour = driver.getTeamColour();

        // Extraire prénom et nom du nom complet
        String[] parts = this.fullName.split(" ", 2);
        if (parts.length > 0) {
            this.firstName = parts[0];
        }
        if (parts.length > 1) {
            this.lastName = parts[1];
        }
    }

    public int getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(int driverNumber) {
        this.driverNumber = driverNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        return firstName + " " + lastName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNameAcronym() {
        return nameAcronym;
    }

    public void setNameAcronym(String nameAcronym) {
        this.nameAcronym = nameAcronym;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamColour() {
        return teamColour;
    }

    public void setTeamColour(String teamColour) {
        this.teamColour = teamColour;
    }

    public double getPoints() {
        return points;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPoints(double points) {
        this.points = points;
    }
}

