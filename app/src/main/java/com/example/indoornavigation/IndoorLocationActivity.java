package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.example.bean.Location;
import com.example.handler.DeviceAttitudeHandler;
import com.example.handler.StepDetectionHandler;
import com.example.handler.StepPositioningHandler;

import java.util.ArrayList;
import java.util.List;

public class IndoorLocationActivity extends AppCompatActivity {
    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;

    private ImageView startLocation;
    private TextView info;

    private LatLng latLng;
    List<LatLng> latLngs = new ArrayList<LatLng>();

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

    private StepDetectionHandler.StepDetectionListener mStepDetectionListener = new StepDetectionHandler.StepDetectionListener(){
        public void newStep(float stepSize) {
            Location newLocation = stepPositioningHandler.computeNextStep(stepSize, deviceAttitudeHandler.orientationVals[0]);
            Log.d("LATLNG", sLocation.getLatitude() + " " + sLocation.getLongitude()+ " " + deviceAttitudeHandler.orientationVals[0]);
            if (isWalking) {
                //绘制新的点
                // gm.newPoint(new LatLng(newloc.getLatitude(), newloc.getLongitude()),dah.orientationVals[0]);
                final LatLng lng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("Js:"+lng.latitude+","+lng.longitude+"/n");
                    }
                });
                latLngs.add(lng);
                aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255,1,1,1)));
            }
        }
    };
    //UI初始化
    private void initUI() {
        startLocation = findViewById(R.id.indoor_start_location);
        info = findViewById(R.id.info);


        startLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止GPS定位
                //mapLocationClient.stopLocation();
                aMap.setMyLocationEnabled(false);
                Toast.makeText(IndoorLocationActivity.this,"关闭GPS，开始室内定位",Toast.LENGTH_SHORT).show();
                initSensor();

                latLngs.add(latLng);
                deviceAttitudeHandler.start();
                stepDetectionHandler.start();
                isWalking = true;
            }
        });
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
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        mapLocationClient.stopLocation();
        mapLocationClient.onDestroy();

        deviceAttitudeHandler.stop();
        stepDetectionHandler.stop();
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

