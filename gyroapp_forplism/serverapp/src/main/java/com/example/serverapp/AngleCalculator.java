package com.example.serverapp;

import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class AngleCalculator {
    private static final int MAXSIZE_ANGLE_DATA = 10;
    private static final int MATRIX_SIZE = 16;
    private static final int DIMENSION = 3;
    private ArrayList<AngleData> angleDataList;
    private float[] accelerometerValues;
    private float[] magneticValues;
    private AngleData currentAngleData;
    private AngleData initializedAngleData;

    public AngleCalculator(){
        angleDataList = new ArrayList<AngleData>();
        currentAngleData = new AngleData(0,0,0,0,0,0);
        initializedAngleData = new AngleData(0,0,0,0,0,0);
    }
    public  AngleData getCurrentAngleData(){
        return this.currentAngleData;
    }

    private float radianToDegrees(float angrad) {
        double deg = Math.toDegrees(angrad);
        return  (float)Math.round(deg);
        //return  (float)Math.floor(angrad >= 0 ? deg : 360 + deg);
    }

    @NonNull
    @Contract(" -> new")
    private AngleData getAverageAngleData(){
        float x = 0, y = 0, z = 0;

        int dataSize = this.angleDataList.size();
        for(int i = 0; i < dataSize; i++){
            AngleData data = angleDataList.get(i);
            x = x + data.pitchX;
            y = y + data.rollY;
            z = z + data.azimuthZ;
        }
        x = x / dataSize;
        y = y / dataSize;
        z = z / dataSize;
        return new AngleData(0, x, y, z, 0,0);
    }
    
    public void calcAngle() {

    }
}
