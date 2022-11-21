package com.example.gyroapp_forplism;

import androidx.annotation.NonNull;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GyroMessageQueue extends ConcurrentLinkedQueue<GyroData>{
    public GyroMessageQueue(){
    }
    private void MakeMessage(@NonNull GyroData gyroData){
        DateTimeFormatter dtformat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        String fdate = dtformat.format(gyroData.datetime);
        gyroData.message = String.format("%s,%f,%f,%f", fdate,gyroData.sensorX, gyroData.sensorY, gyroData.sensorZ);
    }

    @Override
    public boolean add(@NonNull GyroData gyroData) {
        MakeMessage(gyroData);
        return super.add(gyroData);
    }
}

