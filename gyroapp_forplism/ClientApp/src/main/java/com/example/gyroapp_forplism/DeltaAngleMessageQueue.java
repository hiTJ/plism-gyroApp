package com.example.gyroapp_forplism;

import androidx.annotation.NonNull;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeltaAngleMessageQueue extends ConcurrentLinkedQueue<DeltaAngleData>{
    public DeltaAngleMessageQueue(){
    }

    @Override
    public boolean add(@NonNull DeltaAngleData deltaAngleData) {
        return super.add(deltaAngleData);
    }
}

