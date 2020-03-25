package com.example.handler;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepDetectionHandler extends Activity implements SensorEventListener {
    SensorManager sm;
    Sensor sensor;

    private StepDetectionListener mStepDetectionListener;

    int step = 0;

    public StepDetectionHandler(SensorManager sm) {
        super();
        this.sm = sm;
        sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);//线性传感器
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
        float y;
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            y = event.values[1];

            if (y > 1 && mStepDetectionListener != null) {
                onNewStepDetected();
            }
        }
    }

    public void onNewStepDetected() {
        float distanceStep = 0.6f;//步长设置
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
