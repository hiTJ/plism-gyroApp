package com.example.gyroapp_forplism;

import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AngleDataMessageQueue extends ConcurrentLinkedQueue<AngleData>{
    public AngleDataMessageQueue(){}
    @Override
    public boolean add(@NonNull AngleData angleData) {
        return super.add(angleData);
    }
}
