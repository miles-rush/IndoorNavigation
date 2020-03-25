package com.example.engine;

//步伐检测类
public class PedometerEngine {
    private static PedometerEngine pedometerEngine;

    private PedometerEngine(){}

    public static final PedometerEngine getInstance() {
        if (pedometerEngine == null) {
            pedometerEngine = new PedometerEngine();
        }
        return pedometerEngine;
    }


}
