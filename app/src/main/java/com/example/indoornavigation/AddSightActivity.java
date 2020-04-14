package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.adapter.PointAdapter;
import com.example.adapter.SpotAdapter;
import com.example.bean.Point;
import com.example.bean.ResponseCode;
import com.example.bean.Sight;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddSightActivity extends AppCompatActivity {
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

    //判断当前景区是否保存
    private Integer sightId = 0;
    private Sight sight = null;
    private List<Point> points = new ArrayList<>();
    private LinearLayoutManager manager;
    private PointAdapter pointAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sight);
        init();
    }

    private void init() {
        back = findViewById(R.id.add_sight_back);
        done = findViewById(R.id.add_sight_done);

        name = findViewById(R.id.input_sight_name);
        coordinate = findViewById(R.id.input_sight_coordinate);
        introduce = findViewById(R.id.input_sight_introduce);

        address = findViewById(R.id.input_sight_address);
        contact = findViewById(R.id.input_sight_contact);

        getLocation = findViewById(R.id.add_get_location);
        pointsSwipe = findViewById(R.id.add_points_swipe);
        pointRecyclerView = findViewById(R.id.add_points_list);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSight();
            }
        });

        pointsSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (sightId == 0) {
                    pointsSwipe.setRefreshing(false);
                }else {
                    getSightInfo();
                }
            }
        });

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sightId == 0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AddSightActivity.this);
                    dialog.setTitle("信息:");
                    dialog.setMessage("请先保存当前景区信息！");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    Intent intent = new Intent(AddSightActivity.this,LocationMapActivity.class);
                    intent.putExtra("sightId", sightId);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sightId != 0) {
            getSightInfo();
        }
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
                        if (sight != null) {
                            if (sight.getPoints() != null) {
                                points = sight.getPoints();
                            }
                            initPointList();
                            pointsSwipe.setRefreshing(false);
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AddSightActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveSight() {
        String nameText = name.getText().toString().trim();
        String coordinateText = coordinate.getText().toString().trim();
        String introduceText = introduce.getText().toString().trim();
        String addressText = address.getText().toString().trim();
        String contactText = contact.getText().toString().trim();

        RequestBody requestBody = new FormBody.Builder()
                .add("name",nameText)
                .add("introduce",introduceText)
                .add("address",addressText)
                .add("contact",contactText)
                .build();
        HttpUtil.sendOkHttpPostRequest("/sight/add", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddSightActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            //添加成功 隐藏提交按钮 允许添加入口坐标位置
                            done.setVisibility(View.INVISIBLE);
                            name.setEnabled(false);
                            introduce.setEnabled(false);
                            address.setEnabled(false);
                            contact.setEnabled(false);
                            sightId = responseCode.getAdditionalId();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddSightActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
