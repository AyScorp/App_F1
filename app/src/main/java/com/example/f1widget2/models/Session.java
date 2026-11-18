package com.example.f1widget2.models;

import com.google.gson.annotations.SerializedName;

public class Session {
    @SerializedName("session_key")
    private int sessionKey;

    @SerializedName("session_name")
    private String sessionName;

    @SerializedName("date_start")
    private String dateStart;

    @SerializedName("date_end")
    private String dateEnd;

    @SerializedName("country_name")
    private String countryName;

    @SerializedName("circuit_short_name")
    private String circuitShortName;

    @SerializedName("session_type")
    private String sessionType;

    @SerializedName("year")
    private int year;

    public int getSessionKey() {
        return sessionKey;
    }


    public String getSessionName() {
        return sessionName;
    }

    public String getDateStart() {
        return dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCircuitShortName() {
        return circuitShortName;
    }

    public String getSessionType() {
        return sessionType;
    }

    public int getYear() {
        return year;
    }
}
