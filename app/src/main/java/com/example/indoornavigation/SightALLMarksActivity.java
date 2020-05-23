package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Response;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.example.bean.Sight;
import com.example.bean.Spot;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;

import java.io.IOException;
import java.util.List;

public class SightALLMarksActivity extends AppCompatActivity {
    private Integer sightId;

    private ImageView back;

    private Sight sight;

    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_all_marks);
        mapView = (MapView) findViewById(R.id.all_marks_map);
        mapView.onCreate(savedInstanceState);


        sightId = getIntent().getIntExtra("sightId",0);

        initMap();
        initUI();

        getSightInfo();
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
        back = findViewById(R.id.all_marks_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                        drawMap();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SightALLMarksActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void drawMap() {
        LatLng lng = null;
        List<Point> pointList = sight.getPoints();
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
                    markerOptions.title("景区入口").snippet(point.getName());
                    markerOptions.setFlat(true);
                    Marker marker = aMap.addMarker(markerOptions);
                    marker.showInfoWindow();
                    lng = latLng;
                }
            }
        }

        List<Spot> spotList = sight.getSpots();
        for (Spot spot : spotList) {
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
                    markerOptions.title("景点").snippet(spot.getName());
                    markerOptions.setFlat(true);
                    Marker marker = aMap.addMarker(markerOptions);
                    marker.showInfoWindow();
                }
            }
        }

        //移动到指定位置
        CameraPosition position = new CameraPosition(lng,15,0,30);
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        aMap.moveCamera(update);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));//缩放程度设置

    }


}
