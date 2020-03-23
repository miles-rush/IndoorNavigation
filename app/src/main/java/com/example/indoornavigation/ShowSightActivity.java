package com.example.indoornavigation;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Response;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.adapter.ShowSightAdapter;
import com.example.adapter.ShowSpotAdapter;
import com.example.bean.Sight;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.example.tool.MusicService;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.List;

public class ShowSightActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView sightImageView;
    private TextView sightContentText;
    private ActionBar actionBar;


    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private ShowSpotAdapter spotAdapter;

    private Integer sightId;
    private Sight sight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sight);
        sightId = getIntent().getIntExtra("sightId",0);
        init();
        getSightInfo();
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        sightImageView = findViewById(R.id.sight_image_view);
        sightContentText = findViewById(R.id.sight_content_text);
        recyclerView = findViewById(R.id.show_sight_spot_list);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Glide.with(this).load(R.drawable.background_top).into(sightImageView);

    }

    //加载信息后的UI设置
    private void afterInit() {
        int color = getResources().getColor(R.color.colorBlack);
        collapsingToolbarLayout.setCollapsedTitleTextColor(color);
        collapsingToolbarLayout.setTitle(sight.getName());
        //扩展introduce字段
        String text = "";
        for (int i=0;i<=10;i++) {
            text += sight.getIntroduce();
        }
        sightContentText.setText(text);
        //初始化景点列表
        initList();
    }

    private void initList() {
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        spotAdapter = new ShowSpotAdapter(sight.getSpots());
        recyclerView.setAdapter(spotAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void getSightInfo() {
        HttpUtil.sendOkHttpGetRequest("/sight/query?id=" + sightId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                sight = GsonUtil.getSightJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //基础信息的显示
                        afterInit();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ShowSightActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                spotAdapter.stopMusic();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
