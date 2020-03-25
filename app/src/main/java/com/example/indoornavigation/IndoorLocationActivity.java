package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import com.amap.api.maps.model.MyLocationStyle;

public class IndoorLocationActivity extends AppCompatActivity {
    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_location);

        mapView = (MapView) findViewById(R.id.location_map);
        mapView.onCreate(savedInstanceState);

        initMap();
        initLocation();
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

                        }
                    });
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
