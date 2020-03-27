package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.bean.Point;
import com.example.bean.ResponseCode;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class ShowMarksActivity extends AppCompatActivity {
    private TextView locationText;
    private ImageView back;
    private ImageView done;//提交更新
    private EditText locationName;

    private FloatingActionButton delete;
    private FloatingActionButton inEdit;//进入编辑状态

    private Integer pointId;
    private Point point = null;

    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_marks);
        mapView = (MapView) findViewById(R.id.show_map);
        mapView.onCreate(savedInstanceState);

        pointId = getIntent().getIntExtra("pointId",0);
        initMap();
        initUI();

        getPointInfo();
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);

        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000);

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.showIndoorMap(true);//显示室内地图
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));//缩放程度设置
        aMap.setMyLocationEnabled(false);//不显示定位蓝点

    }

    private void initUI() {
        locationText = findViewById(R.id.manager_location_text);
        locationName = findViewById(R.id.manager_location_name);

        back = findViewById(R.id.manager_location_back);
        done = findViewById(R.id.manager_local_done);

        delete = findViewById(R.id.delete_point);
        inEdit = findViewById(R.id.edit_point);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        inEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationName.setEnabled(true);
                done.setVisibility(View.VISIBLE);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beforeDelete();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beforeUpdate();
            }
        });
    }

    private void beforeDelete() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ShowMarksActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否删除当前坐标信息！");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void delete() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",pointId.toString())
                .build();
        HttpUtil.sendOkHttpPostRequest("/point/delete", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShowMarksActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {//删除后关闭
                            finish();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ShowMarksActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void beforeUpdate() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ShowMarksActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否更新当前坐标信息！");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                update();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //更新成功后 编辑框锁定 提交按钮隐藏
    private void update() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",pointId.toString())
                .add("name",locationName.getText().toString().trim())
                .build();
        HttpUtil.sendOkHttpPostRequest("/point/update", requestBody,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ShowMarksActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {
                            //更新成功 隐藏按钮 设置不可编辑
                            locationName.setEnabled(false);
                            done.setVisibility(View.INVISIBLE);
                            getPointInfo();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ShowMarksActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getPointInfo() {
        HttpUtil.sendOkHttpGetRequest("/point/query?id=" + pointId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                point = GsonUtil.getPointJson(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //基础信息的显示
                        if (point != null) {
                            locationText.setText("["+point.getLatitude()+","+point.getLongitude()+"]");
                            locationName.setText(point.getName());
                            drawMap();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ShowMarksActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //绘制标记点
    private void drawMap() {
        if (point != null) {
            aMap.clear(true);
            MarkerOptions markerOptions = new MarkerOptions();
            Double x = Double.parseDouble(point.getLatitude());
            Double y = Double.parseDouble(point.getLongitude());
            LatLng latLng = new LatLng(x,y);
            //移动到指定位置
            CameraPosition position = new CameraPosition(latLng,15,0,30);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            aMap.moveCamera(update);
            aMap.moveCamera(CameraUpdateFactory.zoomTo(20));//缩放程度设置
            //绘制标记
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.map_point_s)));
            markerOptions.title(point.getName());
            markerOptions.setFlat(true);
            Marker marker = aMap.addMarker(markerOptions);
            marker.showInfoWindow();

        }
    }
}
