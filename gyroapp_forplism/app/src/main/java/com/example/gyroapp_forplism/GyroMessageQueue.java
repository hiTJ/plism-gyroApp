package com.example.gyroapp_forplism;

import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentLinkedQueue;

public class GyroMessageQueue extends ConcurrentLinkedQueue<GyroData>{
    public GyroMessageQueue(){
    }
    private void MakeMessage(@NonNull GyroData gyroData){
        gyroData.message = String.format("%s,%f,%f,%f", gyroData.datetime,gyroData.sensorX, gyroData.sensorY, gyroData.sensorZ);
    }

    @Override
    public boolean add(@NonNull GyroData gyroData) {
        MakeMessage(gyroData);
        return super.add(gyroData);
    }
}

