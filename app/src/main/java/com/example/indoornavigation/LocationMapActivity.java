package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.bean.ResponseCode;
import com.example.bean.Sight;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class LocationMapActivity extends AppCompatActivity {
    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;

    private TextView locationText;
    private ImageView back;
    private EditText locationName;
    private FloatingActionButton addPoint;

    private String latitude = "";//纬度
    private String longitude = ""; //经度

    private Integer sightId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_map);

        mapView = (MapView) findViewById(R.id.location_map);
        mapView.onCreate(savedInstanceState);

        sightId = getIntent().getIntExtra("sightId",0);

        initMap();
        initUI();
        initLocation();
    }

    //地图相关的参数
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000);

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.showIndoorMap(true);//显示室内地图
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));//缩放程度设置
        aMap.setMyLocationEnabled(true);
    }

    //定位相关参数
    private AMapLocationClient mapLocationClient = null;
    public AMapLocationListener mapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(final AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //更新当前位置经纬度
                            latitude = aMapLocation.getLatitude() + "";
                            longitude = aMapLocation.getLongitude() + "";
                            String locationInfo = "[" + aMapLocation.getLatitude() + "," + aMapLocation.getLongitude() + "]";
                            locationText.setText(locationInfo);
                        }
                    });
                }else {
                    Toast.makeText(LocationMapActivity.this,"GPS定位失败",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    private AMapLocationClientOption aMapLocationClientOption;
    private void initLocation() {
        mapLocationClient = new AMapLocationClient(getApplicationContext());
        mapLocationClient.setLocationListener(mapLocationListener);

        aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        aMapLocationClientOption.setInterval(2000);
        aMapLocationClientOption.setHttpTimeOut(20000);
        mapLocationClient.setLocationOption(aMapLocationClientOption);
        mapLocationClient.startLocation();
    }

    //UI
    private void initUI() {
        locationText = findViewById(R.id.location_text);
        locationName = findViewById(R.id.location_name);
        back = findViewById(R.id.location_back);
        addPoint = findViewById(R.id.add_point);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(latitude.equals("")&&longitude.equals(""))) {
                    beforeAddCheck();
                }else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LocationMapActivity.this);
                    dialog.setTitle("信息:");
                    dialog.setMessage("无定位数据！");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void beforeAddCheck() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(LocationMapActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否保存坐标为:"+latitude+","+longitude+"的位置为该景区下起始点!");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addPointInfo();
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

    private void addPointInfo() {
        String name = locationName.getText().toString().trim();
        if (name.equals("")) {
            name = "NO_NAME";
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("name",name)
                .add("id",sightId.toString())
                .add("coordinate",latitude + "," + longitude)
                .build();
        HttpUtil.sendOkHttpPostRequest("/point/add", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LocationMapActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {

                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LocationMapActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        mapLocationClient.stopLocation();
        mapLocationClient.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}
