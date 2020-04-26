package com.example.handler;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.engine.PedometerEngine;

public class StepCounterHandler extends Activity implements SensorEventListener {
    SensorManager sm;
    Sensor sensor;



    private StepDetectionHandler.StepDetectionListener mStepDetectionListener;

    int step = 0;

    public StepCounterHandler(SensorManager sm) {
        super();
        this.sm = sm;
        //sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);//线性加速度传感器
        sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);//计步传感器

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

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

        }

    }

    public void onNewStepDetected(float length) {
        //float distanceStep = 0.6f;//步长设置
        float distanceStep = length;
        step++;
        mStepDetectionListener.newStep(distanceStep);
    }

    public void setStepListener(StepDetectionHandler.StepDetectionListener listener) {
        mStepDetectionListener = listener;
    }

    public interface StepDetectionListener {
        public void newStep(float stepSize);
    }
}
