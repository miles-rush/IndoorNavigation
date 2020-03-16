package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bean.ResponseCode;
import com.example.bean.Spot;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class SpotManagerActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;

    private ImageView recordVoice;
    private ImageView openFile;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;

    private FloatingActionButton updateSpot;
    private FloatingActionButton deleteSpot;

    private Integer spotId;
    private Spot spot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_manager);
        init();
        getSightInfo();
    }

    private void init() {
        spotId = getIntent().getIntExtra("spotId",0);
        back = findViewById(R.id.manager_spot_back);
        done = findViewById(R.id.manager_spot_done);

        name = findViewById(R.id.manager_spot_name);
        coordinate = findViewById(R.id.manager_spot_coordinate);
        introduce = findViewById(R.id.manager_spot_introduce);

        updateSpot = findViewById(R.id.update_spot);
        deleteSpot = findViewById(R.id.delete_spot);

        recordVoice = findViewById(R.id.manager_voice_in);
        openFile = findViewById(R.id.manager_open_voice_file);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSpot();
            }
        });

        updateSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入可编辑状态
                name.setEnabled(true);
                introduce.setEnabled(true);
                done.setVisibility(View.VISIBLE);
            }
        });

        deleteSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSpot();
            }
        });
    }

    //调用更新接口
    private void updateSpot() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",spotId.toString())
                .add("name",name.getText().toString().trim())
                .add("introduce",introduce.getText().toString().trim())
                .build();
        HttpUtil.sendOkHttpPostRequest("/spot/update", requestBody,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpotManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //加载景区信息
    private void getSightInfo() {
        HttpUtil.sendOkHttpGetRequest("/spot/query?id=" + spotId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                spot = GsonUtil.getSpotJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //基础信息的显示
                        name.setText(spot.getName());
                        introduce.setText(spot.getIntroduce());

                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSpot() {
        HttpUtil.sendOkHttpGetRequest("/spot/delete?id=" + spotId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpotManagerActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode() == "1") {//删除后关闭
                            finish();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SpotManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
