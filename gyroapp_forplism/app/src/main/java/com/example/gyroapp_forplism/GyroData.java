package com.example.gyroapp_forplism;

import java.util.Date;

public class GyroData {
    Date datetime;
    float sensorX;
    float sensorY;
    float sensorZ;
    String message;

    public GyroData(){
        datetime = new Date();
        sensorX = 0;
        sensorY = 0;
        sensorZ = 0;
        message = "";
    }
    public GyroData(Date dt, float sX, float sY, float sZ, String m){
        datetime = dt;
        sensorX = sX;
        sensorY = sY;
        sensorZ = sZ;
        message = m;
    }
    public GyroData(float sX, float sY, float sZ){
        datetime = new Date();
        sensorX = sX;
        sensorY = sY;
        sensorZ = sZ;
        message = "";
    }
}
