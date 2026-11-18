package com.example.f1widget2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.f1widget2.models.ConstructorStanding;

import java.util.List;
import java.util.Locale;

public class ConstructorStandingAdapter extends RecyclerView.Adapter<ConstructorStandingAdapter.ViewHolder> {
    private List<ConstructorStanding> constructors;

    public ConstructorStandingAdapter(List<ConstructorStanding> constructors) {
        this.constructors = constructors;
    }

    public void updateConstructors(List<ConstructorStanding> newConstructors) {
        this.constructors = newConstructors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_constructor_standing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstructorStanding constructor = constructors.get(position);

        holder.tvPosition.setText(String.valueOf(constructor.getPosition()));

        String teamName = constructor.getTeamName() != null ? constructor.getTeamName() : "Equipe inconnue";
        String[] parts = teamName.split(" ", 2);
        holder.tvFirstLine.setText(parts.length > 0 ? parts[0] : "Equipe");
        holder.tvSecondLine.setText(parts.length > 1 ? parts[1].toUpperCase(Locale.getDefault()) : teamName.toUpperCase(Locale.getDefault()));

        holder.tvTagTeam.setText(teamName);
        holder.tvTagType.setText(holder.itemView.getContext().getString(R.string.constructor));
        holder.tvPoints.setText(String.format(Locale.getDefault(), "%.0f pts", constructor.getPoints()));

        String logoName = constructor.getTeamLogo() != null ? constructor.getTeamLogo() : "default_team";
        int logoRes = holder.itemView.getContext().getResources()
                .getIdentifier(logoName, "drawable", holder.itemView.getContext().getPackageName());
        holder.imgTeam.setImageResource(logoRes != 0 ? logoRes : R.drawable.carhome);

        String teamColour = constructor.getTeamColour();
        if (teamColour != null && !teamColour.isEmpty()) {
            try {
                String colorString = teamColour.startsWith("#") ? teamColour : "#" + teamColour;
                holder.teamColorBar.setBackgroundColor(Color.parseColor(colorString));
            } catch (IllegalArgumentException e) {
                holder.teamColorBar.setBackgroundColor(Color.parseColor("#E10600"));
            }
        } else {
            holder.teamColorBar.setBackgroundColor(Color.parseColor("#E10600"));
        }
    }

    @Override
    public int getItemCount() {
        return constructors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition;
        TextView tvFirstLine;
        TextView tvSecondLine;
        TextView tvTagTeam;
        TextView tvTagType;
        TextView tvPoints;
        ImageView imgTeam;
        View teamColorBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvFirstLine = itemView.findViewById(R.id.tvFirstLine);
            tvSecondLine = itemView.findViewById(R.id.tvSecondLine);
            tvTagTeam = itemView.findViewById(R.id.tvTagTeam);
            tvTagType = itemView.findViewById(R.id.tvTagType);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            imgTeam = itemView.findViewById(R.id.imgTeamPhoto);
            teamColorBar = itemView.findViewById(R.id.teamColorBar);
        }
    }
}

