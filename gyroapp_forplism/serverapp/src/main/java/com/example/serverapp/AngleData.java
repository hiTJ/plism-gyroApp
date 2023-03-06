package com.example.serverapp;

import androidx.annotation.NonNull;

public class AngleData implements Cloneable{
    int direction;
    int pitchX;
    int rollY;
    int azimuthZ;
    int initialize;
    int deviceReset;

    public AngleData(int direction, float pitchX, float rollY, float azimuthZ, int initialize, int deviceReset){
        this.direction = direction;
        this.pitchX = (int)pitchX;
        this.rollY = (int)rollY;
        this.azimuthZ = (int)azimuthZ;
        this.initialize = initialize;
        this.deviceReset = deviceReset;
    }
    public AngleData(@NonNull AngleData angleData){
        this.direction = angleData.direction;
        this.pitchX = angleData.pitchX;
        this.rollY = angleData.rollY;
        this.azimuthZ = angleData.azimuthZ;
        this.initialize = angleData.initialize;
        this.deviceReset = angleData.deviceReset;
    }
}
