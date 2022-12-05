package com.example.gyroapp_forplism;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AngleData implements Cloneable{
    float pitchX;
    float rollY;
    float azimuthZ;

    public AngleData(float pitchX, float rollY, float azimuthZ){
        this.pitchX = pitchX;
        this.rollY = rollY;
        this.azimuthZ = azimuthZ;
    }
    public AngleData(@NonNull AngleData angleData){
        this.pitchX = angleData.pitchX;
        this.rollY = angleData.rollY;
        this.azimuthZ = angleData.azimuthZ;
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
