package com.example.f1widget2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.f1widget2.models.DriverResult;
import java.util.List;

public class DriverResultAdapter extends RecyclerView.Adapter<DriverResultAdapter.ViewHolder> {
    private Context context;
    private List<DriverResult> results;

    public DriverResultAdapter(Context context, List<DriverResult> results) {
        this.context = context;
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_driver_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DriverResult result = results.get(position);

        holder.tvPosition.setText(String.valueOf(result.getPosition()));

        // Séparer le nom complet en prénom et nom
        String fullName = result.getDriverName();
        String[] nameParts = fullName.split(" ", 2);

        if (nameParts.length == 2) {
            holder.tvDriverFirstName.setText(nameParts[0]);
            holder.tvDriverLastName.setText(nameParts[1].toUpperCase());
        } else {
            holder.tvDriverFirstName.setText("");
            holder.tvDriverLastName.setText(fullName.toUpperCase());
        }

        holder.tvTeamName.setText(result.getTeamName());
        holder.tvDriverAcronym.setText(result.getDriverAcronym());

        int logoResId = context.getResources().getIdentifier(
                result.getTeamLogo(), "drawable", context.getPackageName());

        if (logoResId != 0) {
            holder.ivTeamLogo.setImageResource(logoResId);
        } else {
            holder.ivTeamLogo.setImageResource(R.drawable.default_team);
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition;
        ImageView ivTeamLogo;
        TextView tvDriverFirstName;
        TextView tvDriverLastName;
        TextView tvTeamName;
        TextView tvDriverAcronym;

        ViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            ivTeamLogo = itemView.findViewById(R.id.ivTeamLogo);
            tvDriverFirstName = itemView.findViewById(R.id.tvDriverFirstName);
            tvDriverLastName = itemView.findViewById(R.id.tvDriverLastName);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvDriverAcronym = itemView.findViewById(R.id.tvDriverAcronym);
        }
    }
}
