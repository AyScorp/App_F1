package com.example.f1widget2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.f1widget2.models.DriverStanding;

import java.util.List;
import java.util.Locale;

public class DriverStandingAdapter extends RecyclerView.Adapter<DriverStandingAdapter.ViewHolder> {
    private List<DriverStanding> drivers;

    public DriverStandingAdapter(List<DriverStanding> drivers) {
        this.drivers = drivers;
    }

    public void updateDrivers(List<DriverStanding> newDrivers) {
        this.drivers = newDrivers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_standing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DriverStanding driver = drivers.get(position);

        // Position en haut à gauche
        holder.tvPosition.setText(String.valueOf(driver.getPosition()));

        // Prénom et Nom séparés
        if (driver.getFirstName() != null && !driver.getFirstName().isEmpty()) {
            holder.tvFirstName.setText(driver.getFirstName());
        } else {
            holder.tvFirstName.setVisibility(View.GONE);
        }

        if (driver.getLastName() != null && !driver.getLastName().isEmpty()) {
            holder.tvLastName.setText(driver.getLastName().toUpperCase());
        } else {
            holder.tvLastName.setText(driver.getFullName().toUpperCase());
        }

        // Tags en bas
        holder.tvPoints.setText(String.format(Locale.getDefault(), "%.0f pts", driver.getPoints()));
        holder.tvTeam.setText(driver.getTeamName());
        holder.tvDriverNumber.setText(String.format(Locale.getDefault(), "#%d", driver.getDriverNumber()));

        // Couleur de l'équipe
        if (driver.getTeamColour() != null && !driver.getTeamColour().isEmpty()) {
            try {
                String colorString = driver.getTeamColour();
                if (!colorString.startsWith("#")) {
                    colorString = "#" + colorString;
                }
                int teamColor = Color.parseColor(colorString);
                holder.teamColorBar.setBackgroundColor(teamColor);
            } catch (IllegalArgumentException e) {
                holder.teamColorBar.setBackgroundColor(Color.parseColor("#E10600")); // Rouge F1 par défaut
            }
        } else {
            holder.teamColorBar.setBackgroundColor(Color.parseColor("#E10600"));
        }
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition;
        TextView tvFirstName;
        TextView tvLastName;
        TextView tvPoints;
        TextView tvTeam;
        TextView tvDriverNumber;
        ImageView imgDriverPhoto;
        View teamColorBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvFirstName = itemView.findViewById(R.id.tvFirstName);
            tvLastName = itemView.findViewById(R.id.tvLastName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvTeam = itemView.findViewById(R.id.tvTeam);
            tvDriverNumber = itemView.findViewById(R.id.tvDriverNumber);
            imgDriverPhoto = itemView.findViewById(R.id.imgDriverPhoto);
            teamColorBar = itemView.findViewById(R.id.teamColorBar);
        }
    }
}

