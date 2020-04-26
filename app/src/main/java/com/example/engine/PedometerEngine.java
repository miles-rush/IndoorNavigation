package com.example.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import androidx.recyclerview.widget.LinearSmoothScroller;

//步伐检测类-检测是否走了一步
public class PedometerEngine {
    private static PedometerEngine pedometerEngine;

    private static int w = 15;//滑动窗口的值

    private int p;//滑动窗口的计数
    private int step;

    private List<Float> accList = new Vector<>();//存储每个采样点的加速度
    private List<Float> thresholdList = new Vector<>();//每个移动窗口的阈值

    private List<Float> tempAcc = new Vector<>();//存储一步中所有的加速度值

    public List<Float> getThresholdList() {
        return thresholdList;
    }
    public List<Float> getAcc() {
        return accList;
    }

    public int getSize() {
        return accList.size();
    }

    public int getStep() {
        return this.step;
    }

    private PedometerEngine(){
        p = w; //初始化滑动窗口的位置为 step*W
        s = 0;
        e = 0;

        step = 0;
        isWalking = false;
    }

    public static final PedometerEngine getInstance() {
        if (pedometerEngine == null) {
            pedometerEngine = new PedometerEngine();
        }
        return pedometerEngine;
    }

    private static float T1 = 2; //检查大幅度迈步时期具有较高的加速度
    private static float T2 = 1; //检测脚与地面接触的站立时期

    private boolean isWalking; //判断起步开始 到 起步结束
    //迈出一步的判断条件
    //1.从高加速度往低加速度转变
    //2.在当前样本i之前窗口大小w之中至少有一个低加速度被检测到
    public synchronized boolean calculationVarChange() {
        if (accList.size() >= w*2+1) {
            float acc = accList.get(p);
            float avg = getAvgAccList(p);//平均值
            float var = getVar(p, avg);//方差
            thresholdList.add(var);
            p++;
            if (isWalking == false) {
                //当前没有在走动
                //判断是否出现高加速度
                if (var > T1) {
                    isWalking = true;
                    tempAcc.add(acc);
                }else {
                    return false;
                }

            }else if (isWalking) {
                //当前在走动
                //判断是否走动结束
                if (var > T2) {
                    tempAcc.add(acc);
                    return false;//一直是高加速度 这一步没有停止
                }else {
                    //出现低加速度 表明这一步结束落地
                    step++;
                    isWalking = false;
                    strideLength();//计算这步的步长
                    return true;
                }
            }
            return false;
        }else {
            //采样点不足 无法判断是否迈动一步
            return false;
        }
    }

    //计算三轴加速度的合值
    private float getAvgXYZ(float x, float y, float z) {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public synchronized void addAvg(float x, float y, float z) {
        accList.add(getAvgXYZ(x,y,z));
    }

    //计算加速度的平均值  传入采样点i
    public float getAvgAccList(int i) {
        int left = i-w;
        int right = i+w;
        float sum = 0;
        for (int t = left;t <= right;t++) {
            sum += accList.get(t);
        }
        return sum/(2*w + 1);
    }


    //计算加速度方差 传入采样点i和加速度平均值
    public float getVar(int i,float avg) {
        int left = i-w;
        int right = i+w;
        float sum = 0;
        for (int t = left;t <= right;t++) {
            float j = accList.get(t);
            float temp = (j-avg) * (j-avg);
            sum += temp;
        }
        float var = (float)Math.sqrt(sum/(2*w+1));
        return var;
    }

    private int s;//对应一步的起始位置
    private int e;//对应一步的结束位置
    private float length = 0;
    //计算步长
    //使用一步的加速度变化来计算
    private synchronized void strideLength() {
        float sum = 0;
        for (float f:tempAcc) {
            sum += Math.abs(f);
        }
        int num = tempAcc.size();
        float result = 0.98f * (float) Math.pow(sum/(num*1.0),1/3.0);
        length = result;
        tempAcc.clear();
    }

    public float getLength() {
        return this.length;
    }
}
