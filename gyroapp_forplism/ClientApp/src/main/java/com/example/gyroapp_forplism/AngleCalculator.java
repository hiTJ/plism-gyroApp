package com.example.gyroapp_forplism;

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
    private AngleData previousAngleData;
    private DeltaAngleData deltaAngleData;

    public AngleCalculator(){
        angleDataList = new ArrayList<AngleData>();
        currentAngleData = new AngleData(0,0,0,0,0,0);
        previousAngleData = new AngleData(0,0,0,0,0,0);
        deltaAngleData = new DeltaAngleData(0,0,0);
    }
    public  AngleData getCurrentAngleData(){
        return this.currentAngleData;
    }
    private DeltaAngleData calcDeltaAngle(){
        this.deltaAngleData = new DeltaAngleData(currentAngleData, previousAngleData);

        return deltaAngleData;
    }

    private float radianToDegrees(float angrad) {
        double deg = Math.toDegrees(angrad);
        return  (float)Math.round(deg);
        //return  (float)Math.floor(angrad >= 0 ? deg : 360 + deg);
    }

    public void setAccelerometer(float[] values){
        this.accelerometerValues = values;
    }

    public void setMagneticValue(float[] values){
        this.magneticValues = values;
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
        return new AngleData(0, x, y, z, 0, 0);
    }
    
    public void calcAngle(boolean needInitialize, boolean needResetDevice) {
        if (this.magneticValues != null && this.accelerometerValues != null) {
            float[] rotationMatrix = new float[MATRIX_SIZE];
            float[] inclinationMatrix = new float[MATRIX_SIZE];
            float[] remapedMatrix = new float[MATRIX_SIZE];
            float[] orientationValues = new float[DIMENSION];
            // 加速度センサーと地磁気センサーから回転行列を取得
            SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, this.accelerometerValues, this.magneticValues);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
            SensorManager.getOrientation(remapedMatrix, orientationValues);
            // ラジアン値を変換し、それぞれの回転角度を取得する
            float azimuthZ = radianToDegrees(orientationValues[0]);
            float pitchX = radianToDegrees(orientationValues[1]);
            float rollY = radianToDegrees(orientationValues[2]);
            AngleData angleData = new AngleData(0, pitchX, rollY, azimuthZ,0, 0);
            if (angleDataList.size() >= MAXSIZE_ANGLE_DATA) {
                angleDataList.remove(0);
            }
            angleDataList.add(angleData);
            currentAngleData = getAverageAngleData();
            currentAngleData.initialize = needInitialize ? 1 : 0;

            Log.d("DEBUG", "Current: " + currentAngleData.azimuthZ + ", " + currentAngleData.rollY + ", " + currentAngleData.pitchX);
            Log.d("DEBUG", "Current: " + previousAngleData.azimuthZ + ", " + previousAngleData.rollY + ", " + previousAngleData.pitchX);
            calcDeltaAngle();
            DeltaAngleData.cloneObject(previousAngleData, currentAngleData);
        }
    }
}
