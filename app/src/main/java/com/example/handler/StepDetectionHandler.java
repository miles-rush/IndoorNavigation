package com.example.handler;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.engine.PedometerEngine;

public class StepDetectionHandler extends Activity implements SensorEventListener {
    SensorManager sm;
    Sensor sensor;
    PedometerEngine pedometerEngine;

    private StepDetectionListener mStepDetectionListener;

    int step = 0;

    public StepDetectionHandler(SensorManager sm) {
        super();
        this.sm = sm;
        //sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);//线性加速度传感器
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//加速度传感器
        pedometerEngine = PedometerEngine.getInstance();
    }

    public void start() {
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sm.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x;
        float y;
        float z;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            //y > 1
            if (y > 5 && mStepDetectionListener != null) {
                onNewStepDetected(0.2f);
            }

            //采样到加速度后 添加到列表之中
//            pedometerEngine.addAvg(x,y,z);
//            if (pedometerEngine.calculationVarChange() && mStepDetectionListener != null) {
//                onNewStepDetected(0.5f);
//            }

        }

    }

    public void onNewStepDetected(float length) {
        //float distanceStep = 0.6f;//步长设置
        float distanceStep = length;
        step++;
        mStepDetectionListener.newStep(distanceStep);
    }

    public void setStepListener(StepDetectionListener listener) {
        mStepDetectionListener = listener;
    }

    public interface StepDetectionListener {
        public void newStep(float stepSize);
    }

}
