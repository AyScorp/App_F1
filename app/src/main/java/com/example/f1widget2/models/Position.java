package com.example.f1widget2.models;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("position")
    private int position;

    @SerializedName("driver_number")
    private int driverNumber;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("name_acronym")
    private String nameAcronym;

    @SerializedName("team_name")
    private String teamName;

    @SerializedName("team_colour")
    private String teamColour;

    @SerializedName("date")
    private String date;

    // Getters
    public int getPosition() {
        return position;
    }

    public int getDriverNumber() {
        return driverNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLastName() {
        // Si fullName existe, extraire le nom de famille
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            if (parts.length > 1) {
                return parts[parts.length - 1];
            }
            return fullName;
        }
        return null;
    }

    public String getNameAcronym() {
        return nameAcronym;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamColour() {
        return teamColour;
    }

    public String getDate() {
        return date;
    }
}
