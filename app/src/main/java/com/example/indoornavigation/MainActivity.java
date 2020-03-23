package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bean.ResponseCode;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //public static String API_URL = "http://10.0.2.2:8080";//本机地址

    private EditText account;
    private EditText password;
    private Button login;
    private Button register;

    private CheckBox autoLogin;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        init();
        getPermission();
        //自动登录检测
        autoLogin();
        //事件绑定
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountText = account.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                login(accountText,passwordText);
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
                HttpUtil.sendOkHttpPostRequest("/user/register", requestBody, new okhttp3.Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                if (responseCode.getCode().equals("1")) {//注册后跳转

                                }
                            }
                        });
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

    private void getPermission() {
        //权限申请
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            //locationUtil.requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //locationUtil.requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    //自动登录勾选检查
    private void check(String account, String password) {
        editor =preferences.edit();
        if (autoLogin.isChecked()) {
            editor.putBoolean("auto", true);
            editor.putString("account" , account);
            editor.putString("password", password);
        }else {
            editor.clear();
        }
        editor.apply();
    }

    private void autoLogin() {
        boolean isAuto = preferences.getBoolean("auto",false);
        if (isAuto) {
            autoLogin.setChecked(true);
            String accountText = preferences.getString("account", "");
            String passwordText = preferences.getString("password", "");
            login(accountText, passwordText);
        }
    }

    private void login(final String accountText, final String passwordText) {
        RequestBody requestBody = new FormBody.Builder()
                .add("account",accountText)
                .add("password",passwordText)
                .build();
        HttpUtil.sendOkHttpPostRequest("/user/login", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {//普通用户
                            check(accountText, passwordText);
                            Intent intent = new Intent(MainActivity.this, UserActivity.class);
                            intent.putExtra("userId", responseCode.getAdditionalId()); //登录后传递该账户的ID
                            startActivity(intent);
                            finish();
                        }else if (responseCode.getCode().equals("2")) {//管理员
                            check(accountText, passwordText);
                            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                            intent.putExtra("userId", responseCode.getAdditionalId()); //登录后传递该账户的ID
                            startActivity(intent);
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
                        Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
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
        autoLogin = (CheckBox) findViewById(R.id.auto_login);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
