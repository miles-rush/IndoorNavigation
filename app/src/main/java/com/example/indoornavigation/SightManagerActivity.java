package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
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

import com.example.bean.ResponseCode;
import com.example.bean.Sight;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class SightManagerActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;

    private FloatingActionButton addSpot;
    private FloatingActionButton updateSight;
    private FloatingActionButton deleteSight;

    private RecyclerView spotRecyclerView;

    private Integer sightId;
    private Sight sight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_manager);
        init();
        //初始数据加载
        getSightInfo();
        //
        initButtons();
    }

    private void init() {
        sightId = getIntent().getIntExtra("sightId", 0);

        back = findViewById(R.id.manager_sight_back);
        done = findViewById(R.id.manager_sight_done);

        name = findViewById(R.id.manager_sight_name);
        coordinate = findViewById(R.id.input_sight_coordinate);
        introduce = findViewById(R.id.manager_sight_introduce);

        addSpot = findViewById(R.id.add_spot);
        updateSight = findViewById(R.id.update_sight);
        deleteSight = findViewById(R.id.delete_sight);

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
                        name.setText(sight.getName());
                        introduce.setText(sight.getIntroduce());

                        //加载数据后使得不可编辑 不可提交
                        name.setEnabled(false);
                        introduce.setEnabled(false);
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

            }
        });
        //更新景区信息
        updateSight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //文本框可编辑 提交按钮可见
                name.setEnabled(true);
                introduce.setEnabled(true);
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
                .build();
        HttpUtil.sendOkHttpPostRequest("/sight/update", requestBody,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SightManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode() == "1") {
                            //更新成功 隐藏按钮 设置不可编辑
                            name.setEnabled(false);
                            introduce.setEnabled(false);
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
        HttpUtil.sendOkHttpGetRequest("/sight/delete?id=" + sightId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SightManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode() == "1") {//删除后关闭
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
