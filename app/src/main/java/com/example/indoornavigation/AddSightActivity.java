package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
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
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;

import java.io.IOException;

public class AddSightActivity extends AppCompatActivity {
    private ImageView back;
    private ImageView done;

    private EditText name;
    private EditText coordinate;
    private EditText introduce;

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
    }

    /*
    坐标内容尚未添加
     */
    private void saveSight() {
        String nameText = name.getText().toString().trim();
        String coordinateText = coordinate.getText().toString().trim();
        String introduceText = introduce.getText().toString().trim();
        RequestBody requestBody = new FormBody.Builder()
                .add("name",nameText)
                .add("introduce",introduceText)
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
                            //添加成功返回
                            finish();
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
