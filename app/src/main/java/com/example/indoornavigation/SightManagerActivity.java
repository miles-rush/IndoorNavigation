package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.adapter.PointAdapter;
import com.example.adapter.SightAdapter;
import com.example.adapter.SpotAdapter;
import com.example.bean.Point;
import com.example.bean.ResponseCode;
import com.example.bean.Sight;
import com.example.bean.Spot;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SightManagerActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;

    private EditText address;
    private EditText contact;

    //定位的相关UI
    private ImageView getLocation;
    private SwipeRefreshLayout pointsSwipe;
    private RecyclerView pointRecyclerView;

    private FloatingActionButton addSpot;
    private FloatingActionButton updateSight;
    private FloatingActionButton deleteSight;

    private SwipeRefreshLayout spotSwipeRefreshLayout;
    private RecyclerView spotRecyclerView;
    private LinearLayoutManager manager;
    private SpotAdapter spotAdapter;
    private List<Spot> spotList = new ArrayList<>();

    private List<Point> points = new ArrayList<>();
    private LinearLayoutManager pointManager;
    private PointAdapter pointAdapter;

    private Integer sightId;
    private Sight sight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_manager);
        init();
        //初始数据加载
        getSightInfo();
        //事件初始化
        initButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSightInfo();
    }

    private void init() {
        sightId = getIntent().getIntExtra("sightId", 0);

        back = findViewById(R.id.manager_sight_back);
        done = findViewById(R.id.manager_sight_done);

        name = findViewById(R.id.manager_sight_name);
        coordinate = findViewById(R.id.input_sight_coordinate);
        introduce = findViewById(R.id.manager_sight_introduce);

        address = findViewById(R.id.manager_sight_address);
        contact = findViewById(R.id.manager_sight_contact);

        addSpot = findViewById(R.id.add_spot);
        updateSight = findViewById(R.id.update_sight);
        deleteSight = findViewById(R.id.delete_sight);

        spotRecyclerView = findViewById(R.id.sight_spot_list);
        spotSwipeRefreshLayout = findViewById(R.id.sight_spot_swipe);

        getLocation = findViewById(R.id.manager_get_location);
        pointsSwipe = findViewById(R.id.manager_points_swipe);
        pointRecyclerView = findViewById(R.id.manager_points_list);

        spotSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新景点列表
                getSightInfo();
            }
        });

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SightManagerActivity.this,LocationMapActivity.class);
                intent.putExtra("sightId", sightId);
                startActivity(intent);
            }
        });

        pointsSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSightInfo();
            }
        });
    }

    //景区下景点信息列表显示
    private void initSpotList() {
        spotList = sight.getSpots();
        manager = new LinearLayoutManager(this);
        spotRecyclerView.setLayoutManager(manager);
        spotAdapter = new SpotAdapter(spotList);
        spotRecyclerView.setAdapter(spotAdapter);
        spotRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void initPointList() {
        points = sight.getPoints();
        manager = new LinearLayoutManager(this);
        pointRecyclerView.setLayoutManager(manager);
        pointAdapter = new PointAdapter(points);
        pointRecyclerView.setAdapter(pointAdapter);
        pointRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
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
                        name.setText(sight.getName());
                        introduce.setText(sight.getIntroduce());
                        address.setText(sight.getAddress());
                        contact.setText(sight.getContact());
                        //景区下景点列表显示
                        initSpotList();
                        //景区下坐标列表显示
                        points = sight.getPoints();
                        initPointList();
                        //加载数据后使得不可编辑 刷新状态清除
                        spotSwipeRefreshLayout.setRefreshing(false);
                        pointsSwipe.setRefreshing(false);
                        name.setEnabled(false);
                        introduce.setEnabled(false);
                        address.setEnabled(false);
                        contact.setEnabled(false);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SightManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initButtons() {
        //添加景区下的景点
        addSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SightManagerActivity.this, AddSpotActivity.class);
                intent.putExtra("sightId", sightId);
                startActivity(intent);
            }
        });
        //更新景区信息
        updateSight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //文本框可编辑 提交按钮可见
                name.setEnabled(true);
                introduce.setEnabled(true);
                address.setEnabled(true);
                contact.setEnabled(true);
                done.setVisibility(View.VISIBLE);
            }
        });
        //删除这个景区
        deleteSight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSight();
            }
        });
        //调用提交接口
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSight();
            }
        });
        //关闭
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //调用更新接口
    private void updateSight() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",sightId.toString())
                .add("name",name.getText().toString().trim())
                .add("introduce",introduce.getText().toString().trim())
                .add("address",address.getText().toString().trim())
                .add("contact",contact.getText().toString().trim())
                .build();
        HttpUtil.sendOkHttpPostRequest("/sight/update", requestBody,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SightManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            //更新成功 隐藏按钮 设置不可编辑
                            name.setEnabled(false);
                            introduce.setEnabled(false);
                            address.setEnabled(false);
                            contact.setEnabled(false);
                            done.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SightManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //调用删除接口
    private void deleteSight() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",sightId.toString())
                .build();
        HttpUtil.sendOkHttpPostRequest("/sight/delete", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SightManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {//删除后关闭
                            finish();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SightManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
