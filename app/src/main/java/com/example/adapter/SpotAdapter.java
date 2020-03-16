package com.example.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bean.Sight;
import com.example.bean.Spot;
import com.example.indoornavigation.AddSpotActivity;
import com.example.indoornavigation.R;
import com.example.indoornavigation.SightManagerActivity;
import com.example.indoornavigation.SpotManagerActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.ViewHolder> {
    private List<Spot> spotList;

    public SpotAdapter(List<Spot> spotList) {
        this.spotList = spotList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView spotName;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            spotName = view.findViewById(R.id.spot_name);
        }
    }

    @NonNull
    @Override
    public SpotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spot_item, viewGroup, false);
        final SpotAdapter.ViewHolder holder = new SpotAdapter.ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Spot spot = spotList.get(position);
                Toast.makeText(v.getContext(),"name:" + spot.getName(),Toast.LENGTH_SHORT).show();
                int spotId = spot.getId();
                Intent intent = new Intent(v.getContext(), SpotManagerActivity.class);
                intent.putExtra("spotId",spotId);
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SpotAdapter.ViewHolder viewHolder, int i) {
        Spot spot = spotList.get(i);
        viewHolder.spotName.setText(spot.getName());
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }
}
