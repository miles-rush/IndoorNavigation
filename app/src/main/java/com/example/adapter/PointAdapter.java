package com.example.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bean.Point;
import com.example.bean.Sight;
import com.example.indoornavigation.R;
import com.example.indoornavigation.SightManagerActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.ViewHolder> {

    private List<Point> pointList;

    public PointAdapter(List<Point> points) {
        pointList = points;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView pointName;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            pointName = view.findViewById(R.id.point_name);
        }
    }

    @NonNull
    @Override
    public PointAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.point_item, viewGroup, false);
        final PointAdapter.ViewHolder holder = new PointAdapter.ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Point point = pointList.get(position);
                Toast.makeText(v.getContext(),"name:" + point.getName(),Toast.LENGTH_SHORT).show();
//                int sightId = sight.getId();
//                Intent intent = new Intent(v.getContext(), SightManagerActivity.class);
//                intent.putExtra("sightId",sightId);
//                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PointAdapter.ViewHolder viewHolder, int i) {
        Point point = pointList.get(i);
        viewHolder.pointName.setText(point.getName());
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }
}
