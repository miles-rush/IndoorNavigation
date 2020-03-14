package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Response;

import android.os.Bundle;
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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSights();
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
                sightList = GsonUtil.getSightJson(sightsData);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initList();
                        //sightAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        initList();
//                    }
//                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AdminActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
