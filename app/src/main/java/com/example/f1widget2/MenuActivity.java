package com.example.f1widget2;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_menu);

        MaterialCardView cardLastRace = findViewById(R.id.cardLastRace);
        MaterialCardView cardStandings = findViewById(R.id.cardStandings);
        MaterialCardView cardCalendar = findViewById(R.id.cardCalendar);

        cardLastRace.setOnClickListener(v -> openWithTransition(MainActivity.class));

        cardStandings.setOnClickListener(v -> openWithTransition(DriverActivity.class));

        cardCalendar.setOnClickListener(v -> openWithTransition(ConstructorActivity.class));
    }

    private void openWithTransition(Class<?> destination) {
        startActivity(new Intent(MenuActivity.this, destination));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
