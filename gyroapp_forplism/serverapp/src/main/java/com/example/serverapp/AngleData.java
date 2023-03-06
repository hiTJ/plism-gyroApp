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

    //@Override
    //public AngleData clone(){
    //    AngleData angleData = null;

    //    try {
    //        angleData = (AngleData) super.clone();
    //        angleData.pitchX = this.pitchX;
    //        angleData.rollY = this.rollY;
    //        angleData.azimuthZ = this.azimuthZ;
    //    }catch(CloneNotSupportedException ex){
    //        ex.printStackTrace();
    //    }

    //    return angleData;
    //}
}
