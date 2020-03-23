package com.example.adapter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bean.Sight;
import com.example.bean.Spot;
import com.example.indoornavigation.R;
import com.example.indoornavigation.ShowSightActivity;
import com.example.tool.HttpUtil;
import com.example.tool.MusicService;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.BIND_AUTO_CREATE;

public class ShowSpotAdapter extends RecyclerView.Adapter<ShowSpotAdapter.ViewHolder> {
    private List<Spot> spotList;

    public ShowSpotAdapter(List<Spot> spots) {
        spotList = spots;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView spotName;
        TextView spotIntroduce;
        ImageView voiceStart;
        ImageView voiceStop;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            spotName = view.findViewById(R.id.show_spot_name);
            spotIntroduce = view.findViewById(R.id.show_spot_introduce);
            voiceStart = view.findViewById(R.id.show_spot_voice);
            voiceStop = view.findViewById(R.id.show_spot_voice_stop);
        }
    }

    @NonNull
    @Override
    public ShowSpotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_spot_item, viewGroup, false);
        final ShowSpotAdapter.ViewHolder holder = new ShowSpotAdapter.ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
//                Sight sight = sightList.get(position);
//                Toast.makeText(v.getContext(),"name:" + sight.getName(),Toast.LENGTH_SHORT).show();
//                int sightId = sight.getId();
//                Intent intent = new Intent(v.getContext(), ShowSightActivity.class);
//                intent.putExtra("sightId",sightId);
//                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ShowSpotAdapter.ViewHolder viewHolder, int i) {
        final Spot spot = spotList.get(i);
        viewHolder.spotName.setText(spot.getName());
        viewHolder.spotIntroduce.setText(spot.getIntroduce());

        viewHolder.voiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spot.getVoices() != null) {
                    if (spot.getVoices().size() > 0){
                        if (spot.getVoices().get(0) != null) {
                            String path = HttpUtil.RESOURCE_URL + spot.getVoices().get(0).getResourcesPath();
                            initMusic(path);
                        }
                    }
                }
                mediaPlayer.start();
                viewHolder.voiceStart.setVisibility(View.GONE);
                viewHolder.voiceStop.setVisibility(View.VISIBLE);
            }
        });

        viewHolder.voiceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                viewHolder.voiceStart.setVisibility(View.VISIBLE);
                viewHolder.voiceStop.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }
    private MediaPlayer mediaPlayer = new MediaPlayer();
    public void initMusic(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

}
