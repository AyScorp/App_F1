package com.example.f1widget2.models;

import com.google.gson.annotations.SerializedName;

public class ChampionshipDriver {
    @SerializedName("driver_number")
    private int driverNumber;

    @SerializedName("position_current")
    private int positionCurrent;

    @SerializedName("points_current")
    private double pointsCurrent;

    public int getDriverNumber() {
        return driverNumber;
    }

    public int getPositionCurrent() {
        return positionCurrent;
    }

    public double getPointsCurrent() {
        return pointsCurrent;
    }
}

