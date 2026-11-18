package com.example.f1widget2.models;

import com.google.gson.annotations.SerializedName;

public class ChampionshipTeam {
    @SerializedName(value = "team_name", alternate = {"constructor_name", "name"})
    private String teamName;

    @SerializedName(value = "position_current", alternate = {"position"})
    private int positionCurrent;

    @SerializedName(value = "points_current", alternate = {"points"})
    private double pointsCurrent;

    public String getTeamName() {
        return teamName;
    }

    public int getPositionCurrent() {
        return positionCurrent;
    }

    public double getPointsCurrent() {
        return pointsCurrent;
    }
}

