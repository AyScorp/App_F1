// Java
package com.example.f1widget2.api;

import com.example.f1widget2.models.Driver;
import com.example.f1widget2.models.ChampionshipDriver;
import com.example.f1widget2.models.ChampionshipTeam;
import com.example.f1widget2.models.Position;
import com.example.f1widget2.models.Session;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface F1ApiService {
    @GET("sessions")
    Call<List<Session>> getSessions(
            @Query("year") Integer year,
            @Query("location") String location,
            @Query("session_name") String sessionName,
            @Query("session_type") String sessionType
    );

    // Méthode pratique si vous utilisez uniquement l'année
    @GET("sessions")
    Call<List<Session>> getSessionsByYear(@Query("year") Integer year);

    @GET("drivers")
    Call<List<Driver>> getDrivers(@Query("session_key") Integer sessionKey);

    @GET("position")
    Call<List<Position>> getPositions(@Query("session_key") Integer sessionKey);

    @GET("championship_drivers")
    Call<List<ChampionshipDriver>> getChampionshipDrivers(@Query("session_key") Integer sessionKey);

    @GET("championship_teams")
    Call<List<ChampionshipTeam>> getChampionshipTeams(@Query("session_key") Integer sessionKey);
}
