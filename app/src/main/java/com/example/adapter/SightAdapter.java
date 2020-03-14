package com.example.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bean.Sight;
import com.example.indoornavigation.R;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SightAdapter extends RecyclerView.Adapter<SightAdapter.ViewHolder> {
    private List<Sight> sightList;

    public SightAdapter(List<Sight> sights) {
        sightList = sights;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView sightName;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            sightName = view.findViewById(R.id.sight_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sight_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Sight sight = sightList.get(position);
                Toast.makeText(v.getContext(),"name:" + sight.getName(),Toast.LENGTH_SHORT).show();
                //点击景区跳转
//                int id = sight.getId();
//                Intent intent = new Intent(v.getContext(),ShowActivity.class);
//                intent.putExtra("id",id);
//                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Sight sight = sightList.get(i);
        viewHolder.sightName.setText(sight.getName());
    }

    @Override
    public int getItemCount() {
        return sightList.size();
    }
}
