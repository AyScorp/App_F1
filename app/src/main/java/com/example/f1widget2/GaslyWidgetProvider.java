package com.example.f1widget2;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.core.content.res.ResourcesCompat;

import com.example.f1widget2.api.F1ApiService;
import com.example.f1widget2.api.RetrofitClient;
import com.example.f1widget2.models.Driver;
import com.example.f1widget2.models.Position;
import com.example.f1widget2.models.Session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class GaslyWidgetProvider extends AppWidgetProvider {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private Bitmap createTextBitmap(Context context, String text, int textSize, int textColor, int fontResId) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        Typeface typeface = ResourcesCompat.getFont(context, fontResId);
        paint.setTypeface(typeface);
        paint.setShadowLayer(4, 2, 2, Color.BLACK);

        float width = paint.measureText(text);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float height = fm.descent - fm.ascent;

        Bitmap bitmap = Bitmap.createBitmap((int) Math.ceil(width), (int) Math.ceil(height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, 0, -fm.ascent, paint);

        return bitmap;
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.gasly_widget);

        executor.execute(() -> {
            try {
                F1ApiService apiService = RetrofitClient.getApiService();
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                // Essaye d’abord de récupérer les courses en cours/déjà démarrées, sinon fallback sans filtre.
                Response<List<Session>> response = apiService.getSessions(currentYear, null, null, "Race").execute();
                if ((!response.isSuccessful() || response.body() == null || response.body().isEmpty())) {
                    response = apiService.getSessions(currentYear, null, null, null).execute();
                }

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Session lastSession = pickLatestStartedSession(response.body());
                    if (lastSession == null) {
                        postTextOnly(context, appWidgetManager, appWidgetId, views, "Pas de session active");
                        return;
                    }

                    Response<List<Driver>> driversResponse = apiService.getDrivers(lastSession.getSessionKey()).execute();
                    Driver gaslyDriver = null;

                    if (driversResponse.isSuccessful() && driversResponse.body() != null) {
                        for (Driver driver : driversResponse.body()) {
                            String fullName = driver.getFullName();
                            if (fullName != null && (fullName.toLowerCase().contains("gasly") ||
                                    "pierre gasly".equalsIgnoreCase(fullName))) {
                                gaslyDriver = driver;
                                break;
                            }
                        }
                    }

                    String finalPosition = "P-";
                    if (gaslyDriver != null) {
                        Response<List<Position>> positionsResponse = apiService.getPositions(lastSession.getSessionKey()).execute();

                        if (positionsResponse.isSuccessful() && positionsResponse.body() != null) {
                            List<Position> allPositions = positionsResponse.body();
                            Position lastPosition = null;

                            for (Position position : allPositions) {
                                if (position.getDriverNumber() == gaslyDriver.getDriverNumber()) {
                                    if (lastPosition == null || position.getDate().compareTo(lastPosition.getDate()) > 0) {
                                        lastPosition = position;
                                    }
                                }
                            }

                            if (lastPosition != null && lastPosition.getPosition() > 0) {
                                finalPosition = "P" + lastPosition.getPosition();
                            }
                        }
                    }

                    String displayPosition = finalPosition;
                    String circuitName = lastSession.getCircuitShortName();
                    String sessionName = lastSession.getSessionName();

                    mainHandler.post(() -> {
                        Bitmap circuitBitmap = createTextBitmap(context, circuitName != null ? circuitName : "-", 48, Color.WHITE, R.font.formula1_bold_web_0);
                        Bitmap sessionBitmap = createTextBitmap(context, sessionName != null ? sessionName : "-", 32, 0xFFAAAAAA, R.font.formula1_regular_web_0);
                        Bitmap positionBitmap = createTextBitmap(context, displayPosition, 140, 0xFFFF6B9D, R.font.formula1_bold_web_0);
                        Bitmap firstNameBitmap = createTextBitmap(context, "Pierre", 42, Color.WHITE, R.font.formula1_regular_web_0);
                        Bitmap lastNameBitmap = createTextBitmap(context, "GASLY", 68, Color.WHITE, R.font.formula1_bold_web_0);

                        views.setImageViewBitmap(R.id.tvCircuit, circuitBitmap);
                        views.setImageViewBitmap(R.id.tvSession, sessionBitmap);
                        views.setImageViewBitmap(R.id.tvPosition, positionBitmap);
                        views.setImageViewBitmap(R.id.tvFirstName, firstNameBitmap);
                        views.setImageViewBitmap(R.id.tvLastName, lastNameBitmap);

                        int circuitDrawableId = getCircuitDrawableId(context, circuitName);
                        views.setImageViewResource(R.id.ivCircuit, circuitDrawableId);

                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    });
                } else {
                    postTextOnly(context, appWidgetManager, appWidgetId, views, "Pas de données");
                }
            } catch (Exception e) {
                e.printStackTrace();
                postTextOnly(context, appWidgetManager, appWidgetId, views, "Erreur API");
            }
        });
    }

    private Session pickLatestStartedSession(List<Session> sessions) {
        if (sessions == null || sessions.isEmpty()) return null;
        long now = System.currentTimeMillis();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);

        // Trie par date_start descendante, garde celles déjà commencées
        Collections.sort(sessions, new Comparator<Session>() {
            @Override
            public int compare(Session s1, Session s2) {
                long t1 = parseDate(fmt, s1.getDateStart());
                long t2 = parseDate(fmt, s2.getDateStart());
                return Long.compare(t2, t1);
            }
        });

        for (Session s : sessions) {
            if (parseDate(fmt, s.getDateStart()) <= now) {
                return s;
            }
        }
        return sessions.get(0); // fallback : prochaine session
    }

    private long parseDate(SimpleDateFormat fmt, String date) {
        if (date == null) return 0L;
        try {
            return fmt.parse(date).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    private void postTextOnly(Context context, AppWidgetManager manager, int appWidgetId, RemoteViews views, String text) {
        mainHandler.post(() -> {
            Bitmap placeholder = createTextBitmap(context, text, 48, Color.WHITE, R.font.formula1_regular_web_0);
            views.setImageViewBitmap(R.id.tvCircuit, placeholder);
            views.setImageViewBitmap(R.id.tvSession, null);
            views.setImageViewBitmap(R.id.tvPosition, null);
            views.setImageViewBitmap(R.id.tvFirstName, null);
            views.setImageViewBitmap(R.id.tvLastName, null);
            views.setImageViewResource(R.id.ivCircuit, R.drawable.interlagos);
            manager.updateAppWidget(appWidgetId, views);
        });
    }

    private int getCircuitDrawableId(Context context, String circuitName) {
        if (circuitName == null) return R.drawable.interlagos;

        String resourceName = circuitName.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replaceAll("[^a-z0-9_]", "");

        int resourceId = context.getResources().getIdentifier(
                resourceName,
                "drawable",
                context.getPackageName()
        );

        return resourceId != 0 ? resourceId : R.drawable.interlagos;
    }
}
