package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.SightAdapter;
import com.example.bean.Sight;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private List<Sight> sightList = new ArrayList<>();

    private LinearLayoutManager manager;
    private SightAdapter sightAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private FloatingActionButton addSight;

    private ImageView userManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        init();
        loadSights();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        addSight = findViewById(R.id.add_sight);
        recyclerView = findViewById(R.id.sight_list);
        swipeRefreshLayout = findViewById(R.id.sight_swipe);
        userManager = findViewById(R.id.user_manager_in);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSights();
            }
        });

        addSight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AddSightActivity.class);
                startActivity(intent);
            }
        });

        userManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, UserManagerActivity.class);
                Integer userId = getIntent().getIntExtra("userId", 0);
                intent.putExtra("userId", userId); //传递用户ID
                startActivity(intent);
            }
        });
    }

    private void initList() {
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        sightAdapter = new SightAdapter(sightList);
        recyclerView.setAdapter(sightAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void loadSights() {
        HttpUtil.sendOkHttpGetRequest("/sight/list", new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                System.out.println(sightsData);
                sightList = GsonUtil.getSightsJson(sightsData);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initList();
                        //sightAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AdminActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
