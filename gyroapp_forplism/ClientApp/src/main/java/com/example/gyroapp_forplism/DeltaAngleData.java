package com.example.gyroapp_forplism;

import android.util.Log;

public class DeltaAngleData {
    float deltaPitchX = 0;
    float deltaRollY = 0;
    float deltaAzimuthZ = 0;

    public DeltaAngleData(float deltaPitchX, float deltaRollY, float deltaAzimuthZ){
        this.deltaPitchX = deltaPitchX;
        this.deltaRollY = deltaRollY;
        this.deltaAzimuthZ = deltaAzimuthZ;
    }
    public DeltaAngleData(AngleData current, AngleData previous){
        //if(previous == null){
        //    this.deltaPitchX = current.pitchX;
        //    this.deltaRollY = current.rollY;
        //    this.deltaAzimuthZ = current.azimuthZ;;
        //}
        if(current == null || previous ==null){
            return;
        }
        this.deltaPitchX = current.pitchX - previous.pitchX;
        this.deltaRollY = current.rollY - previous.rollY;
        this.deltaAzimuthZ = current.azimuthZ - previous.azimuthZ;
    }
    public static void cloneObject(AngleData x, AngleData y){
        x.pitchX = y.pitchX;
        x.rollY = y.rollY;
        x.azimuthZ = y.azimuthZ;
    }
    public static boolean isEqualData(AngleData current, AngleData previous){
        if (current == previous){
            Log.d("DEBUG", "isEqualData: current and previous are same object!");
            return true;
        }
        return (current.pitchX == previous.pitchX && current.rollY == previous.rollY && current.azimuthZ == previous.azimuthZ);
    }
}
