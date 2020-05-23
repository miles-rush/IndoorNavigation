package com.example.indoornavigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Response;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.PolylineOptions;
import com.example.bean.Location;
import com.example.bean.Point;
import com.example.bean.Sight;
import com.example.bean.Spot;
import com.example.bean.Voice;
import com.example.handler.DeviceAttitudeHandler;
import com.example.handler.StepDetectionHandler;
import com.example.handler.StepPositioningHandler;
import com.example.tool.GsonUtil;
import com.example.tool.HttpUtil;
import com.example.tool.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TravelActivity extends AppCompatActivity {
    private Integer sightId;
    private Sight sight;
    private List<Point> sightPointList = new ArrayList<>();
    private List<Spot> spotList = new ArrayList<>();

    private MapView mapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle = null;

    private TextView nowLocationText;
    private TextView nowVoiceName;
    private Button testButton;

    private ImageView back;
    private ImageView voiceStop;
    private ImageView voiceRetry;
    private ImageView voiceGoOn;

    private LatLng gpsLatLng; //GPS定位时的坐标
    private LatLng doorLatLng; //起点坐标
    private LatLng nowLng; //室内定位点坐标

    List<LatLng> latLngs = new ArrayList<LatLng>();
    boolean isWalking = false;
    Location sLocation = new Location();

    private FloatingActionButton start;
    private FloatingActionButton end;

    private String nowPlayVoiceFilePath = null;
    private MusicService.PlayMusicBinder playMusicBinder;

    //传感器
    private SensorManager sensorManager;
    private StepDetectionHandler stepDetectionHandler;
    private StepPositioningHandler stepPositioningHandler;
    private DeviceAttitudeHandler deviceAttitudeHandler;

    //线程
    private JudgeThread judgeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        mapView = (MapView) findViewById(R.id.travel_map);
        mapView.onCreate(savedInstanceState);
        sightId = getIntent().getIntExtra("sightId",0);

        initMap();
        initUI();
        initLocation();
        getAllInfo();


        //音乐播放服务
        Intent intent = new Intent(TravelActivity.this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playMusicBinder = (MusicService.PlayMusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
                            gpsLatLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                            nowLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                        }
                    });
                }else {
                    Toast.makeText(TravelActivity.this,"GPS定位失败",Toast.LENGTH_SHORT).show();
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


    private void initUI() {
        nowLocationText = findViewById(R.id.travel_now_location);
        nowVoiceName = findViewById(R.id.travel_now_voice_name);

        back = findViewById(R.id.travel_back);
        voiceStop = findViewById(R.id.travel_voice_stop);
        voiceRetry = findViewById(R.id.travel_voice_retry);
        voiceGoOn = findViewById(R.id.travel_voice_go_on);

        start = findViewById(R.id.travel_start);
        end = findViewById(R.id.travel_end);

        testButton = findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nearLocation();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTravel(v);
            }
        });


        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTravel();
            }
        });


        voiceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicBinder.pause();
                Snackbar.make(v,"停止播放",Snackbar.LENGTH_SHORT).show();
            }
        });

        voiceGoOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicBinder.start();
                Snackbar.make(v,"继续播放",Snackbar.LENGTH_SHORT).show();
            }
        });

        voiceRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicBinder.stop();
                initMusicPlay();
                playMusicBinder.start();
                Snackbar.make(v,"再次播放",Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    private void startTravel(View v) {
        if (latLngs.size() > 0) {
            Snackbar.make(v,"再次导航请关闭当前界面后再进入！",Snackbar.LENGTH_SHORT).show();
        }else {
            if (nearLocation()) {
                //停止GPS定位
                mapLocationClient.stopLocation();
                aMap.setMyLocationEnabled(false);
                Toast.makeText(TravelActivity.this,"关闭GPS，开始室内景点导航",Toast.LENGTH_SHORT).show();

                //todo 演示用 将起点定位到博物馆
                LatLng testLat = new LatLng(30.250609,120.143709);
                gpsLatLng = testLat;
                doorLatLng = testLat;
                //移动到指定位置
                CameraPosition position = new CameraPosition(testLat,15,0,30);
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                aMap.moveCamera(update);
                aMap.moveCamera(CameraUpdateFactory.zoomTo(20));//缩放程度设置


                //传感器初始化
                initSensor();

                latLngs.add(doorLatLng);
                deviceAttitudeHandler.start();
                stepDetectionHandler.start();
                isWalking = true;

                judgeThread = new JudgeThread();
                judgeThread.start();

                //隐藏开始按钮 提交按钮设置为可见
                start.setVisibility(View.INVISIBLE);
                end.setVisibility(View.VISIBLE);

            }else {
                //提示
                Snackbar.make(v,"不在景区的入口坐标附近！",Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void endTravel() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(TravelActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否结束本次导航!");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deviceAttitudeHandler != null) {
                    deviceAttitudeHandler.stop();
                }
                if (stepDetectionHandler != null) {
                    stepDetectionHandler.stop();
                }
                keep = false;

                end.setVisibility(View.INVISIBLE);
                start.setVisibility(View.VISIBLE);

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

    //传感器相关内容初始化
    private void initSensor() {
        //获取传感器管理器对象
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        stepDetectionHandler = new StepDetectionHandler(sensorManager);
        stepDetectionHandler.setStepListener(mStepDetectionListener);
        deviceAttitudeHandler = new DeviceAttitudeHandler(sensorManager);
        stepPositioningHandler = new StepPositioningHandler();
        sLocation.setLongitude(gpsLatLng.longitude);
        sLocation.setLatitude(gpsLatLng.latitude);
        stepPositioningHandler.setmCurrentLocation(sLocation);

        //开始室内定位后 当前位置设置为起始位置
        nowLng = new LatLng(gpsLatLng.latitude,gpsLatLng.longitude);
    }


    public static int pointJudeTimes = 1;
    public static int pointUpdateTimes = 1;
    private int times = 0;
    private StepDetectionHandler.StepDetectionListener mStepDetectionListener = new StepDetectionHandler.StepDetectionListener(){
        public void newStep(float stepSize) {
            Location newLocation = stepPositioningHandler.computeNextStep(stepSize, deviceAttitudeHandler.orientationVals[0]);
            Log.d("LATLNG", sLocation.getLatitude() + " " + sLocation.getLongitude()+ " " + deviceAttitudeHandler.orientationVals[0]);
            if (isWalking) {
                //绘制新的点
                // gm.newPoint(new LatLng(newloc.getLatitude(), newloc.getLongitude()),dah.orientationVals[0]);
                //final LatLng lng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                nowLng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                latLngs.add(nowLng);
                aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(3).color(Color.argb(255,1,1,1)));
                updatePeopleLocation(nowLng);
                //travelInfoUpdate();

//                times++;
//                if (times % pointJudeTimes == 0) {
//                    travelInfoUpdate();
//                }
//                if (times % pointUpdateTimes == 0) {
//                    latLngs.add(nowLng);
//                    aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(10).color(Color.argb(255,1,1,1)));
//                }
            }
        }
    };

    //退出导航界面
    private void quit() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(TravelActivity.this);
        dialog.setTitle("信息:");
        dialog.setMessage("是否退出导航并关闭当前界面!");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                keep = false;
                finish();
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

    //初始数据加载
    private void getAllInfo() {
        HttpUtil.sendOkHttpGetRequest("/sight/query?id=" + sightId, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String sightsData = response.body().string();
                sight = GsonUtil.getSightJson(sightsData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawMap();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(TravelActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //显示地图所有标记
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
//        CameraPosition position = new CameraPosition(lng,15,0,30);
//        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
//        aMap.moveCamera(update);
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));//缩放程度设置
    }

    //开始室内定位后 更新游客所在的位置
    private Marker peopleIcon;
    private void updatePeopleLocation(LatLng lng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(lng);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_people)));
        markerOptions.title("你的位置");
        markerOptions.setFlat(true);

        if (peopleIcon != null) {
            peopleIcon.destroy();
        }

        peopleIcon = aMap.addMarker(markerOptions);
    }


    public static float nearMeter = 5; //判断坐标近似的阈值
    private String nearPointName;
    //定位开始前判断是否在某个sight入口附近
    private boolean nearLocation() {
        //AMapUtils.calculateLineDistance()
        List<Point> pointList = sight.getPoints();
        for (Point point:pointList) {
            nearPointName = point.getName();
            Double x = Double.parseDouble(point.getLatitude());
            Double y = Double.parseDouble(point.getLongitude());
            LatLng temp = new LatLng(x,y);

            float distance = AMapUtils.calculateLineDistance(gpsLatLng, temp);
            if (distance < nearMeter) {
                doorLatLng = temp; //起始坐标
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nowLocationText.setText("当前位置:" + nearPointName);
                    }
                });
                return true;
            }
        }
        return false;
    }

    public static final int UPDATE_TEXT = 1;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    travelInfoUpdate();
                    break;
                default:
                    break;
            }
        }
    };

    private boolean keep = true; //控制线程开关
    //线程 每隔2s对现在的坐标点进行判断
    private class JudgeThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (keep) {
                //travelInfoUpdate();
                Message message = new Message();
                message.what = UPDATE_TEXT;
                handler.sendMessage(message);
                try {
                    Thread.sleep(5000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static float judeMeter = 5;
    private String sightName;
    private Spot tempSpot;
    private String historySightName;
    private Spot historySpot;

    //运动时信息更新
    private synchronized boolean travelInfoUpdate() {
        //判断游客是否来到进入点
        sightPointList = sight.getPoints();
        for (Point point : sightPointList) {
            sightName = point.getName();
            Double x = Double.parseDouble(point.getLatitude());
            Double y = Double.parseDouble(point.getLongitude());
            LatLng temp = new LatLng(x,y);

            float distance = AMapUtils.calculateLineDistance(nowLng, temp);
            if (distance < judeMeter) {
                if (historySightName != null) {
                    if (historySightName.equals(sightName)) {
                        return false;//还是上一次的点范围内
                    }
                }
                historySightName = sightName;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nowLocationText.setText("当前位置:" + sightName);
                    }
                });
                return true;
            }
        }

        spotList = sight.getSpots();
        for (Spot spot : spotList) {
            if (spot.getPoint() != null) {
                if (spot.getPoint().getId() != null) {
                    Double x = Double.parseDouble(spot.getPoint().getLatitude());
                    Double y = Double.parseDouble(spot.getPoint().getLongitude());
                    LatLng temp = new LatLng(x,y);

                    float distance = AMapUtils.calculateLineDistance(nowLng, temp);
                    tempSpot = spot;
                    if (distance < judeMeter) {
                        if (historySpot != null) {
                            if (tempSpot.getName().equals(historySpot.getName())) {
                                return false;
                            }
                        }
                        historySpot = tempSpot;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nowLocationText.setText("当前位置:" + tempSpot.getName());
                                if (tempSpot.getVoices() != null) {
                                    if (tempSpot.getVoices().size() > 0) {
                                        Voice voice = tempSpot.getVoices().get(0);
                                        nowPlayVoiceFilePath = HttpUtil.RESOURCE_URL + voice.getResourcesPath();
                                        nowVoiceName.setText("景点音频:" + voice.getName());
                                        initMusicPlay();
                                        playMusicBinder.start();
                                    }
                                }
                            }
                        });
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //初始化音乐播放器
    private void initMusicPlay() {
        playMusicBinder.stop();
        playMusicBinder.init(nowPlayVoiceFilePath);
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
