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
import com.example.f1widget2.models.ChampionshipTeam;
import com.example.f1widget2.models.ConstructorStanding;
import com.example.f1widget2.models.Driver;
import com.example.f1widget2.models.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class ConstructorActivity extends AppCompatActivity {
    private static final String TAG = "ConstructorActivity";

    private RecyclerView rvConstructors;
    private ConstructorStandingAdapter adapter;
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

        setContentView(R.layout.activity_constructor);

        rvConstructors = findViewById(R.id.rvConstructors);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        spinnerYear = findViewById(R.id.spinnerYear);

        rvConstructors.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConstructorStandingAdapter(new ArrayList<>());
        rvConstructors.setAdapter(adapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= availableYears.size()) {
                    return;
                }
                loadConstructorStandingsForYear(availableYears.get(position));
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
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(getString(R.string.loading_seasons));
        rvConstructors.setVisibility(View.GONE);

        F1ApiService apiService = RetrofitClient.getApiService();
        apiService.getSessions(null, null, null, null).enqueue(new Callback<List<Session>>() {
            @Override
            public void onResponse(Call<List<Session>> call, Response<List<Session>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    showError(getString(R.string.no_season_available));
                    return;
                }

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

                List<String> labels = new ArrayList<>();
                for (Integer year : availableYears) {
                    labels.add(getString(R.string.season_format, year));
                }

                ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                        ConstructorActivity.this,
                        R.layout.item_year_selected,
                        labels
                );
                yearAdapter.setDropDownViewResource(R.layout.item_year_dropdown);
                spinnerYear.setAdapter(yearAdapter);

                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int defaultIndex = availableYears.indexOf(currentYear);
                if (defaultIndex < 0) {
                    defaultIndex = 0;
                }
                spinnerYear.setSelection(defaultIndex);
            }

            @Override
            public void onFailure(Call<List<Session>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError(getString(R.string.standings_load_error, safeMessage(t)));
            }
        });
    }

    private void loadConstructorStandingsForYear(final int year) {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        rvConstructors.setVisibility(View.GONE);

        RetrofitClient.getApiService().getSessions(year, null, null, "Race")
                .enqueue(new Callback<List<Session>>() {
                    @Override
                    public void onResponse(Call<List<Session>> call, Response<List<Session>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            showError(getString(R.string.no_race_for_year, year));
                            return;
                        }

                        long now = System.currentTimeMillis();
                        List<Session> racedSessions = new ArrayList<>();
                        for (Session session : response.body()) {
                            long startMs = parseApiDateMillis(session.getDateStart());
                            if (session.getSessionKey() > 0 && startMs > 0 && startMs <= now) {
                                racedSessions.add(session);
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

                                        Map<String, String> teamColors = buildTeamColorMap(response.body());
                                        loadOfficialConstructors(sessionKey, teamColors);
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

    private void loadOfficialConstructors(int sessionKey, Map<String, String> teamColors) {
        RetrofitClient.getApiService().getChampionshipTeams(sessionKey)
                .enqueue(new Callback<List<ChampionshipTeam>>() {
                    @Override
                    public void onResponse(Call<List<ChampionshipTeam>> call, Response<List<ChampionshipTeam>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            showError(getString(R.string.no_constructor_championship_data));
                            return;
                        }

                        List<ConstructorStanding> standings = convertToConstructorStandings(response.body(), teamColors);
                        if (standings.isEmpty()) {
                            showError(getString(R.string.no_constructor_championship_data));
                            return;
                        }

                        rvConstructors.setVisibility(View.VISIBLE);
                        tvError.setVisibility(View.GONE);
                        adapter.updateConstructors(standings);
                    }

                    @Override
                    public void onFailure(Call<List<ChampionshipTeam>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Erreur classement constructeurs", t);
                        showError(getString(R.string.standings_load_error, safeMessage(t)));
                    }
                });
    }

    private List<ConstructorStanding> convertToConstructorStandings(List<ChampionshipTeam> teams, Map<String, String> teamColors) {
        List<ConstructorStanding> standings = new ArrayList<>();

        for (ChampionshipTeam team : teams) {
            String teamName = team.getTeamName() != null ? team.getTeamName() : "Equipe inconnue";
            String color = teamColors.getOrDefault(normalizeTeamName(teamName), "E10600");
            String logo = getTeamLogoName(teamName);

            standings.add(new ConstructorStanding(
                    team.getPositionCurrent(),
                    teamName,
                    team.getPointsCurrent(),
                    color,
                    logo
            ));
        }

        standings.sort(Comparator.comparingInt(ConstructorStanding::getPosition));
        return standings;
    }

    private Map<String, String> buildTeamColorMap(List<Driver> drivers) {
        Map<String, String> map = new HashMap<>();
        for (Driver driver : drivers) {
            if (driver.getTeamName() == null) {
                continue;
            }
            String key = normalizeTeamName(driver.getTeamName());
            if (!map.containsKey(key) && driver.getTeamColour() != null) {
                map.put(key, driver.getTeamColour());
            }
        }
        return map;
    }

    private String normalizeTeamName(String teamName) {
        if (teamName == null) {
            return "";
        }
        return teamName.toLowerCase(Locale.US).trim().replace(" ", "_").replace("-", "_");
    }

    private String getTeamLogoName(String teamName) {
        String normalized = normalizeTeamName(teamName);

        if (normalized.contains("rb_f1") || normalized.contains("racing_bulls") || normalized.contains("visa_cash_app")) {
            return "rb_f1_team";
        }
        if (normalized.contains("red_bull")) {
            return "red_bull_racing";
        }
        if (normalized.contains("haas")) {
            return "haas_f1_team";
        }
        if (normalized.contains("kick") || normalized.contains("sauber")) {
            return "kick_sauber";
        }
        if (normalized.contains("aston_martin")) {
            return "aston_martin";
        }
        if (normalized.contains("williams")) {
            return "williams";
        }
        if (normalized.contains("mclaren")) {
            return "mclaren";
        }
        if (normalized.contains("ferrari")) {
            return "ferrari";
        }
        if (normalized.contains("mercedes")) {
            return "mercedes";
        }
        if (normalized.contains("alpine")) {
            return "alpine";
        }
        if (normalized.contains("audi")) {
            return "audi";
        }
        if (normalized.contains("cadillac")) {
            return "cadillac";
        }

        return "default_team";
    }

    private long parseApiDateMillis(String date) {
        if (date == null || date.isEmpty()) {
            return 0L;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            java.util.Date parsed = format.parse(date);
            return parsed != null ? parsed.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        rvConstructors.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String safeMessage(Throwable t) {
        return (t != null && t.getMessage() != null && !t.getMessage().isEmpty())
                ? t.getMessage()
                : getString(R.string.unknown_error);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

