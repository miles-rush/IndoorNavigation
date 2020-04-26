package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.example.bean.Location;
import com.example.bean.Point;
import com.example.bean.ResponseCode;
import com.example.bean.Sight;
import com.example.bean.Spot;
import com.example.engine.PedometerEngine;
import com.example.handler.DeviceAttitudeHandler;
import com.example.handler.StepDetectionHandler;
import com.example.handler.StepPositioningHandler;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndoorLocationActivity extends AppCompatActivity {
    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;

    private FloatingActionButton startLocation;
    private FloatingActionButton uploadLocation;
    private FloatingActionButton deleteLocation;

    private ImageView back;

    private TextView infoSpotName;
    private TextView infoSpotLocation;


    private LatLng latLng; //GPS定位时的坐标
    private LatLng doorLatLng; //起点坐标
    private LatLng nowLng; //室内定位点坐标

    List<LatLng> latLngs = new ArrayList<LatLng>();

    private Integer sightId;
    private Integer spotId;
    private Integer pointId;

    private Sight sight = null;
    private Spot spot = null;
    private List<Point> pointList = new ArrayList<>();


    //传感器
    private SensorManager sensorManager;
    private StepDetectionHandler stepDetectionHandler;
    private StepPositioningHandler stepPositioningHandler;
    private DeviceAttitudeHandler deviceAttitudeHandler;

    boolean isWalking = false;
    Location sLocation = new Location();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_location);

        mapView = (MapView) findViewById(R.id.indoor_location_map);
        mapView.onCreate(savedInstanceState);

        sightId = getIntent().getIntExtra("sightId",0);
        spotId = getIntent().getIntExtra("spotId",0);



        initMap();
        initLocation();
        initUI();
        //initSensor();
