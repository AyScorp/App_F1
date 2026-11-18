package com.example.f1widget2.models;

public class DriverResult {
    private int position;
    private String driverName;
    private String driverAcronym;
    private String teamName;
    private String teamLogo;

    public DriverResult(int position, String driverName, String driverAcronym, String teamName, String teamLogo) {
        this.position = position;
        this.driverName = driverName;
        this.driverAcronym = driverAcronym;
        this.teamName = teamName;
        this.teamLogo = teamLogo;
    }

    public int getPosition() {
        return position;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverAcronym() {
        return driverAcronym;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamLogo() {
        return teamLogo;
    }
}
