package com.example.indoornavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bean.Point;
import com.example.engine.PedometerEngine;

import java.util.List;


public class TestActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;


    private TextView mTxtValue;
    private TextView varText;
    private Button test;
    private Button testR;


    private Sensor mSensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        engine = PedometerEngine.getInstance();
        mTxtValue = (TextView) findViewById(R.id.show_test_text);
        varText = findViewById(R.id.show_test_var);
        test = findViewById(R.id.test);
        testR = findViewById(R.id.test_r);
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 获取传感器的类型(TYPE_ACCELEROMETER:加速度传感器)
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<Float> list = engine.getThresholdList();
//                String s = "";
//                for (float i:list) {
//                    s = s + i + "|";
//                }
//                varText.setText("方差数目:" + list.size() + "当前步长:" + engine.getLength());
            }
        });

        testR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<Float> list = engine.getAcc();
//                String s = "";
//                for (float i:list) {
//                    s = s + i + "|";
//                }
                varText.setText("当前步数:" + step);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为加速度传感器注册监听器
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消监听
        mSensorManager.unregisterListener(this);
    }

    // 当传感器的值改变的时候回调该方法
    PedometerEngine engine;
    float x;
    float y;
    float z;
    int i = 0;
    int step = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                step++;
            }
        }

//        float[] values = event.values;
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("X方向的加速度：");
//
//        sb.append(values[0]);
//
//        sb.append("\nY方向的加速度：");
//
//        sb.append(values[1]);
//
//        sb.append("\nZ方向的加速度：");
//
//        sb.append(values[2]);
//
//        mTxtValue.setText(sb.toString());
//        i++;
//
//
//        x = event.values[0];
//        y = event.values[1];
//        z = event.values[2];
//        if (i > 300) {
//            engine.addAvg(x,y,z);
//            engine.calculationVarChange();
//        }
    }

    // 当传感器精度发生改变时回调该方法
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

