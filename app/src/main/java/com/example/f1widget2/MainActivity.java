package com.example.f1widget2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.f1widget2.api.RetrofitClient;
import com.example.f1widget2.models.Driver;
import com.example.f1widget2.models.DriverResult;
import com.example.f1widget2.models.Position;
import com.example.f1widget2.models.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_RESULTS = "f1_results_cache";
    private static final String KEY_CACHE_TITLE = "cache_title";
    private static final String KEY_CACHE_RESULTS = "cache_results";
    private TextView tvSessionTitle;
    private RecyclerView rvResults;
    private DriverResultAdapter adapter;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        tvSessionTitle = findViewById(R.id.tvSessionTitle);
        rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        showCachedResultIfAny();
        loadLatestRace();
    }

    private void showCachedResultIfAny() {
        CachedResult cached = readCachedResult();
        if (cached == null || cached.results == null || cached.results.isEmpty()) {
            return;
        }

        tvSessionTitle.setText(getString(R.string.cached_result_title, cached.title));
        adapter = new DriverResultAdapter(this, cached.results);
        rvResults.setAdapter(adapter);
    }

    private void loadLatestRace() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        RetrofitClient.getApiService().getSessions(currentYear, null, null, "Race")
                .enqueue(new Callback<List<Session>>() {
                    @Override public void onResponse(Call<List<Session>> call, Response<List<Session>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Session> sessions = response.body();
                            // Parse date_start, garder celles qui ont commencé (date_start <= now)
                            long now = System.currentTimeMillis();
                            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
                            Session latest = sessions.stream()
                                    .filter(s -> "Race".equalsIgnoreCase(s.getSessionType()))
                                    .filter(s -> {
                                        try { return fmt.parse(s.getDateStart()).getTime() <= now; }
                                        catch (Exception e) { return false; }
                                    })
                                    .max(Comparator.comparing(s -> {
                                        try { return fmt.parse(s.getDateStart()).getTime(); }
                                        catch (Exception e) { return 0L; }
                                    }))
                                    .orElse(null);

                            if (latest != null) {
                                Log.d(TAG, "Session trouvée: " + latest.getSessionName() + " - Key: " + latest.getSessionKey());
                                loadPositions(latest);
                            } else {
                                showOfflineWithCache(getString(R.string.no_race_started_for_year, currentYear));
                            }
                        } else {
                            showOfflineWithCache(getString(R.string.no_session_found_code, response.code()));
                        }
                    }
                    @Override public void onFailure(Call<List<Session>> call, Throwable t) {
                        showOfflineWithCache(getString(R.string.error_sessions, safeMessage(t)));
                    }
                });
    }

    private void loadPositions(final Session session) {
        int sessionKey = session.getSessionKey();

        RetrofitClient.getApiService().getDrivers(sessionKey).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final List<Driver> drivers = response.body();
                    Log.d(TAG, "Nombre de pilotes: " + drivers.size());

                    if (drivers.isEmpty()) {
                        showOfflineWithCache(getString(R.string.no_drivers_for_session));
                        return;
                    }

                    RetrofitClient.getApiService().getPositions(sessionKey).enqueue(new Callback<List<Position>>() {
                        @Override
                        public void onResponse(Call<List<Position>> call, Response<List<Position>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.d(TAG, "Nombre de positions brutes: " + response.body().size());
                                displayResults(response.body(), drivers, session);
                            } else {
                                showOfflineWithCache(getString(R.string.no_position_found));
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Position>> call, Throwable t) {
                            showOfflineWithCache(getString(R.string.error_positions, safeMessage(t)));
                            Log.e(TAG, "Erreur positions", t);
                        }
                    });
                } else {
                    showOfflineWithCache(getString(R.string.no_driver_found_code, response.code()));
                    Log.e(TAG, "Erreur HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                showOfflineWithCache(getString(R.string.error_loading_drivers, safeMessage(t)));
                Log.e(TAG, "Erreur drivers", t);
            }
        });
    }

    private void displayResults(List<Position> positions, List<Driver> drivers, Session session) {
        java.util.Map<Integer, Position> latestPositions = new java.util.HashMap<>();

        for (Position pos : positions) {
            Integer driverNum = pos.getDriverNumber();
            if (!latestPositions.containsKey(driverNum) ||
                    pos.getDate().compareTo(latestPositions.get(driverNum).getDate()) > 0) {
                latestPositions.put(driverNum, pos);
            }
        }

        List<Position> finalPositions = new ArrayList<>(latestPositions.values());
        Collections.sort(finalPositions, new Comparator<Position>() {
            @Override
            public int compare(Position p1, Position p2) {
                return Integer.compare(p1.getPosition(), p2.getPosition());
            }
        });

        String year = session.getDateStart() != null && session.getDateStart().length() >= 4
                ? session.getDateStart().substring(0, 4)
                : "?";

        String title = session.getSessionName() + "\n" +
                session.getCircuitShortName() + " " + year;
        tvSessionTitle.setText(title);

        List<DriverResult> driverResults = new ArrayList<>();
        for (Position pos : finalPositions) {
            Driver driver = findDriverByNumber(drivers, pos.getDriverNumber());
            if (driver != null) {
                String teamLogo = getTeamLogoName(driver.getTeamName());
                driverResults.add(new DriverResult(
                        pos.getPosition(),
                        driver.getFullName(),
                        driver.getNameAcronym(),
                        driver.getTeamName(),
                        teamLogo
                ));
            }
        }

        saveLastResultToCache(title, driverResults);
        adapter = new DriverResultAdapter(this, driverResults);
        rvResults.setAdapter(adapter);
    }

    private void saveLastResultToCache(String title, List<DriverResult> results) {
        SharedPreferences prefs = getSharedPreferences(PREFS_RESULTS, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_CACHE_TITLE, title)
                .putString(KEY_CACHE_RESULTS, gson.toJson(results))
                .apply();
    }

    private CachedResult readCachedResult() {
        SharedPreferences prefs = getSharedPreferences(PREFS_RESULTS, MODE_PRIVATE);
        String title = prefs.getString(KEY_CACHE_TITLE, null);
        String json = prefs.getString(KEY_CACHE_RESULTS, null);

        if (title == null || json == null || json.isEmpty()) {
            return null;
        }

        Type listType = new TypeToken<List<DriverResult>>() {}.getType();
        List<DriverResult> cachedResults = gson.fromJson(json, listType);
        if (cachedResults == null || cachedResults.isEmpty()) {
            return null;
        }
        return new CachedResult(title, cachedResults);
    }

    private void showOfflineWithCache(String reason) {
        CachedResult cached = readCachedResult();
        if (cached != null) {
            tvSessionTitle.setText(getString(R.string.offline_using_cache_title, cached.title));
            adapter = new DriverResultAdapter(this, cached.results);
            rvResults.setAdapter(adapter);
            Toast.makeText(this, getString(R.string.offline_using_cache_toast), Toast.LENGTH_LONG).show();
            Log.w(TAG, reason);
            return;
        }
        showError(reason);
    }

    private String safeMessage(Throwable t) {
        return (t != null && t.getMessage() != null && !t.getMessage().isEmpty())
                ? t.getMessage()
                : getString(R.string.unknown_error);
    }

    private static class CachedResult {
        final String title;
        final List<DriverResult> results;

        CachedResult(String title, List<DriverResult> results) {
            this.title = title;
            this.results = results;
        }
    }

    private Driver findDriverByNumber(List<Driver> drivers, int driverNumber) {
        for (Driver driver : drivers) {
            if (driver.getDriverNumber() == driverNumber) {
                return driver;
            }
        }
        return null;
    }

    private String getTeamLogoName(String teamName) {
        if (teamName == null) return "default_team";

        String normalized = teamName.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_");

        // Gestion des cas spéciaux
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
        if (normalized.contains("audi"))    {
            return "audi";
        }
        if (normalized.contains("cadillac")) {
            return "cadillac";
        }

        return "default_team";
    }


    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        tvSessionTitle.setText(message);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
