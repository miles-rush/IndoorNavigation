package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bean.ResponseCode;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;

import java.io.IOException;

public class UserManagerActivity extends AppCompatActivity {
    private ImageView back;

    private TextView account;
    private TextView exitPassword;
    private TextView loginOut;

    private ImageView about;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Integer userId;//登录用户的ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manager);
        init();
    }

    //用户信息的显示
    private void showUserAccount() {
        HttpUtil.sendOkHttpGetRequest("/user/query?id=" + userId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String accountData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        account.setText(accountData);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(UserManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        about = findViewById(R.id.about);
        back = findViewById(R.id.user_manager_back);
        account = findViewById(R.id.user_manager_name);
        exitPassword = findViewById(R.id.user_manager_edit_password);
        loginOut = findViewById(R.id.user_manager_exit_account);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //初始化当前账户信息
        userId = getIntent().getIntExtra("userId",0);
        showUserAccount();
        //退出当前界面
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //关于软件
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(UserManagerActivity.this);
                dialog.setTitle("关于:");
                dialog.setMessage("Make by miles.Github:https://github.com/miles-rush/IndoorNavigation");
                dialog.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        //修改密码
        exitPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText oldPassword = new EditText(UserManagerActivity.this);
                final EditText newPassword = new EditText(UserManagerActivity.this);
                AlertDialog.Builder checkDialog = new AlertDialog.Builder(UserManagerActivity.this);
                checkDialog.setTitle("输入旧密码").setView(oldPassword).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String oldPasswordText = oldPassword.getText().toString().trim();
                        AlertDialog.Builder newPasswordDialog = new AlertDialog.Builder(UserManagerActivity.this);
                        newPasswordDialog.setTitle("输入新密码").setView(newPassword)
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String newPasswordText = newPassword.getText().toString().trim();
                                //调用修改密码接口
                                RequestBody requestBody = new FormBody.Builder()
                                        .add("id", userId.toString())
                                        .add("oldPassword", oldPasswordText)
                                        .add("newPassword", newPasswordText)
                                        .build();
                                HttpUtil.sendOkHttpPostRequest("/user/editPassword", requestBody,new okhttp3.Callback() {
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String responseData = response.body().string();
                                        final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(UserManagerActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        //需要修改自动登录保存的文件
                                        if (responseCode.getCode() == "1") {
                                            editor =preferences.edit();
                                            editor.remove("password");
                                            editor.putString("password",newPasswordText);
                                            editor.apply();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Toast.makeText(UserManagerActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).show();
                    }
                }).show();
            }
        });
        //退出账户
        loginOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除自动登录保存的信息
                editor =preferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(UserManagerActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
