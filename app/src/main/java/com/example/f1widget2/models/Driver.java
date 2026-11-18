package com.example.f1widget2.models;

import com.google.gson.annotations.SerializedName;

public class Driver {
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

    public int getDriverNumber() {
        return driverNumber;
    }

    public String getFullName() {
        return fullName;
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
}
