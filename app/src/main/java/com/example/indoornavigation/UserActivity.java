package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.adapter.ShowSightAdapter;
import com.example.adapter.SightAdapter;
import com.example.bean.Sight;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    private EditText searchText;
    private ImageView search;
    private ImageView userManager;

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private ShowSightAdapter sightAdapter;

    private List<Sight> sightList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        init();
        loadSights();
    }

    private void initList(List<Sight> sights) {
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        sightAdapter = new ShowSightAdapter(sights);
        recyclerView.setAdapter(sightAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void init() {
        searchText = findViewById(R.id.search_text);
        search = findViewById(R.id.search);
        userManager = findViewById(R.id.user_manager_in);
        recyclerView = findViewById(R.id.user_sight_list);
        //跳转到管理界面
        userManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, UserManagerActivity.class);
                Integer userId = getIntent().getIntExtra("userId", 0);
                intent.putExtra("userId", userId); //传递用户ID
                startActivity(intent);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = searchText.getText().toString().trim();
                List<Sight> temp = new ArrayList<>();
                for (Sight s:sightList) {
                    if (s.getName().contains(text)) {
                        temp.add(s);
                    }
                }
                initList(temp);
            }
        });
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
                        initList(sightList);
                        //sightAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(UserActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }


}
