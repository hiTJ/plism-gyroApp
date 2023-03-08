package com.example.gyroapp_forplism;

import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class AngleCalculator {
    private static final int MAXSIZE_ANGLE_DATA = 20;
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

    private float calcAverageNum(float p, float n, int pSize, int nSize){
        if(pSize > 0){
            p = p / pSize;
        }
        if(nSize > 0){
            n = n / nSize;
        }
        return p + n;
    }
    @NonNull
    @Contract(" -> new")
    private AngleData getAverageAngleData(){
        float x = 0, y = 0, z = 0;
        float px = 0, py = 0, pz = 0;
        float nx = 0, ny = 0, nz = 0;

        int dataSize = this.angleDataList.size();
        int pxNumSize = 0, pyNumSize = 0, pzNumSize = 0;
        int nxNumSize = 0, nyNumSize = 0, nzNumSize = 0;
        for(int i = 0; i < dataSize; i++){
            AngleData data = angleDataList.get(i);
            if(data.pitchX >= 0){
                px = px + data.pitchX;
                pxNumSize++;
            }else{
                nx = nx + data.pitchX;
                nxNumSize++;
            }
            if(data.rollY >= 0){
                py = py + data.rollY;
                pyNumSize++;
            }else{
                ny = ny + data.rollY;
                nyNumSize++;
            }
            if(data.azimuthZ >= 0){
                pz = pz + data.azimuthZ;
                pzNumSize++;
            }else{
                nz = nz + data.azimuthZ;
                nzNumSize++;
            }
        }
        x = calcAverageNum(px, nx, pxNumSize, nxNumSize);
        y = calcAverageNum(py, ny, pyNumSize, nyNumSize);
        z = calcAverageNum(pz, nz, pzNumSize, nzNumSize);

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
            while (angleDataList.size() >= MAXSIZE_ANGLE_DATA) {
                angleDataList.remove(0);
            }
            angleDataList.add(angleData);
            currentAngleData = getAverageAngleData();
            currentAngleData.initialize = needInitialize ? 1 : 0;
            currentAngleData.deviceReset = needResetDevice ? 1 : 0;

            Log.d("DEBUG", "Current: " + currentAngleData.azimuthZ + ", " + currentAngleData.rollY + ", " + currentAngleData.pitchX);
            Log.d("DEBUG", "Current: " + previousAngleData.azimuthZ + ", " + previousAngleData.rollY + ", " + previousAngleData.pitchX);
            calcDeltaAngle();
            DeltaAngleData.cloneObject(previousAngleData, currentAngleData);
        }
    }
}