//        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//        List<Sensor> sensorsList = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        String infoText = "";
//        for (Sensor sensor : sensorsList) {
//            if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
//                infoText += "1ok";
//            }
//            if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//                infoText += "2ok";
//            }
//        }
//        info.setText(infoText);
    }

    //传感器相关内容初始化
    private void initSensor() {
        //获取传感器管理器对象
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        stepDetectionHandler = new StepDetectionHandler(sensorManager);
        stepDetectionHandler.setStepListener(mStepDetectionListener);
        deviceAttitudeHandler = new DeviceAttitudeHandler(sensorManager);
        stepPositioningHandler = new StepPositioningHandler();
        sLocation.setLongitude(latLng.longitude);
        sLocation.setLatitude(latLng.latitude);
        stepPositioningHandler.setmCurrentLocation(sLocation);
    }

    //todo test
    List<Float> list = new ArrayList<>();

    private StepDetectionHandler.StepDetectionListener mStepDetectionListener = new StepDetectionHandler.StepDetectionListener(){
        public void newStep(float stepSize) {
            Location newLocation = stepPositioningHandler.computeNextStep(stepSize, deviceAttitudeHandler.orientationVals[0]);
            Log.d("LATLNG", sLocation.getLatitude() + " " + sLocation.getLongitude()+ " " + deviceAttitudeHandler.orientationVals[0]);
            if (isWalking) {
                //绘制新的点
                // gm.newPoint(new LatLng(newloc.getLatitude(), newloc.getLongitude()),dah.orientationVals[0]);
                //final LatLng lng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                nowLng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String x = "";
                        list = PedometerEngine.getInstance().getThresholdList();
                        if (list.size() > 0) {
                            for (float i:list) {
                                x = x + i + "|";
                            }
                        }
                        infoSpotLocation.setText("Js:"+x+"||"+nowLng.latitude+","+nowLng.longitude);
                    }
                });
                latLngs.add(nowLng);
                aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255,1,1,1)));
            }
        }
    };

    //UI初始化
    private void initUI() {
        startLocation = findViewById(R.id.indoor_start_location); //开始定位
        uploadLocation = findViewById(R.id.indoor_upload_location); //上传当前的坐标关联到景点
        deleteLocation = findViewById(R.id.indoor_delete_location); //删除当前坐标

        back = findViewById(R.id.indoor_location_back);

        infoSpotName = findViewById(R.id.info_spot_name);
        infoSpotLocation = findViewById(R.id.info_spot_location);

        getSightInfo();
        getSpotInfo();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(IndoorLocationActivity.this);
                dialog.setTitle("信息:");
                dialog.setMessage("是否要删除当前景点坐标信息!");
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
        });

        startLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nearLocation()) {
                    //停止GPS定位
                    mapLocationClient.stopLocation();
                    aMap.setMyLocationEnabled(false);
                    Toast.makeText(IndoorLocationActivity.this,"关闭GPS，开始室内定位",Toast.LENGTH_SHORT).show();

                    //传感器初始化
                    initSensor();

                    latLngs.add(doorLatLng);
                    deviceAttitudeHandler.start();
                    stepDetectionHandler.start();
                    isWalking = true;

                    //隐藏开始按钮 提交按钮设置为可见
                    startLocation.setVisibility(View.INVISIBLE);
                    uploadLocation.setVisibility(View.VISIBLE);

                }else {
                    //提示
                    Snackbar.make(v,"不在景区的入口坐标附近！",Snackbar.LENGTH_SHORT).show();
                }
            }
        });



        uploadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //提交当前坐标后 按钮可视情况重置
                beforeAddCheck();
            }
        });
    }



    private void delete() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("id",pointId.toString())
                .build();
        HttpUtil.sendOkHttpPostRequest("/spotPoint/delete", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IndoorLocationActivity.this, responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        if (responseCode.getCode().equals("1")) {//删除后关闭
                            finish();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(IndoorLocationActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void beforeAddCheck() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(IndoorLocationActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否保存坐标为:"+nowLng.latitude+","+nowLng.longitude+"的位置为该景点的坐标!");
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
        RequestBody requestBody = new FormBody.Builder()
                .add("latitude",""+nowLng.latitude)
                .add("longitude",""+nowLng.longitude)
                .add("id",spotId.toString())
                .build();
        HttpUtil.sendOkHttpPostRequest("/spotPoint/add", requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                final ResponseCode responseCode = GsonUtil.getResponseJson(responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IndoorLocationActivity.this,responseCode.getInfo(),Toast.LENGTH_SHORT).show();
                        startLocation.setVisibility(View.VISIBLE);
                        uploadLocation.setVisibility(View.INVISIBLE);
                        //todo 添加成功后
                        finish();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IndoorLocationActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //下载数据 显示数据
    private void getSpotInfo() {
        HttpUtil.sendOkHttpGetRequest("/spot/query?id=" + spotId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String spotData = response.body().string();
                spot = GsonUtil.getSpotJson(spotData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //基础信息的显示
                        infoSpotName.setText("景点:" + spot.getName());
                        //判断是否已有数据 已有的话显示信息 和 删除按钮
                        if (spot.getPoint() != null) {
                            if (spot.getPoint().getId() != null) {
                                pointId = spot.getPoint().getId();
                                String lat = spot.getPoint().getLatitude();
                                String lon = spot.getPoint().getLongitude();
                                if (lat.length() > 0 && lon.length() > 0) {
                                    int length = 7;
                                    if (lat.length() < length) {
                                        length = lat.length();
                                    }
                                    if (lon.length() < length) {
                                        length = lon.length();
                                    }
                                    String loc = lat.substring(0,length) + "," + lon.substring(0,length);
                                    infoSpotLocation.setText("坐标:" + loc);

                                    deleteLocation.setVisibility(View.VISIBLE);
                                    startLocation.setVisibility(View.INVISIBLE);

                                    //显示 景点的地图标记
//                                    MarkerOptions markerOptions = new MarkerOptions();
//                                    Double x = Double.parseDouble(lat);
//                                    Double y = Double.parseDouble(lon);
//                                    LatLng latLng = new LatLng(x,y);
//                                    markerOptions.position(latLng);
//                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.spot_point)));
//                                    markerOptions.title(spot.getName());
//                                    markerOptions.setFlat(true);
//                                    Marker marker = aMap.addMarker(markerOptions);
//                                    marker.showInfoWindow();
                                }
                            }
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(IndoorLocationActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSightInfo() {
        HttpUtil.sendOkHttpGetRequest("/sight/query?id=" + sightId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightData = response.body().string();
                sight = GsonUtil.getSightJson(sightData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //基础信息的显示
                        if (sight.getPoints() != null) {
                            if (sight.getPoints().size() > 0) {
                                pointList = sight.getPoints();
                            }
                        }
                        showSightDoor();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(IndoorLocationActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static float nearMeter = 2; //判断坐标近似的阈值
    //定位开始前判断是否在某个sight入口附近
    private boolean nearLocation() {
        //AMapUtils.calculateLineDistance()
        for (Point point:pointList) {
            Double x = Double.parseDouble(point.getLatitude());
            Double y = Double.parseDouble(point.getLongitude());
            LatLng temp = new LatLng(x,y);

            float distance = AMapUtils.calculateLineDistance(latLng, temp);
            if (distance < nearMeter) {
                doorLatLng = temp; //起始坐标
                return true;
            }
        }
        return false;
    }
    //显示地图上的标记点-入口起始位置
    private void showSightDoor() {
        if (pointList != null) {
            aMap.clear(true);//重置
            if (pointList.size() > 0) {
                for (Point point:pointList) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    Double x = Double.parseDouble(point.getLatitude());
                    Double y = Double.parseDouble(point.getLongitude());
                    LatLng latLng = new LatLng(x,y);
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.map_point_s)));
                    markerOptions.title(point.getName());
                    markerOptions.setFlat(true);
                    Marker marker = aMap.addMarker(markerOptions);
                    marker.showInfoWindow();
                }
            }
        }

        for (Spot spot : sight.getSpots()) {
            if (spot.getId() == spotId) {
                if (spot.getPoint() != null) {
                    if (spot.getPoint().getId() != null) {
                        String lat = spot.getPoint().getLatitude();
                        String lon = spot.getPoint().getLongitude();
                        //显示 景点的地图标记
                        MarkerOptions markerOptions = new MarkerOptions();
                        Double x = Double.parseDouble(lat);
                        Double y = Double.parseDouble(lon);
                        LatLng latLng = new LatLng(x,y);
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.spot_point)));
                        markerOptions.title(spot.getName());
                        markerOptions.setFlat(true);
                        Marker marker = aMap.addMarker(markerOptions);
                        marker.showInfoWindow();
                    }
                }
            }
        }

    }

    //地图相关的参数
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
        aMap.setMyLocationEnabled(true);
    }

    int jude = 1;
    //定位相关参数
    private AMapLocationClient mapLocationClient = null;
    public AMapLocationListener mapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(final AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功
                    latLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                }else {
                    Toast.makeText(IndoorLocationActivity.this,"GPS定位失败",Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceAttitudeHandler != null) {
            deviceAttitudeHandler.stop();
        }
        if (stepDetectionHandler != null) {
            stepDetectionHandler.stop();
        }
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

