package com.example.f1widget2.models;

public class ConstructorStanding {
    private int position;
    private String teamName;
    private double points;
    private String teamColour;
    private String teamLogo;

    public ConstructorStanding(int position, String teamName, double points, String teamColour, String teamLogo) {
        this.position = position;
        this.teamName = teamName;
        this.points = points;
        this.teamColour = teamColour;
        this.teamLogo = teamLogo;
    }

    public int getPosition() {
        return position;
    }

    public String getTeamName() {
        return teamName;
    }

    public double getPoints() {
        return points;
    }

    public String getTeamColour() {
        return teamColour;
    }

    public String getTeamLogo() {
        return teamLogo;
    }
}

