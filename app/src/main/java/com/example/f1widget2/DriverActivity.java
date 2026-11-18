package com.example.f1widget2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.f1widget2.api.F1ApiService;
import com.example.f1widget2.api.RetrofitClient;
import com.example.f1widget2.models.ChampionshipDriver;
import com.example.f1widget2.models.Driver;
import com.example.f1widget2.models.DriverStanding;
import com.example.f1widget2.models.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivity extends AppCompatActivity {
    private static final String TAG = "DriverActivity";
    private RecyclerView rvDrivers;
    private DriverStandingAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvError;
    private Spinner spinnerYear;
    private final List<Integer> availableYears = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_driver);

        rvDrivers = findViewById(R.id.rvDrivers);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        spinnerYear = findViewById(R.id.spinnerYear);

        rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DriverStandingAdapter(new ArrayList<>());
        rvDrivers.setAdapter(adapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= availableYears.size()) {
                    return;
                }
                int selectedYear = availableYears.get(position);
                loadDriverStandingsForYear(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });

        loadAvailableYears();
    }

    private void loadAvailableYears() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        rvDrivers.setVisibility(View.GONE);
        tvError.setText(getString(R.string.loading_seasons));
        tvError.setVisibility(View.VISIBLE);

        F1ApiService apiService = RetrofitClient.getApiService();

        apiService.getSessions(null, null, null, null).enqueue(new Callback<List<Session>>() {
            @Override
            public void onResponse(Call<List<Session>> call, Response<List<Session>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> yearsSet = new HashSet<>();
                    for (Session session : response.body()) {
                        if (session.getYear() > 0) {
                            yearsSet.add(session.getYear());
                        }
                    }

                    if (yearsSet.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        showError(getString(R.string.no_season_available));
                        return;
                    }

                    availableYears.clear();
                    availableYears.addAll(yearsSet);
                    Collections.sort(availableYears, Collections.reverseOrder());

                    List<String> yearLabels = new ArrayList<>();
                    for (Integer year : availableYears) {
                        yearLabels.add(getString(R.string.season_format, year));
                    }

                    ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                            DriverActivity.this,
                            R.layout.item_year_selected,
                            yearLabels
                    );
                    yearAdapter.setDropDownViewResource(R.layout.item_year_dropdown);
                    spinnerYear.setAdapter(yearAdapter);

                    int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                    int defaultIndex = availableYears.indexOf(currentYear);
                    if (defaultIndex < 0) {
                        defaultIndex = 0;
                    }
                    spinnerYear.setSelection(defaultIndex);

                } else {
                    progressBar.setVisibility(View.GONE);
                    showError(getString(R.string.no_season_available));
                }
            }

            @Override
            public void onFailure(Call<List<Session>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Erreur chargement saisons", t);
                showError(getString(R.string.standings_load_error, safeMessage(t)));
            }
        });
    }

    private void loadDriverStandingsForYear(final int year) {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        rvDrivers.setVisibility(View.GONE);

        F1ApiService apiService = RetrofitClient.getApiService();
        apiService.getSessions(year, null, null, "Race").enqueue(new Callback<List<Session>>() {
            @Override
            public void onResponse(Call<List<Session>> call, Response<List<Session>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    showError(getString(R.string.no_race_for_year, year));
                    return;
                }

                long now = System.currentTimeMillis();
                List<Session> racedSessions = new ArrayList<>();
                for (Session s : response.body()) {
                    long startMs = parseApiDateMillis(s.getDateStart());
                    if (s.getSessionKey() > 0 && startMs > 0 && startMs <= now) {
                        racedSessions.add(s);
                    }
                }

                if (racedSessions.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    showError(getString(R.string.no_race_for_year, year));
                    return;
                }

                racedSessions.sort(Comparator.comparingLong(s -> parseApiDateMillis(s.getDateStart())));
                final Session latestSession = racedSessions.get(racedSessions.size() - 1);
                final int sessionKey = latestSession.getSessionKey();

                RetrofitClient.getApiService().getDrivers(sessionKey)
                        .enqueue(new Callback<List<Driver>>() {
                            @Override
                            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                                    progressBar.setVisibility(View.GONE);
                                    showError(getString(R.string.standings_load_error, "drivers indisponibles"));
                                    return;
                                }

                                List<Driver> drivers = response.body();
                                loadOfficialChampionship(sessionKey, drivers);
                            }

                            @Override
                            public void onFailure(Call<List<Driver>> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                showError(getString(R.string.standings_load_error, safeMessage(t)));
                            }
                        });
            }

            @Override
            public void onFailure(Call<List<Session>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError(getString(R.string.standings_load_error, safeMessage(t)));
            }
        });
    }

    private void loadOfficialChampionship(int sessionKey, List<Driver> drivers) {
        RetrofitClient.getApiService().getChampionshipDrivers(sessionKey)
                .enqueue(new Callback<List<ChampionshipDriver>>() {
                    @Override
                    public void onResponse(Call<List<ChampionshipDriver>> call, Response<List<ChampionshipDriver>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            showError(getString(R.string.no_championship_data));
                            return;
                        }

                        List<DriverStanding> standings = convertOfficialStandings(response.body(), drivers);
                        if (standings.isEmpty()) {
                            showError(getString(R.string.no_championship_data));
                            return;
                        }

                        rvDrivers.setVisibility(View.VISIBLE);
                        tvError.setVisibility(View.GONE);
                        adapter.updateDrivers(standings);
                    }

                    @Override
                    public void onFailure(Call<List<ChampionshipDriver>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showError(getString(R.string.standings_load_error, safeMessage(t)));
                    }
                });
    }

    private long parseApiDateMillis(String date) {
        if (date == null || date.isEmpty()) {
            return 0L;
        }
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            java.util.Date parsed = format.parse(date);
            return parsed != null ? parsed.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private List<DriverStanding> convertOfficialStandings(List<ChampionshipDriver> championshipDrivers, List<Driver> drivers) {
        List<DriverStanding> standings = new ArrayList<>();
        Map<Integer, Driver> driversByNumber = new HashMap<>();

        for (Driver driver : drivers) {
            driversByNumber.put(driver.getDriverNumber(), driver);
        }

        for (ChampionshipDriver championship : championshipDrivers) {
            Driver matchedDriver = driversByNumber.get(championship.getDriverNumber());
            DriverStanding standing = matchedDriver != null
                    ? new DriverStanding(matchedDriver)
                    : buildFallbackStanding(championship.getDriverNumber());

            standing.setDriverNumber(championship.getDriverNumber());
            standing.setPoints(championship.getPointsCurrent());
            standing.setPosition(championship.getPositionCurrent());
            standings.add(standing);
        }

        standings.sort(Comparator.comparingInt(DriverStanding::getPosition));
        return standings;
    }

    private DriverStanding buildFallbackStanding(int driverNumber) {
        DriverStanding fallback = new DriverStanding();
        fallback.setDriverNumber(driverNumber);
        fallback.setFirstName("Pilote");
        fallback.setLastName(String.valueOf(driverNumber));
        fallback.setFullName("Pilote " + driverNumber);
        fallback.setNameAcronym("NA");
        fallback.setTeamName("Equipe inconnue");
        fallback.setTeamColour("E10600");
        return fallback;
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        rvDrivers.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String safeMessage(Throwable t) {
        return (t != null && t.getMessage() != null && !t.getMessage().isEmpty())
                ? t.getMessage()
                : "inconnue";
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

