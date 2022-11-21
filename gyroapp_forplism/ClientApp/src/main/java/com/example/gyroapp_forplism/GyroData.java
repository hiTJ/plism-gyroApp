package com.example.gyroapp_forplism;

import java.time.LocalDateTime;
import java.util.Date;

public class GyroData {
    LocalDateTime datetime;
    float sensorX;
    float sensorY;
    float sensorZ;
    String message;

    public GyroData(){
        datetime = LocalDateTime.now();
        sensorX = 0;
        sensorY = 0;
        sensorZ = 0;
        message = "";
    }
    public GyroData(LocalDateTime dt, float sX, float sY, float sZ, String m){
        datetime = dt;
        sensorX = sX;
        sensorY = sY;
        sensorZ = sZ;
        message = m;
    }
    public GyroData(float sX, float sY, float sZ){
        datetime = LocalDateTime.now();
        sensorX = sX;
        sensorY = sY;
        sensorZ = sZ;
        message = "";
    }
}
