package com.example.serverapp;

import androidx.annotation.NonNull;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue extends ConcurrentLinkedQueue<String>{
    public MessageQueue(){
    }
    @Override
    public boolean add(@NonNull String message) {
        return super.add(message);
    }
}

