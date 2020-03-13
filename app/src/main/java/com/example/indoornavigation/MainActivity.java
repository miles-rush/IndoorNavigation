package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bean.ResponseCode;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    public static String API_URL = "http://10.0.2.2:8080";//本机地址



    private EditText account;
    private EditText password;
    private Button login;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        init();
        //事件绑定
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountText = account.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                RequestBody requestBody = new FormBody.Builder()
                        .add("account",accountText)
                        .add("password",passwordText)
                        .build();
                HttpUtil.sendOkHttpPostRequest(API_URL + "/user/login", requestBody, new okhttp3.Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                        if (responseCode.getCode() == "1") {//普通用户
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                }
                            });
                            //跳转

                        }else if (responseCode.getCode() == "2") {//管理员
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                }
                            });
                            //跳转

                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountText = account.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                RequestBody requestBody = new FormBody.Builder()
                        .add("account",accountText)
                        .add("password",passwordText)
                        .build();
                HttpUtil.sendOkHttpPostRequest(API_URL + "/user/register", requestBody, new okhttp3.Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                        if (responseCode.getCode() == "1") {
                            runOnUiThread(new Runnable() {//注册成功
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                }
                            });
                            //跳转

                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void init() {
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
    }
}
